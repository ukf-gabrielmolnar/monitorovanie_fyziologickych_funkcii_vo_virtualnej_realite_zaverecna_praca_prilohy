package com.example.firebasebreathing_java;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.MutableBoolean;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.firebasebreathing_java.models.RelaxedState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MeasureRelaxedState implements Runnable {

    // Premenné na správu odkazov na databázu Firebase a aktualizácie používateľského rozhrania
    private final Handler handler;
    private final DatabaseReference hrRef, gsrRef, relaxedRef;
    private final Context context; // Kontext na povolenie aktualizácií používateľského rozhrania a prípitkov
    private boolean isRunning; // Príznak na správu stavu procesu merania

    // Premenné na ukladanie údajov v reálnom čase a štatistických údajov
    private Float heartRateValue, gsrValue, respirationIntensityValue, respirationRateValue,
            respirationRate_avg, respirationRate_max, respirationRate_min,
            respirationIntensity_avg, respirationIntensity_max, respirationIntensity_min,
            heartRate_avg, heartRate_max, heartRate_min,
            gsr_avg, gsr_max, gsr_min,
            temperature, humidity;

    // Identifikácia používateľa a relácie
    private int userID, userSessionNr;

    // Čas merania a počítadlá na správu trvania merania
    private final int measuringTime = 120000; // Celkový čas merania v milisekundách
    private int timeLeft = measuringTime / 1000; // Zostávajúci čas v sekundách

    // Zoznamy na uchovávanie viacerých údajov na štatistické výpočty
    private final ArrayList<Float> heartRateValues = new ArrayList<>();
    private final ArrayList<Float> gsrValues = new ArrayList<>();
    private final ArrayList<Float> respirationRateValues = new ArrayList<>();
    private final ArrayList<Float> respirationIntensityValues = new ArrayList<>();
    private long startTime; // Čas začiatku merania
    MutableBoolean arraysFilled; // Príznak na kontrolu, či boli dátové polia naplnené

    // Konštruktor na inicializáciu všetkých potrebných premenných a odkazov
    public MeasureRelaxedState(Handler handler, DatabaseReference hrRef, DatabaseReference gsrRef, DatabaseReference relaxedRef, Context context, MutableBoolean arraysFilled) {
        this.handler = handler;
        this.hrRef = hrRef;
        this.gsrRef = gsrRef;
        this.relaxedRef = relaxedRef;
        this.context = context;
        this.arraysFilled = arraysFilled;
        this.isRunning = false;
    }

    // Spôsob spustenia procesu merania
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(this);
        }
    }

    // Metóda na zastavenie procesu merania
    public void stop() {
        if (isRunning) {
            isRunning = false;
            timeLeft = measuringTime / 1000; // Resetovanie časovača
            startTime = 0; // Obnovenie času spustenia
            handler.removeCallbacks(this);
        }
    }

    // Kontrola, či meranie práve prebieha
    public boolean isRunning() {
        return isRunning;
    }

    // Nastavenie údajov používateľa a podmienok prostredia pre meranie
    public void setData(int userID, int userSessionNr, float temperature, float humidity) {
        this.userID = userID;
        this.userSessionNr = userSessionNr;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    // Aktualizácia údajov o dýchaní prijatých z externých snímačov
    public void updateRespirationData(float rate, int intensity) {
        this.respirationRateValue = rate;
        this.respirationIntensityValue = (float) intensity;
    }

    // Hlavná metóda, ktorá sa stará o pravidelné získavanie údajov a výpočet štatistík
    @SuppressLint("SetTextI18n")
    @Override
    public void run() {
        if (!isRunning) {
            return; // Zastaviť spustenie, ak by nemalo byť spustené
        }

        // Podmienka na zabezpečenie naplnenia dátových polí pred spracovaním
        if (arraysFilled.value) {

            timeLeft -= 1; // Zníženie zostávajúceho času každú sekundu

            String uID = relaxedRef.push().getKey(); // Generovanie jedinečného ID pre Firebase
            long currentTime = System.currentTimeMillis();

            if (startTime == 0) {
                startTime = currentTime; // Nastavenie času začiatku merania
            }

            if (currentTime - startTime < measuringTime) {

                // Pridávanie údajov v reálnom čase do zoznamov na neskoršiu štatistickú analýzu
                hrRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String heartRateStr = Objects.requireNonNull(snapshot.getValue()).toString();
                            heartRateValue = Float.parseFloat(heartRateStr);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "HeartRate Value Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
                gsrRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String gsrStr = Objects.requireNonNull(snapshot.getValue()).toString();
                            gsrValue = Float.parseFloat(gsrStr);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "GSR Value Not Found", Toast.LENGTH_SHORT).show();
                    }
                });

                if (heartRateValue != null) {
                    heartRateValues.add(heartRateValue);
                }
                if (gsrValue != null) {
                    gsrValues.add(gsrValue);
                }
                respirationRateValues.add(respirationRateValue);
                respirationIntensityValues.add(respirationIntensityValue);

                // Aktualizácia používateľského rozhrania so zostávajúcim časom
                ((Activity) context).runOnUiThread(() -> {
                    Button measureRelaxedStateButton = ((Activity) context).findViewById(R.id.measureRelaxedStateButton);
                    measureRelaxedStateButton.setText("Stop Measuring - Time left: " + timeLeft + "s");
                });

            } else {
                // Po minúte vypočíta a zobrazí štatistiky
                displayStatistics(heartRateValues, "heartRate");
                displayStatistics(gsrValues, "gsr");
                displayStatistics(respirationRateValues, "respirationRate");
                displayStatistics(respirationIntensityValues, "respirationIntensity");

                // Reset pre ďalší zber údajov
                heartRateValues.clear();
                gsrValues.clear();
                respirationRateValues.clear();
                respirationIntensityValues.clear();
                startTime = 0;
                isRunning = false; // Zastavenie behu
                arraysFilled.value = false;

                RelaxedState relaxedState = getRelaxedState(currentTime);

                // Aktualizácia databázy Firebase s údajmi o uvoľnenom stave
                assert uID != null;
                relaxedRef.child(uID).setValue(relaxedState);

                // Aktualizujte používateľské rozhranie, aby odrážalo, že meranie sa zastavilo
                ((Activity) context).runOnUiThread(() -> {
                    Button measureRelaxedStateButton = ((Activity) context).findViewById(R.id.measureRelaxedStateButton);
                    measureRelaxedStateButton.setText("Start Measuring - Relaxed State");
                });
            }
        }

        // Opätovné odoslanie Runnable na pokračovanie spracovania, pokiaľ sa nezastaví
        if (isRunning) {
            handler.postDelayed(this, 1000); // Pokračujte v behu každú sekundu
        }
    }

    // Vytvorenie objektu uvoľneného stavu so všetkými agregovanými údajmi
    @NonNull
    private RelaxedState getRelaxedState(long currentTime) {
        RelaxedState relaxedState = new RelaxedState();
        relaxedState.setUserID(userID);
        relaxedState.setUserSessionNr(userSessionNr);
        relaxedState.setTemperature(temperature);
        relaxedState.setHumidity(humidity);

        // Nastavenie priemerných, minimálnych a maximálnych hodnôt pre všetky sledované parametre
        relaxedState.setHeartRate_avg(heartRate_avg);
        relaxedState.setHeartRate_min(heartRate_min);
        relaxedState.setHeartRate_max(heartRate_max);

        relaxedState.setGsr_avg(gsr_avg);
        relaxedState.setGsr_min(gsr_min);
        relaxedState.setGsr_max(gsr_max);

        relaxedState.setRespirationRate_avg(respirationRate_avg);
        relaxedState.setRespirationRate_min(respirationRate_min);
        relaxedState.setRespirationRate_max(respirationRate_max);

        relaxedState.setRespirationIntensity_avg(respirationIntensity_avg);
        relaxedState.setRespirationIntensity_min(respirationIntensity_min);
        relaxedState.setRespirationIntensity_max(respirationIntensity_max);

        relaxedState.setTimestamp(currentTime);
        return relaxedState;
    }

    // Pomocná metóda na výpočet priemernej hodnoty zoznamu
    private float average(ArrayList<Float> values) {
        float sum = 0;
        for (Float value : values) {
            sum += value;
        }
        return !values.isEmpty() ? sum / values.size() : 0;
    }

    // Vypočíta a zaznamená priemerné, minimálne a maximálne hodnoty pre daný súbor údajov
    private void displayStatistics(ArrayList<Float> values, String valueType) {

        if (!values.isEmpty()) {

            Collections.sort(values);

            float avg = average(values);
            float max = values.get(values.size() - 1);
            float min = values.get(0);

            switch (valueType) {
                case "heartRate":
                    heartRate_avg = avg;
                    heartRate_max = max;
                    heartRate_min = min;
                    break;
                case "gsr":
                    gsr_avg = avg;
                    gsr_max = max;
                    gsr_min = min;
                    break;
                case "respirationRate":
                    respirationRate_avg = avg;
                    respirationRate_max = max;
                    respirationRate_min = min;
                    break;
                case "respirationIntensity":
                    respirationIntensity_avg = avg;
                    respirationIntensity_max = max;
                    respirationIntensity_min = min;
                    break;
            }

            Log.d("Statistics", valueType + " Avg: " + avg + ", Max: " + max + ", Min: " + min);
        }
    }
}