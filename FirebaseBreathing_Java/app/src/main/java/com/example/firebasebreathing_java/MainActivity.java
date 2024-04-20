package com.example.firebasebreathing_java;

import static java.lang.StrictMath.round;

// Importovať potrebné knižnice Android a Java
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Importy TensorFlow a Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.firebasebreathing_java.models.User;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.Classifications;

// Štandardné nástroje Java
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.MutableBoolean;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

// Hlavná trieda aktivity, ktorá spracováva všetky interakcie s používateľom a údaje zo senzorov
public class MainActivity extends AppCompatActivity {

    // Odkazy na databázu Firebase na ukladanie rôznych typov údajov
    FirebaseDatabase database;
    DatabaseReference myRef, myStorageVideoRef, myStorageGameRef, relaxedRef, hrRef, gsrRef, levelIDRef, levelNameRef;

    // Deklarované prvky používateľského rozhrania
    private Button startStopButton, measureRelaxedStateButton;
    private EditText editTextUserID, editTextSessionNumber, editTextTemperature, editTextHumidity;
    private TextView tvHeartRateValue, tvGSRValue, tvTimestampValue;
    public Spinner spinnerExposureAmount;
    public CheckBox cbFearOfHeights, cbGameSession;
    public TextView tvRespirationIntensityValue, tvRespirationRateValue;
    private SyncUpdateData syncUpdateData ;
    private MeasureRelaxedState measureRelaxedState;

    // Kód žiadosti o povolenie na nahrávanie zvuku
    private static final int REQUEST_RECORD_AUDIO_CODE = 1;

    // Objekty na nahrávanie a klasifikáciu zvuku
    private AudioRecord audioRecord;
    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;
    private TimerTask timerTask;
    private final float[] breathingScores = new float[50];
    private final float[] breathCount = new float[150];
    private int index = 0;
    private float sum = 0;
    private int respirationIntensity;
    private float respirationRate;
    private int currentIndex = 0;
    private boolean wasBreathing = false;
    MutableBoolean arraysFilled = new MutableBoolean(false);
    MutableBoolean cbGameSessionChecked = new MutableBoolean(false);

    // Inicializácia aktivity, nastavenie rozloženia a oprávnení
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Skontrolujte povolenie na nahrávanie zvuku, ak nie je udelené, požiadajte oň
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_CODE);
        }  // púšťa sa aplikácia

        // Inicializácia klasifikátora zvuku na spracovanie zvukov
        try {
            audioClassifier = AudioClassifier.createFromFile(MainActivity.this, "yamnet.tflite");
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Error initializing audio classifier", Toast.LENGTH_LONG).show();
            return;
        }

        tensorAudio = audioClassifier.createInputTensorAudio();
        audioRecord = audioClassifier.createAudioRecord();

        // Prepojenie prvkov používateľského rozhrania s príslušnými zobrazeniami v rozvrhnutí
        editTextUserID = findViewById(R.id.editTextUserID);
        editTextSessionNumber = findViewById(R.id.editTextSessionNumber);
        editTextTemperature = findViewById(R.id.editTextTemperature);
        editTextHumidity = findViewById(R.id.editTextHumidity);

        tvHeartRateValue = findViewById(R.id.tvHeartRate);
        tvGSRValue = findViewById(R.id.tvGSR);
        tvRespirationRateValue = findViewById(R.id.tvBF);
        tvTimestampValue = findViewById(R.id.tvTime);
        tvRespirationIntensityValue = findViewById(R.id.tvClassification);

        cbGameSession = findViewById(R.id.cbGameSession);

        // Nastavenie poslucháča pre tlačidlo štart/stop
        startStopButton = findViewById(R.id.startStopButton);
        measureRelaxedStateButton = findViewById(R.id.measureRelaxedStateButton);

        cbGameSession.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(cbGameSessionChecked.value){
                    cbGameSessionChecked.value = false;
                }else {
                    cbGameSessionChecked.value = true;
                }
            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                // Spracovanie akcií spustenia alebo zastavenia
                int userID;
                int userSessionNr;

                try {
                    userID = Integer.parseInt(editTextUserID.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    userSessionNr = Integer.parseInt(editTextSessionNumber.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                syncUpdateData.setUserAndSessionID(userID, userSessionNr);

                if (syncUpdateData.isRunning()) {
                    stopListening(view);
                    syncUpdateData.stop();
                    cbGameSession.setEnabled(true);
                    audioRecord.startRecording();
                    startStopButton.setText("Start");
                } else {
                    startListening(view);
                    syncUpdateData.start();
                    cbGameSession.setEnabled(false);
                    audioRecord.startRecording();
                    startStopButton.setText("Stop");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Data gathering started, please be patient.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        measureRelaxedStateButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                int userID;
                int userSessionNr;
                float temperature;
                float humidity;

                try {
                    userID = Integer.parseInt(editTextUserID.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    userSessionNr = Integer.parseInt(editTextSessionNumber.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    temperature = Float.parseFloat(editTextTemperature.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    humidity = Float.parseFloat(editTextHumidity.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                measureRelaxedState.setData(userID, userSessionNr, temperature, humidity);

                if (measureRelaxedState.isRunning()) {
                    stopListening(view);
                    measureRelaxedState.stop();
                    measureRelaxedStateButton.setText("Start Measuring - Relaxed State");
                } else {
                    startListening(view);
                    measureRelaxedState.start();
                    measureRelaxedStateButton.setText("Stop Measuring - Relaxed State");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Data gathering started, please be patient.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // Inicializácia odkazu na databázu Firebase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("User");
        myStorageVideoRef = database.getReference("Storage/Video");
        myStorageGameRef = database.getReference("Storage/Game");
        relaxedRef = database.getReference("Storage/RelaxedState");
        hrRef = database.getReference("HR/heartRate");
        gsrRef = database.getReference("GSR/1-setFloat/Value");
        levelIDRef = database.getReference("Level/currentLvl");
        levelNameRef = database.getReference("Level/levelName");

        Handler handler = new Handler();
        syncUpdateData = new SyncUpdateData(handler, hrRef, gsrRef, myRef, myStorageVideoRef, myStorageGameRef, levelIDRef, levelNameRef, this, arraysFilled, cbGameSessionChecked);
        measureRelaxedState = new MeasureRelaxedState(handler, hrRef, gsrRef, relaxedRef, this, arraysFilled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Povolenie udelené
                } else {
                    // Povolenie zamietnuté, spracujte prípad
                    finish();
                }
        }
    }

    // Prevedie časovú pečiatku do ľudsky čitateľného formátu dátumu a času
    public String convertTimestampToDateTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return formatter.format(date);
    }

    // Pridá nové skóre dýchania do poľa a udržiava priebežný súčet na výpočet priemeru
    public void addBreathingScore(float newScore) {
        sum -= breathingScores[index];
        breathingScores[index] = newScore;
        sum += newScore;
        index = (index + 1) % breathingScores.length;
    }

    // Vypočíta priemer všetkých výsledkov dýchania uložených v poli
    public float getAverageBreathingScore() {
        return sum / 50;
    }

    // Aktualizuje používateľské rozhranie novými údajmi z objektu User v hlavnom vlákne
    public void updateUserUI(User user) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                String dateTime = convertTimestampToDateTime(user.getTimestamp());

                tvTimestampValue.setText("Time: " + dateTime);
                tvHeartRateValue.setText("HR:  " + user.getHeartRate());
                tvGSRValue.setText("GSR: " + user.getGsr());
            }
        });
    }

    // Spustí počúvanie zvukových údajov, spracuje ich a vypočíta frekvenciu a intenzitu dýchania
    protected void startListening(View view) {

        arraysFilled.value = false; // Resetovanie príznaku označujúceho, či sú dátové polia naplnené
        int[] counter = new int[]{0}; // Počítadlo na sledovanie počtu spracovaných zvukových vzoriek

        // Inicializácia klasifikácie zvuku
        try {
            audioClassifier = AudioClassifier.createFromFile(this, "yamnet.tflite");
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Could not load yamnet.tflite file", Toast.LENGTH_SHORT);
        }

        tensorAudio = audioClassifier.createInputTensorAudio();

        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();

        List<Category> finalOutput = new ArrayList<>();

        // Úloha časovača na opakované spracovanie zvuku
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Spracovanie ukončenia zberu údajov
                if (counter[0] == 150) {
                    counter[0] = 151;
                    arraysFilled.value = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "The arrays have been filled, started sending the data.", Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (counter[0] < 150) {
                    counter[0] += 1;
                    Log.d("Debug", String.valueOf(counter[0]));
                }

                // Načítanie zvukových údajov a klasifikácia
                int numberOfSamples = tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        if (category.getScore() > 0.2f && category.getIndex() == 36) {
                            finalOutput.add(category);
                        }
                    }
                }

                // Kontrola prítomnosti dýchania a výpočet miery dýchania
                boolean isBreathing = !finalOutput.isEmpty();
                if (wasBreathing != isBreathing) {
                    breathCount[currentIndex] = 0.25f; // Zaznamenávame čiastočný nádych
                } else {
                    breathCount[currentIndex] = 0.0f;
                }
                wasBreathing = isBreathing; // Stav aktualizácie

                currentIndex = (currentIndex + 1) % breathCount.length;

                float sum = 0;
                for (float val : breathCount) {
                    sum += val;
                }
                respirationRate = (sum * 4); // Prevod z polovičných dychov na plné dychy za minútu


                if (!finalOutput.isEmpty()) {
                    addBreathingScore(finalOutput.get(0).getScore());
                }else{
                    addBreathingScore(0);
                }

                float averageScore = getAverageBreathingScore();
                respirationIntensity = round(averageScore * 1000);


                syncUpdateData.updateRespirationData(respirationRate, respirationIntensity);
                measureRelaxedState.updateRespirationData(respirationRate, respirationIntensity);


                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        tvRespirationIntensityValue.setText("RI: " + respirationIntensity);
                        tvRespirationRateValue.setText("RR: " + respirationRate);
                        finalOutput.clear();
                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1, 150); // Naplánujeme úlohu časovača
    }

    // Zastaví počúvanie zvukových údajov a zastaví záznam zvuku a časovač.
    protected void stopListening(View view) {
        timerTask.cancel();
        audioRecord.stop();
    }
}