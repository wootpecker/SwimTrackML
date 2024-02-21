package com.example.swimtrackml


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView


class MySensorActivity : Activity() {
    private lateinit var sensorManager: SensorManager

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Sensoren festlegen, alle Eigenschaften
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        //Sensoren anzeigen im Layout
        setContentView(R.layout.sensoren_liste)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceSensors)
        val listView: ListView = findViewById(R.id.sensor_liste)
        listView.adapter = adapter
    }

}