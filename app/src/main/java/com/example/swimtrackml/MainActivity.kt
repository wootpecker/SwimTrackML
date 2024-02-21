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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.swimtrackml.ui.theme.SwimTrackMLTheme


class MainActivity : ComponentActivity() {
    //private lateinit var sensorManager: SensorManager
    //private val buttons = mutableListOf(findViewById(R.id.start_gather_data), findViewById<Button>(R.id.start_show_sensor),findViewById(R.id.start_show_data),findViewById(R.id.start_do_ml_prediction),findViewById(R.id.test_concrete_prediction),findViewById(R.id.test_concrete_java))
    private val buttonsID= mutableListOf(R.id.start_gather_data, R.id.start_show_sensor,R.id.start_show_data,R.id.start_do_ml_prediction,R.id.test_concrete_prediction,R.id.test_concrete_java,R.id.show_line_chart)
    //private val  liste = mutableListOf(GatherDataActivity::class.java,MySensorActivity::class.java,concreteML::class.java,baseContext, testml::class.java)
    private val  liste2 = mutableListOf("GatherDataActivity","MySensorActivity","ShowDataActivity","concreteML","concreteML", "testml")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_main)

        for(i in 0..buttonsID.size-1){
            //buttons[i].setOnClickListener(this)
            findViewById<Button>(buttonsID[i]).setOnClickListener { v ->
                if (v != null) {
                    when (v.id) {
                        buttonsID[0]->startActivity(Intent(baseContext,GatherDataActivity::class.java))
                        buttonsID[1]->startActivity(Intent(baseContext,MySensorActivity::class.java))
                        buttonsID[2]->startActivity(Intent(baseContext,ShowDataActivity::class.java))
                        buttonsID[3]->startActivity(Intent(baseContext,SwimmingMLActivity::class.java))
                        buttonsID[4]->startActivity(Intent(baseContext,concreteML::class.java))
                        buttonsID[5]->startActivity(Intent(baseContext,testml::class.java))
                        buttonsID[6]->startActivity(Intent(baseContext,ShowDataActivityLineChart::class.java))
                    }
                }
            }

        }
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