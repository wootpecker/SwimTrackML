/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.wear.R
import com.example.wear.presentation.theme.SwimTrackMLTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.Locale
import android.hardware.SensorEventListener

class MainActivity : ComponentActivity(), SensorEventListener {
    private val startTime = System.currentTimeMillis()
    private var heartRateTextView: TextView? = null
    private var stepCounterTextView: TextView? = null
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContentView(R.layout.activity_main)




        heartRateTextView = findViewById(R.id.heart_rate)
        stepCounterTextView = findViewById(R.id.steps)
        sensorManager = getSystemService(SensorManager::class.java)
        if (permissionsGranted()) {
            startSensors()
        } else {
            Log.d(MainActivity.TAG, "Requesting permissions")
            ActivityCompat.requestPermissions(this, MainActivity.permissions, 0)
        }

    }

    protected override fun onResume() {
        super.onResume()

        // If we already have all the permissions start immediately, otherwise request permissions
        if (permissionsGranted()) {
            startSensors()
        } else {
            Log.d(MainActivity.TAG, "Requesting permissions")
            ActivityCompat.requestPermissions(this, MainActivity.permissions, 0)
        }
    }

    private fun permissionsGranted(): Boolean {
        var result = true
        for (permission in MainActivity.permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                result = false
            }
        }
        return result
    }


    private fun startSensors() {
        if (sensorManager != null) {
            // Start heart rate sensor
            val heartRateSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            sensorManager!!.registerListener(
                this,
                heartRateSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            // Start step counter
            val stepCounter = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            sensorManager!!.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Sensors registered")
        } else {
            Log.e(TAG, "SensorManager is null")
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        // Check if a value is attached, if not we can ignore it
        if (sensorEvent.values.size > 0) {
            val value = sensorEvent.values[0]
            if (sensorEvent.sensor.type == Sensor.TYPE_HEART_RATE) {
                Log.d(
                    TAG,
                    "Received new heart rate value: $value"
                )
                //heartRateTextView?.setText(getString(R.string.heart_rate_text, value.toInt()))
                heartRateTextView?.setText(value.toInt())
                // Add a new line to file where the heart rate is our value and the step count is empty
                writeSensorDataToFile(String.format(Locale.GERMAN, "%.0f", value), "")
            } else if (sensorEvent.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                Log.d(
                    TAG,
                    "Received new step counter value: $value"
                )
                stepCounterTextView?.setText(value.toInt())

                // Add a new line to file where the step count is our value and the heart rate is empty
                writeSensorDataToFile("", String.format(Locale.GERMAN, "%.0f", value))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        // Not needed
    }

    /**
     * Writes the sensor data to file. Every call to this method appends a new line in the file.
     *
     * @param heartRate The heart rate, or null if not applicable
     * @param steps     The number of steps, or null if not applicable
     */
    @Synchronized
    private fun writeSensorDataToFile(heartRate: String, steps: String) {
        val directory = Environment.getExternalStorageDirectory()
        val file = File(directory, "sensordata.csv")

        // Calculate the time from app start until now in seconds
        val time = (System.currentTimeMillis() - startTime) / 1000.0f
        try {
            // Write to file
            val fileOutputStream = FileOutputStream(file, true)
            val writer = OutputStreamWriter(fileOutputStream)
            writer.append(String.format(Locale.GERMAN, "%.2f;%s;%s\n", time, heartRate, steps))
            writer.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = MainTestActivity::class.simpleName
        private val permissions =
            arrayOf(Manifest.permission.BODY_SENSORS, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


}











@Composable
fun WearApp(greetingName: String) {
    SwimTrackMLTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}