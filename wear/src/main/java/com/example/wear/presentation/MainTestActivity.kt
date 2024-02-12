package com.example.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.wear.ambient.AmbientLifecycleObserver
import com.example.wear.R

class MainTestActivity : ComponentActivity(), SensorEventListener {
    private val startTime = System.currentTimeMillis()
    private var heartRateTextView: TextView? = null
    private var stepCounterTextView: TextView? = null
    private var sensorManager: SensorManager? = null



   // private val callbacks = object : AmbientLifecycleObserver.AmbientLifecycleCallback {}
   // private val ambientObserver = AmbientLifecycleObserver(this, callbacks)





    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the UI text elements
        heartRateTextView = findViewById(R.id.heart_rate)
        stepCounterTextView = findViewById(R.id.steps)
        sensorManager = getSystemService(SensorManager::class.java)

        // Enables Always-on
        //setAmbientEnabled()
    //    lifecycle.addObserver(ambientObserver)

    }

    protected override fun onResume() {
        super.onResume()

        // If we already have all the permissions start immediately, otherwise request permissions
        if (permissionsGranted()) {
            startSensors()
        } else {
            Log.d(TAG, "Requesting permissions")
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    /**
     * Checks if all necessary permissions have been granted
     *
     * @return True if all necessary permissions have been granted, false otherwise
     */
    private fun permissionsGranted(): Boolean {
        var result = true
        for (permission in permissions) {
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

    protected override fun onPause() {
        // Unregister our listener to prevent leaks
        sensorManager!!.unregisterListener(this)
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        requestedPermissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, requestedPermissions, grantResults)
        if (requestCode == 0) {
            if (requestedPermissions.size != permissions.size) {
                Log.e(
                    TAG,
                    "Number of permission results does not match expected number"
                )
                return
            }

            // Check if all permissions have been granted
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            Log.d(TAG, "Permissions granted")

            // Now we can actually start
            startSensors()
        }
    }

    /**
     * Starts gathering sensor data
     */
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
            Log.d(MainTestActivity.Companion.TAG, "Sensors registered")
        } else {
            Log.e(MainTestActivity.Companion.TAG, "SensorManager is null")
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
                    MainTestActivity.Companion.TAG,
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