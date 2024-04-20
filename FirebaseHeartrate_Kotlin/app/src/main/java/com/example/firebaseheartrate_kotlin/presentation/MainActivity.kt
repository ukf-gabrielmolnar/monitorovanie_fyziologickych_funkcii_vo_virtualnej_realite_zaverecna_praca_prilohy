/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.firebaseheartrate_kotlin.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.firebaseheartrate_kotlin.R
import com.example.firebaseheartrate_kotlin.presentation.models.HR
import com.example.firebaseheartrate_kotlin.presentation.theme.FirebaseHeartrate_KotlinTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.Manifest
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme.colors


class MainActivity : ComponentActivity() {

    // Inicializácia odkazu Firebase
    private lateinit var myRef: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())

    // Premenné pre správu senzorov
    private val BODY_SENSORS_REQUEST_CODE = 1

    // Stavové premenné merania srdcovej frekvencie
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    // Poslucháč pre zmeny snímača srdcovej frekvencie
    private var heartRate by mutableFloatStateOf(0.0f)
    private var isMonitoring by mutableStateOf(false)
    private val heartRateEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                heartRate = event.values[0]
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    // Správa napájania na udržanie aplikácie aktívnej
    private var wakeLock: PowerManager.WakeLock? = null

    // Spustenie aktualizácie databázy Firebase každú sekundu s najnovšou srdcovou frekvenciou
    private val updateRunnable = object : Runnable {
        override fun run() {
            val tsLong = System.currentTimeMillis()

            val hr = HR()
            hr.timestamp = tsLong
            hr.heartRate = this@MainActivity.heartRate

            if(hr.heartRate != 0.0f) {
                myRef.setValue(hr)
            }

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        setTheme(android.R.style.Theme_DeviceDefault)

        // Zámok prebudenia, aby aplikácia zostala spustená, keď je obrazovka vypnutá
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager // Get the PowerManager service
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyApp::MyWakeLockTag") // Create a wake lock

        // Nastavenie Firebase na ukladanie údajov o srdcovej frekvencii
        myRef = FirebaseDatabase.getInstance().getReference("HR")

        // Povolenie snímačov tela žiadosti, ak ešte nebolo udelené
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
            // Povolenie nie je udelené, požiadajte oň
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                BODY_SENSORS_REQUEST_CODE)
        } else {
            // Povolenie už bolo udelené, spustenie monitorovania srdcovej frekvencie
        }

        // Nastavenie snímača srdcovej frekvencie
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        // Nastavenie obsahu aktivity na použitie rozhrania Compose UI
        setContent {
            FirebaseHeartrate_KotlinTheme {
                // Volanie HeartRateDisplay vo vnútri boxu na zabezpečenie správneho rozloženia
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StartStopButton(
                        isMonitoring = isMonitoring,
                        onStartMonitoring = { startHeartRateMonitoring() },
                        onStopMonitoring = { stopHeartRateMonitoring() }
                    )
                    HeartRateDisplay(heartRate = heartRate)
                }
            }
        }

        // Spustenie periodickej aktualizácie runnable
        handler.post(updateRunnable) // Start the repeating updates
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        when (requestCode) {
            BODY_SENSORS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //
                } else {
                    finish()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // Spustenie monitorovania srdcovej frekvencie a aktualizácia stavu
    private fun startHeartRateMonitoring() {
        wakeLock?.acquire()
        isMonitoring = true
        heartRateSensor?.also { heart ->
            sensorManager.registerListener(heartRateEventListener, heart, SensorManager.SENSOR_DELAY_NORMAL)
        }
        handler.post(updateRunnable)
    }

    // Zastavenie monitorovania srdcovej frekvencie a aktualizácia stavu
    private fun stopHeartRateMonitoring() {
        wakeLock?.release()
        isMonitoring = false
        sensorManager.unregisterListener(heartRateEventListener)
        handler.removeCallbacks(updateRunnable)
    }

    // Metódy životného cyklu na riadenie monitorovania na základe stavu aktivity

    override fun onPause() {
        super.onPause()
        if (isMonitoring) {
            stopHeartRateMonitoring()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMonitoring) {
            startHeartRateMonitoring()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMonitoring) {
            stopHeartRateMonitoring()
        }
        wakeLock?.release()
    }
}

// Kompozitívne pre tlačidlo štart/stop
@Composable
fun StartStopButton(
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
){
    val buttonColor = if (isMonitoring) colors.error else colors.primaryVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp)
    ) {
        Button(
            onClick = {
                if (isMonitoring) {
                    onStopMonitoring()
                } else {
                    onStartMonitoring()
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor)
        ) {
            Text(text = if (isMonitoring) "Stop" else "Start")
        }
    }
}

// Kompozícia na zobrazenie aktuálnej srdcovej frekvencie
@Composable
fun HeartRateDisplay(heartRate: Float?) {
    Text(
        text = heartRate?.toString() ?: "no measure",
        style = MaterialTheme.typography.body1
    )
}