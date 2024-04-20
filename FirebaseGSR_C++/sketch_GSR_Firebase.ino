// Zahrnúť potrebné knižnice na komunikáciu a spracovanie údajov zo senzorov
#include <SPI.h>
#include <Wire.h>
#include <Arduino_LSM6DS3.h>
#include <Firebase_Arduino_WiFiNINA.h>
#include "arduino_secrets.h"

// Definujte poverenia Firebase a WiFi zo súboru secrets
#define FIREBASE_HOST SECRET_FIREBASE_HOST
#define FIREBASE_AUTH SECRET_FIREBASE_AUTH
#define WIFI_SSID SECRET_WIFI_SSID
#define WIFI_PASSWORD SECRET_WIFI_PASSWORD

// Konštanty na spracovanie dátového poľa senzora
#define MAX_READINGS 20
  
FirebaseData firebaseData;

String path = "/GSR";

int timer = 0;
int sensorReadings[MAX_READINGS]; // Pole na ukladanie údajov zo senzorov
int readingIndex = 0;             // Index pre aktuálny údaj snímača v poli
float sum = 0;                    // Súčet všetkých nameraných hodnôt na spriemerovanie
float average = 0;                // Priemer nameraných hodnôt

const int GSR=A0;                 // Číslo kolíka pre snímač GSR
int sensorValue;                  // Premenná na uloženie hodnoty senzora

void setup(){
  Serial.begin(9600);

  Serial.print("Connecting to WiFi...");

  int status = WL_IDLE_STATUS;

  // Pokúste sa pripojiť k sieti WiFi
  while (status != WL_CONNECTED) {
    status = WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print(".");
    delay(300);
  }

  // Inicializácia Firebase s povereniami a nastaveniami WiFi
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH, WIFI_SSID, WIFI_PASSWORD);
  Firebase.reconnectWiFi(true);

  // Inicializácia poľa údajov snímačov
  for (int i = 0; i < MAX_READINGS; i++) {
    sensorReadings[i] = 0;
  }
}

void loop(){
  // Odpočítajte najstaršie čítanie, keď opustí vyrovnávaciu pamäť
  sum -= sensorReadings[readingIndex];

  // Čítanie aktuálnej hodnoty zo senzora GSR
  sensorValue = analogRead(GSR);
  sensorReadings[readingIndex] = sensorValue;
  
  // Pridajte nové čítanie k súčtu
  sum += sensorValue;

  // Vypočítajte priemer nameraných hodnôt
  average = sum / MAX_READINGS;

  // Presun na ďalší index vo vyrovnávacej pamäti s použitím modulo
  readingIndex = (readingIndex + 1) % MAX_READINGS;

  // Vypíšte všetky údaje zo senzorov na účely ladenia
  for (int i = 0; i < MAX_READINGS; i++) {
    Serial.print(sensorReadings[i]);
    if (i < MAX_READINGS - 1) {
      Serial.print(", ");
    }
  }

  // Skontrolujte, či v bufferi nezostali nuly, čo znamená, že buffer nie je kompletný
  bool hasZero = false;
  for (int i = 0; i < MAX_READINGS; i++) {
    if (sensorReadings[i] == 0) {
      hasZero = true;
      break;
    }
  }

  // Aktualizácia databázy Firebase každých 3 cyklov (300 ms), ale len ak je vyrovnávacia pamäť plná (bez núl)
  timer = timer + 1;
  if (timer == 3){

    // Nastavenie priemernej hodnoty v databáze Firebase pod zadanou cestou
    Firebase.setFloat(firebaseData, path + "/1-setFloat/Value", average);
    // Aktualizácia časovej značky v databáze Firebase pre posledný dátový bod
    Firebase.setTimestamp(firebaseData, path + "/1-setFloat/Time");

    // Resetovanie časovača
    timer = 0;
  }

  // Počkajte 100 ms pred ďalším cyklom
  delay(100);
}