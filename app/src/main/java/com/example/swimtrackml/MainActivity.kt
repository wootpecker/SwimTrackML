package com.example.swimtrackml


import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.swimtrackml.ui.theme.SwimTrackMLTheme


class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_database)
        val button = findViewById<Button>(R.id.start_db)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val sensorenActivity = Intent(baseContext, MySensorActivity::class.java)
                val concreteML = Intent(baseContext, concreteML::class.java)
                val concreteMLJava = Intent(baseContext, testml::class.java)
                startActivity(concreteMLJava)
                //startActivity(Intent(baseContext, MySensorActivity::class.java))
              //  sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
              //  val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
             //   Toast.makeText(baseContext, "deviceSensors = $deviceSensors", Toast.LENGTH_LONG).show();

            }
        })
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SwimTrackMLTheme {
        Greeting("Android")
    }
}