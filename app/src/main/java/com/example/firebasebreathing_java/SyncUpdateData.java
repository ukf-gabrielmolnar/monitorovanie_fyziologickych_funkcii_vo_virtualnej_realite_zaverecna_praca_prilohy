package com.example.firebasebreathing_java;

import android.content.Context;
import android.os.Handler;
import android.util.MutableBoolean;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.firebasebreathing_java.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SyncUpdateData implements Runnable {

    // Odkazy na rôzne cesty k databáze Firebase a lokálny kontext a obslužné programy
    private final Handler handler;
    private final DatabaseReference hrRef, gsrRef, myRef, myStorageVideoRef, myStorageGameRef, levelIDRef, levelNameRef;
    private DatabaseReference myStorageRef; // Určí sa na základe typu relácie
    private final Context context; // Používa sa na zobrazovanie správ Toast
    private boolean isRunning; // Sleduje, či je proces synchronizácie aktívny
    private Float heartRateValue, gsrValue, respirationRateValue; // Premenné fyziologických údajov
    private int userID, userSessionNr, levelID, respirationIntensityValue; // Identifikátory používateľa a relácie a metriky
    private String levelName; // Názov aktuálnej úrovne alebo fázy v aplikácii
    MutableBoolean arraysFilled, cbGameSessionChecked; // Príznaky na kontrolu stavov v aplikácii

    // Konštruktor inicializuje triedu s referenciami na databázu a kontext
    public SyncUpdateData(Handler handler, DatabaseReference hrRef, DatabaseReference gsrRef, DatabaseReference myRef, DatabaseReference myStorageVideoRef, DatabaseReference myStorageGameRef, DatabaseReference levelIDRef, DatabaseReference levelNameRef, Context context, MutableBoolean arraysFilled, MutableBoolean cbGameSessionChecked) {
        this.handler = handler;
        this.hrRef = hrRef;
        this.gsrRef = gsrRef;
        this.myRef = myRef;
        this.myStorageVideoRef = myStorageVideoRef;
        this.myStorageGameRef = myStorageGameRef;
        this.levelIDRef = levelIDRef;
        this.levelNameRef = levelNameRef;
        this.context = context;
        this.arraysFilled = arraysFilled;
        this.cbGameSessionChecked = cbGameSessionChecked;
        this.isRunning = false;
    }

    // Spustí proces synchronizácie údajov, ak ešte nie je spustený
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(this);
        }
    }

    // Zastaví proces synchronizácie údajov
    public void stop() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(this);
        }
    }

    // Skontroluje, či synchronizácia práve prebieha
    public boolean isRunning() {
        return isRunning; // Return the status of isRunning
    }

    // Nastaví ID používateľa a relácie pre aktuálnu synchronizačnú operáciu
    public void setUserAndSessionID(int UserID, int userSessionNr) {
        this.userID = UserID;
        this.userSessionNr = userSessionNr;
    }

    // Aktualizuje údaje týkajúce sa dýchania používateľa
    public void updateRespirationData(float rate, int intensity) {
        this.respirationRateValue = rate;
        this.respirationIntensityValue = intensity;
    }

    // Spustiteľná metóda, ktorá vykonáva synchronizáciu údajov so službou Firebase
    @Override
    public void run() {
        if (!isRunning) {
            return; // Ukončiť, ak sa nemá spustiť
        }

        // Určenie odkazu na úložisko na základe toho, či je relácia hry skontrolovaná
        if(cbGameSessionChecked.value){
            myStorageRef = myStorageGameRef;
        }else{
            myStorageRef = myStorageVideoRef;
        }

        // Generovanie jedinečného ID pre nový riadok
        String uID = myStorageRef.push().getKey();
        long tsLong = System.currentTimeMillis();

        // Počúvanie zmien srdcovej frekvencie a aktualizácia lokálne uloženej hodnoty
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
        levelIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String LevelIDStr = Objects.requireNonNull(snapshot.getValue()).toString();
                    levelID = Integer.parseInt(LevelIDStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Level Value Not Found", Toast.LENGTH_SHORT).show();
            }
        });
        levelNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    levelName = Objects.requireNonNull(snapshot.getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Level Name Not Found", Toast.LENGTH_SHORT).show();
            }
        });

        // Vytvorenie a naplnenie objektu User aktualizovanými údajmi
        User user = new User();
        user.setUserID(userID);
        user.setUserSessionNr(userSessionNr);
        user.setHeartRate(heartRateValue);
        user.setGsr(gsrValue);
        user.setBreathingFrequency(respirationRateValue);
        user.setBreathingIntensity(respirationIntensityValue);
        user.setLevelID(levelID);
        user.setLevelName(levelName);
        user.setTimestamp(tsLong);

        // Aktualizácia používateľského rozhrania s novými údajmi
        ((MainActivity) context).updateUserUI(user);

        // Odoslanie údajov o používateľovi do databázy Firebase, ak sú polia naplnené
        if(arraysFilled.value) {
            myRef.setValue(user); // Nastavenie údajov vo všeobecnom odkaze používateľa

            assert uID != null;
            myStorageRef.child(uID).setValue(user); // Nastavenie údajov pod jedinečným ID v príslušnom odkaze úložiska
        }

        // Naplánujte ďalší beh tejto úlohy, ak je stále aktívna
        if (isRunning) {
            handler.postDelayed(this, 1000); // Po sekunde sa opäť spustí
        }
    }
}