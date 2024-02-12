package com.example.swimtrackml


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast


class MySensorActivity : Activity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var mGravity: Sensor? = null
    private var mPressure: Sensor? = null
    private var mLight: Sensor? = null
    private var counter :Int=100
    private val NAMEN = mutableListOf<String>()
    private val WERTE = mutableListOf<Float>()
    private val SENSOREN = mutableListOf<Sensor?>()
    private val SAVEVALUES=mutableListOf<Float>()
    class Acc(time:Long, values:FloatArray)
    private val AccListe = mutableListOf<Acc>()


    private class ListExampleAdapter(context: Context) {}
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
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        //Sensoren anzeigen im Layout
        setContentView(R.layout.sensoren_liste)
        for (sensor in deviceSensors) {
            Log.d(TAG, "Sensoren: $sensor")
            NAMEN.add(NAMEN.size, sensor.toString())
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, NAMEN)
        val listView: ListView = findViewById(R.id.sensor_liste)
        listView.adapter = adapter


        //Werte der Sensoren anzeigen
        setContentView(R.layout.start_other)
        val listViewOther: ListView = findViewById(R.id.start_liste)
        //Werte als Adapter
        val adapterOther = ArrayAdapter(this, android.R.layout.simple_list_item_1, WERTE)
        // Sensoren als Adapter
        //val adapterOther = ArrayAdapter(this, android.R.layout.simple_list_item_1, SENSOREN)
        listViewOther.adapter = adapterOther
        repeat(11){WERTE.add(0, 0.0F)}
        // Sensoren auf ihren Type setzen
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        SENSOREN.addAll(
            mutableListOf<Sensor?>(
                mAccelerometer,
                mGyroscope,
                mGravity,
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

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        //Toast.makeText(baseContext, "deviceSensors = $luxlist", Toast.LENGTH_LONG).show();
        //event.sensor.
        addSensor(event.timestamp,event.sensor,event.values)
        //for(lux in event.values){
        //WERTE.add(lux)
        //}
        //Listview updaten bei Sensor update
        //val listViewOther: ListView = findViewById(R.id.start_liste)

        //listViewOther.adapter = adapterOther
        //Toast.makeText(baseContext, "deviceSensors = $adapterOther", Toast.LENGTH_LONG).show();

        // SensorDaten in Werte in der Listview anzeigen

        val adapterWerte = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, WERTE)
        val listViewOther: ListView = findViewById(R.id.start_liste)
        adapterWerte.notifyDataSetChanged()
        listViewOther.adapter = adapterWerte
    }

    override fun onResume() {
        super.onResume()
        for (alleSensoren in SENSOREN) {
            alleSensoren.also { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        //mLight?.also { light ->  sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }


    fun addSensor(time_arrived: Long, sensor: Sensor, values: FloatArray) {

        var count = 0
        when (sensor) {
            mAccelerometer -> repeat(3) {
                WERTE[count] = values[count++]
            }

            mGyroscope -> repeat(3) {
                WERTE[count+3] = values[count++]
            }

            mGravity -> repeat(3) {
                WERTE[count+6] = values[count++]
            }

            mPressure -> WERTE[9] = values[0]

            mLight -> WERTE[10] = values[0]

            else -> {
                Toast.makeText(baseContext, "Else-case = kaputt", Toast.LENGTH_LONG).show();
            }
        }
        val Acc_to_add = Acc(time_arrived, values)
        AccListe.add(0,Acc_to_add)
        if(AccListe.size>counter){
            Toast.makeText(baseContext,"$counter",Toast.LENGTH_SHORT).show()
            counter += 100
        }

    }

   /* fun saveFile(){
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val fileName = "AnalysisData.csv"
        val filePath = baseDir + File.separator + fileName
        val f = File(filePath)
        val writer: CSVWriter
        var writer = CSVWriter(FileWriter(csv))
        // File exist

        val data: MutableList<Array<String>> = ArrayList()
        data.add(arrayOf("India", "New Delhi"))
        data.add(arrayOf("United States", "Washington D.C"))
        data.add(arrayOf("Germany", "Berlin"))

        if (f.exists() && !f.isDirectory) {
            var mFileWriter = FileWriter(filePath, true)
            writer = CSVWriter(mFileWriter)
        } else {
            writer = CSVWriter(FileWriter(filePath))
        }

        writer.writeNext(data);

        writer.close();


    }
    private fun readData() {
        val `is` = resources.openRawResource(R.raw.welldata)
        val reader = BufferedReader(
            InputStreamReader(`is`, Charset.forName("UTF-8"))
        )
        var line = ""
        try {
            while (reader.readLine().also { line = it } != null) {
                // Split the line into different tokens (using the comma as a separator).
                val tokens = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                // Read the data and store it in the WellData POJO.
                val wellData = WellData()
                wellData.setOwner(tokens[0])
                wellData.setApi(tokens[1])
                wellData.setLongitude(tokens[2])
                wellData.setLatitude(tokens[3])
                wellData.setProperty(tokens[4])
                wellData.setWellName(tokens[5])
                wellDataList.add(wellData)
                Log.d("MainActivity", "Just Created $wellData")
            }
        } catch (e1: IOException) {
            Log.e("MainActivity", "Error$line", e1)
            e1.printStackTrace()
        }
    }*/
}
