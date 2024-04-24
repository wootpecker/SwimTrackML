package com.example.swimtrackml

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock.elapsedRealtimeNanos
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import java.io.Serializable
import kotlin.math.roundToInt


class FeetoMeter : Activity(), SensorEventListener {
    private var measuring = false
    private val startTime33 = System.currentTimeMillis()
    private var startTime = elapsedRealtimeNanos()
    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var mMagnet: Sensor? = null
    private var mStepCount: Sensor? = null
    private var mStepDetect: Sensor? = null
    private var counter :Int=1000
    private val NAMEN = mutableListOf<String>()
    private val WERTE = mutableListOf<Float>()
    private val SENSOREN = mutableListOf<Sensor?>()

    private val urigeller = Intent()
    private data class Acc(val time:Long,val values:FloatArray,val id:Int)
    private val AccListe2 = mutableListOf<Acc>()
    private val SensorenListe = mutableListOf<MutableList<Serializable>>()
    val CREATE_FILE = 1
    val KURZ = 500


    var timerTextView: TextView? = null
    var startTimer: Long = 0
    var timerHandler: Handler = Handler()
    var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTimer
            var seconds = (millis / 1000).toInt()
            timerTextView?.setText(String.format("%d:%02d", seconds, millis%1000))
            timerHandler.postDelayed(this, 100)
        }
    }


    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Sensoren festlegen, alle Eigenschaften
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)

        //Werte der Sensoren anzeigen
        setContentView(R.layout.gather_walking_data)
        // Sensoren auf ihren Type setzen
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mStepCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        mStepDetect = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        SENSOREN.addAll(
            mutableListOf(
                mAccelerometer,
                mGyroscope,
                mMagnet,
                mStepCount,
                mStepDetect
            )
        )

        timerTextView = findViewById(R.id.txt_time_measuring);
        val button = findViewById<ImageButton>(R.id.btn_start_measuring)

        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(!measuring){
                    measuring = true
                    SensorenListe.clear()
                    startTimer = System.currentTimeMillis();
                    startTime = elapsedRealtimeNanos()
                    timerHandler.postDelayed(timerRunnable, 0);
                    for (alleSensoren in SENSOREN) {
                        alleSensoren.also { sensor ->
                            mSensorManager.registerListener(this@FeetoMeter, sensor, SensorManager.SENSOR_DELAY_GAME)//SensorManager.SENSOR_DELAY_NORMAL)
                          //  mSensorManager.registerListener(this,sensor)
                        }
                    }
                    button.setImageResource(android.R.drawable.ic_delete)
                   // button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, android.R.drawable.ic_delete);
                }
                else{
                    mSensorManager.unregisterListener(this@FeetoMeter)
                    measuring=false
                    button.setImageResource(android.R.drawable.ic_media_play)
                    timerHandler.removeCallbacks(timerRunnable);
                    val intention = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/csv"
                        putExtra(Intent.EXTRA_TITLE, "participant00.csv")
                    }
                    startActivityForResult(intention, CREATE_FILE)

                }

            }
        })



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri: Uri? = data?.data
        //data?.setData(data?.data)
        //urigeller.setData(data?.data)

        if (resultCode == RESULT_OK && requestCode == CREATE_FILE) {
            try {

                savefile(uri)
            } catch (e: Exception) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.gather_data_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_sensor_data -> {
                //Action create pdf
                val intention = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "participant00.csv")
                }
                startActivityForResult(intention, CREATE_FILE)
            }
            R.id.load_sensor_data-> {
            //    val intent = Intent()
           //     intent.setAction(Intent.ACTION_OPEN_DOCUMENT)
            //    intent.setType("text/csv")
            //    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //    startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }






    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        addSensor(event.timestamp,event.sensor,event.values)
    }
    fun addSensor(time_arrived: Long, sensor: Sensor, values: FloatArray) {
        var id=0
        when (sensor) {
            mAccelerometer -> id = 0
            mGyroscope -> id = 1
            mMagnet -> id = 2
            mStepCount -> id = 3
            mStepDetect ->  id =4

            else -> {
                Toast.makeText(baseContext, "Else-case = kaputt", Toast.LENGTH_LONG).show();
                id=100
            }
        }
        val timePassed = ((time_arrived - startTime) / 1000000.0f).roundToInt()
        val liste = mutableListOf<Serializable>(id, time_arrived, timePassed, values[0])
        if(values.size>1) {
            liste.add(values[1])
            liste.add(values[2])
        }
        SensorenListe.add(liste)

        if(SensorenListe.size>counter){
            Toast.makeText(baseContext,"Messwerte: $counter",Toast.LENGTH_SHORT).show()
            counter += 1000
        }

    }








    fun savefile(sourceuri: Uri?) {
        val outputStreamWriter = contentResolver.openOutputStream(sourceuri!!)
        try {
            val values = sensorToString()
            val buf = values.toByteArray()
            outputStreamWriter?.write(buf)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStreamWriter?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }




    }

    fun sensorToString():String{
        val total = StringBuilder()
        //(0-acc|1-gyro|2-mag|3-count|4-detect)
        var zaehler=1
        total.append(",id,time,timedifference,value0,value1,value2",'\n')
        SensorenListe.forEach{ it-> total.append(zaehler++)
                                it.forEach{ it->total.append(",",it)}
                                total.append('\n')}
        return total.toString()
    }

    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }


}



