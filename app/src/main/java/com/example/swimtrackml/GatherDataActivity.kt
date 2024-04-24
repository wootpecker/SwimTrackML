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
import android.os.Environment
import android.os.SystemClock.elapsedRealtimeNanos
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import java.io.BufferedReader

import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Serializable
import kotlin.math.roundToInt


class GatherDataActivity : Activity(), SensorEventListener {
    private val startTime = System.currentTimeMillis()
    private val startTime2 = elapsedRealtimeNanos()
    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var mMagnet: Sensor? = null
    private var mPressure: Sensor? = null
    private var mLight: Sensor? = null
    private var counter :Int=100
    private val NAMEN = mutableListOf<String>()
    private val WERTE = mutableListOf<Float>()
    private val SENSOREN = mutableListOf<Sensor?>()

    private val urigeller = Intent()
    private data class Acc(val time:Long,val values:FloatArray,val id:Int)
    private val AccListe2 = mutableListOf<Acc>()
    private val SensorenListe = mutableListOf<MutableList<Serializable>>()
    val CREATE_FILE = 1
    val KURZ = 500
    val LANGE = 1000
    // 3-axis accelerometer, 3-axis gyroscope, light sensor,, pressure
    //        ax[0].plot(t, df['ACC_0'].values)
    //        ax[0].plot(t, df['ACC_1'].values)
    //        ax[0].plot(t, df['ACC_2'].values)
    //        ax[1].plot(t, df['GYRO_0'].values)
    //        ax[1].plot(t, df['GYRO_1'].values)
    //        ax[1].plot(t, df['GYRO_2'].values)
    //        ax[2].plot(t, df['MAG_0'].values)
    //        ax[2].plot(t, df['MAG_0'].values)
    //        ax[2].plot(t, df['MAG_0'].values)
    //        ax[3].plot(t, df['PRESS'].values)
    //        ax[4].plot(t, df['LIGHT'].values)

    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Sensoren festlegen, alle Eigenschaften
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = mSensorManager.getSensorList(Sensor.TYPE_ALL)

        //Sensoren anzeigen im Layout
        setContentView(R.layout.sensoren_liste)
        for (sensor in deviceSensors) {
            Log.d(TAG, "Sensoren: $sensor")
            NAMEN.add(NAMEN.size, sensor.toString())
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, NAMEN)
        val listView: ListView = findViewById(R.id.sensor_liste)
        listView.adapter = adapter
        val test = Acc(1,FloatArray( 2),0)
        val tester = test.toString()
        //Werte der Sensoren anzeigen
        setContentView(R.layout.gather_data)
        val listViewOther: ListView = findViewById(R.id.start_liste)
        //Werte als Adapter
        val adapterOther = ArrayAdapter(this, android.R.layout.simple_list_item_1, WERTE)
        // Sensoren als Adapter
        //val adapterOther = ArrayAdapter(this, android.R.layout.simple_list_item_1, SENSOREN)
        listViewOther.adapter = adapterOther
        repeat(11){WERTE.add(0, 0.0F)}
        // Sensoren auf ihren Type setzen
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        SENSOREN.addAll(
            mutableListOf(
                mAccelerometer,
                mGyroscope,
                mMagnet,
                mPressure,
                mLight
            )
        )
        val button = findViewById<Button>(R.id.starter)

        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                //val showpdf = Intent(baseContext, MySensorActivity::class.java)
                //startActivity(showpdf)
                //  sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                //  val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
                //   Toast.makeText(baseContext, "deviceSensors = $deviceSensors", Toast.LENGTH_LONG).show();
                //try {
                //    CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
                //    String[] nextLine;
                //    while ((nextLine = reader.readNext()) != null) {
                //        // nextLine[] is an array of values from the line
                //        System.out.println(nextLine[0] + nextLine[1] + "etc...");
                //    }
                //} catch (IOException e) {
                //
                //}
                val intent = Intent()
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT)
                intent.setType("text/csv")
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)

            }
        })



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri: Uri? = data?.data
        data?.setData(data?.data)
        urigeller.setData(data?.data)

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
                    //type = "application/excel"
                    //type = "text/comma-seperated-value"
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "sensoren.csv")

                    // Optionally, specify a URI for the directory that should be opened in
                    // the system file picker before your app creates the document.
                    //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
                startActivityForResult(intention, CREATE_FILE)
            }
            R.id.load_sensor_data-> {
                val intent = Intent()
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT)
                intent.setType("text/csv")
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
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

        // SensorDaten in Werte in der Listview anzeigen
        val adapterWerte = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, WERTE)
        val listViewOther: ListView = findViewById(R.id.start_liste)
        adapterWerte.notifyDataSetChanged()
        listViewOther.adapter = adapterWerte
    }
    fun addSensor(time_arrived: Long, sensor: Sensor, values: FloatArray) {

        var count = 0
        var id=0
        when (sensor) {
            mAccelerometer -> repeat(3) {
                WERTE[count] = values[count++]
                id = 0
            }

            mGyroscope -> repeat(3) {
                WERTE[count + 3] = values[count++]
                id = 1
            }

            mMagnet -> repeat(3) {
                WERTE[count + 6] = values[count++]
                id = 2
            }

            mPressure -> {
                WERTE[9] = values[0]
                id = 3
            }

            mLight ->  {
                WERTE[10] =values[0]
                id =4
            }

            else -> {
                Toast.makeText(baseContext, "Else-case = kaputt", Toast.LENGTH_LONG).show();
                id=100
            }
        }
       // if(id==4) {

            val accToAdd = Acc(time_arrived, values, id)
            val str = java.lang.String.format("%.2f", (time_arrived-startTime2)/ 1000000.0f)
            val temp = ((time_arrived - startTime2) / 1000000.0f).roundToInt()
            val liste = mutableListOf<Serializable>(id, time_arrived, temp, values[0])//(time_arrived - startTime) / 1000.0f
            if(values.size>1) {
                liste.add(values[1])
                liste.add(values[2])
            }
            //total.append("id,time,timedifference,value0,value1,value2",'\n')
            AccListe2.add(accToAdd)
            SensorenListe.add(liste)
      //  }
        if(SensorenListe.size>counter){
            Toast.makeText(baseContext,"$counter",Toast.LENGTH_SHORT).show()
            counter += 100
            if(counter%KURZ==0){
                saveDaten()
            }
        }

    }
    override fun onResume() {
        super.onResume()
        for (alleSensoren in SENSOREN) {
            alleSensoren.also { sensor ->
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        //mLight?.also { light ->  sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }








    fun savefile(sourceuri: Uri?) {
        val outputStreamWriter = contentResolver.openOutputStream(sourceuri!!)
        val outputStreamWriter2 = OutputStreamWriter(baseContext.openFileOutput("sensordaten.csv", MODE_PRIVATE))
        val inputStreamWriter = InputStreamReader(baseContext.openFileInput("sensordaten.csv"))


        val sourceFilename = sourceuri?.path
        val destinationFilename =
            Environment.getExternalStorageDirectory().path + File.separatorChar + "sensordaten.csv"
        var bis = BufferedReader(inputStreamWriter)

        var bos = outputStreamWriter
        try {

            val values = sensorToString()
            val buf = values.toByteArray()
            if (bos != null) {
                bos.write(buf)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bis?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }




    }

    fun sensorToString():String{
        val total2 = StringBuilder()
        //(0-acc|1-gyro|2-mag|3-lig|4-pre)
        total2.append("id,time,timedifference,value0,value1,value2",'\n')
        AccListe2.forEach{it->if(it.values.size<2){ total2.append(it.id,",",it.time,",",(it.time-startTime)/1000.0f,",",it.values[0]).append('\n')}
        else{
            total2.append(it.id,",",it.time,",",(it.time-startTime)/1000.0f,",",it.values[0],",",it.values[1],",",it.values[2]).append('\n')}
        }


        val total = StringBuilder()
        //(0-acc|1-gyro|2-mag|3-lig|4-pre)
        var zaehler=1
        total.append(",id,time,timedifference,value0,value1,value2",'\n')
        SensorenListe.forEach{ it-> total.append(zaehler++)
                                it.forEach{ it->total.append(",",it)}
                                total.append('\n')}//if(it.size<2){ total.append(it.id,",",it.time,",",(it.time-startTime)/1000.0f,",",it.values[0]).append('\n')}

        return total.toString()
    }

    fun sensorResetten(){
        SensorenListe.clear()
    }

    fun saveDaten() {
        val values = sensorToString()
        writeToFile(values,this)
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sensordaten.csv", MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    private fun appendToFile(data: String, context: Context) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sensordaten.csv", MODE_PRIVATE))
            outputStreamWriter.append(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }


    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }


}
