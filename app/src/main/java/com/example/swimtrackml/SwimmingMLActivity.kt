package com.example.swimtrackml


import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset


class SwimmingMLActivity() : Activity() {
    var interpreter: Interpreter? = null

    private val MY_MAX_ARRAY_SIZE = 3600
    val floaterArray= Array(MY_MAX_ARRAY_SIZE){ Array(11){ 0F} }
    val floaterArray2= Array(MY_MAX_ARRAY_SIZE){ Array(11){ 0F} }
    val Acc0= mutableListOf<Float>()
    val Acc1= mutableListOf<Float>()
    val Acc2= mutableListOf<Float>()
    val Gyr0= mutableListOf<Float>()
    val Gyr1= mutableListOf<Float>()
    val Gyr2= mutableListOf<Float>()
    val Mag0= mutableListOf<Float>()
    val Mag1= mutableListOf<Float>()
    val Mag2= mutableListOf<Float>()
    val Lux0= mutableListOf<Float>()
    val Pre0= mutableListOf<Float>()
    val time0= mutableListOf<Float>()
    val label0= mutableListOf<Float>()
    val mini = floatArrayOf(102.000000f, 0.000000f, 0.000000f, 121.750000f, 0.000000f, 801.000000f, 594.000000f, 1.000000f, 2.331808f)
    val maxi = floatArrayOf(540.000000f, 359.400000f, 200.100000f, 247.000000f, 32.200000f, 1145.000000f, 992.600000f, 365.000000f, 82.599225f)
    var counter=0
    private val CSV_ARRAY_START = 2

    @OptIn(DelicateCoroutinesApi::class)
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.swim_ml_main)

        //Arrays for performing MinMaxScaler()

        //Loading the tflite model.
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val values_txt=findViewById<EditText>(R.id.txt_swim_samples)
        val predict = findViewById<Button>(R.id.btn_predict)

        predict.setOnClickListener(View.OnClickListener {
            if (values_txt.text.length == 0) {
                //values_txt?.requestFocus()
                //values_txt?.setError("Please fill this field")
                values_txt.setText("0")
            }
            else{
            GlobalScope.launch {
                withContext(Dispatchers.Default){
                  /*  val conditions = CustomModelDownloadConditions.Builder()
                        .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                        .build()
                    FirebaseModelDownloader.getInstance()
                        .getModel("swimtestcase", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                            conditions)
                        .addOnSuccessListener { model: CustomModel? ->
                            val modelFile = model?.file
                            if (modelFile != null) {
                                interpreter = Interpreter(modelFile)
                            }
                        }*/
                    val arrayStartTemp=values_txt.text
                    val temp = "$arrayStartTemp".toInt()
                    val input = ByteBuffer.allocateDirect(1*180*11*4).order(ByteOrder.nativeOrder())
                    for (y in temp until temp+180) {
                        for (x in 0 until 11) {
                            input.putFloat(floaterArray[y][x])
                        }
                    }

                    val bufferSize = 5 * java.lang.Float.SIZE / java.lang.Byte.SIZE
                    val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
                    interpreter?.run(input, modelOutput)

                    modelOutput.rewind()
                    val probabilities = modelOutput.asFloatBuffer()

                    try {
                        //val reader = BufferedReader(InputStreamReader(assets.open("custom_labels.txt")))
                        var max_index=0
                        var max_value=probabilities.get(0)
                        var results = ""
                        for (i in 0..probabilities.capacity()-1) {
                            //val label: String = reader.readLine()
                            val probability = probabilities.get(i)
                            if(probability>max_value){
                                max_value=probability
                                max_index=i
                            }
                            results = "$probability"
                            when(i){
                                0->findViewById<TextView>(R.id.txt_swim_transition).setText(results)
                                1->findViewById<TextView>(R.id.txt_swim_crawl).setText(results)
                                2->findViewById<TextView>(R.id.txt_swim_breaststroke).setText(results)
                                3->findViewById<TextView>(R.id.txt_swim_backstroke).setText(results)
                                4->findViewById<TextView>(R.id.txt_swim_butterfly).setText(results)
                            }
                        }
                        val txt_results = findViewById<TextView>(R.id.txt_results)
                        txt_results.setText("Max value: $max_value \nIndex:$max_index")
                    } catch (e: IOException) {
                        // File not found?
                        Log.d("Swimming ML", "Model Results Error")
                    }
                }
            }
            val etxt_water_item = findViewById<TextView>(R.id.txt_results)
            etxt_water_item.setText("Neue Size"+floaterArray.size.toString())
            }
        })
        val loadDaten = findViewById<Button>(R.id.btn_load_data)
        loadDaten.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.Default) {
                    readDaten()
                    //readDaten2()
                    val txt_results = findViewById<TextView>(R.id.txt_results)
                    txt_results.setText("Neue Size"+floaterArray.size.toString())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.make_ml, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var predicament = Array(1) { FloatArray(8) }
       //val predictArray = Array()
        val inputArray = Array(1){Array(180){Array(11){Array<Float>(1){0F} } } }
        var x = 0
        when(item.itemId){
            R.id.actionSave ->{
                val conditions = CustomModelDownloadConditions.Builder()
                    .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                    .build()
                FirebaseModelDownloader.getInstance()
                    .getModel("swimtestcase", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                        conditions)
                    .addOnSuccessListener { model: CustomModel? ->
                        // Download complete. Depending on your app, you could enable the ML
                        // feature, or switch from the local model to the remote model, etc.

                        // The CustomModel object contains the local path of the model file,
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        val modelFile = model?.file
                        if (modelFile != null) {
                            interpreter = Interpreter(modelFile)
                        }
                    }


                val input = ByteBuffer.allocateDirect(1*180*11*4).order(ByteOrder.nativeOrder())
                for (y in 0 until 180) {
                    for (x in 0 until 11) {
                        input.putFloat(floaterArray[y][x])
                    }
                }

                val bufferSize = 5 * java.lang.Float.SIZE / java.lang.Byte.SIZE
                val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
                interpreter?.run(input, modelOutput)

                modelOutput.rewind()
                val probabilities = modelOutput.asFloatBuffer()
                try {
                    //val reader = BufferedReader(InputStreamReader(assets.open("custom_labels.txt")))
                    var results = ""
                    for (i in 0..probabilities.capacity()-1) {
                        //val label: String = reader.readLine()
                        val probability = probabilities.get(i)
                        results = "$probability"
                        when(i){
                            0->findViewById<TextView>(R.id.txt_swim_transition).setText(results)
                            1->findViewById<TextView>(R.id.txt_swim_crawl).setText(results)
                            2->findViewById<TextView>(R.id.txt_swim_breaststroke).setText(results)
                            3->findViewById<TextView>(R.id.txt_swim_backstroke).setText(results)
                            4->findViewById<TextView>(R.id.txt_swim_butterfly).setText(results)
                        }

                    }
                    Toast.makeText(baseContext, "Ergebnis $results", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    // File not found?
                }



                //var predictions = doInference2(inputArray)
                val resultbox = findViewById<TextView>(R.id.txt_results)
                resultbox.text = "$modelOutput"
            }
            R.id.actionToggleValues ->{

                if(counter==0){
                    predicament = Array(1) { mini }
                    counter++
                }
                else{
                    predicament = Array(1) { maxi }
                    counter=0
                }

                var predictions = doInference(predicament)
                //predictions = Math.round(predictions)//Math.round(predictions).toDouble()
                val resultbox = findViewById<TextView>(R.id.txt_results)
                resultbox.text = "Estimated Concrete compressive strength is $predictions MPa"
            }
        }
        return super.onOptionsItemSelected(item)
    }
    suspend fun readDaten(): Unit = withContext(Dispatchers.IO){
        val `is` = resources.openRawResource(R.raw.swimmingfreestyle)
        val reader = BufferedReader(
            InputStreamReader(`is`, Charset.forName("UTF-8"))
        )


        var count=0
        var line = ""
        try {
            while (reader.readLine().also { line = it } != null) {
                // Split the line into different tokens (using the comma as a separator).
                if(count==0) {
                    count++
                    continue
                }
                val tokens = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if(tokens.isEmpty() or(count==MY_MAX_ARRAY_SIZE+1)) {
                    break
                }

                //val tempList= mutableListOf<Float>()
                for(j in CSV_ARRAY_START..CSV_ARRAY_START+10){
                    //tempList.add(tokens[j].toFloat())
                    val temp = tokens[j].toFloat()
                    val temp2 = count-1
                    val temp3 = j-CSV_ARRAY_START
                    floaterArray[count-1][j-CSV_ARRAY_START]=tokens[j].toFloat()
                }
                count++

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun doInference(input: Array<FloatArray>): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter!!.run(input, output)
        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        //val assetFileDescriptor: AssetFileDescriptor = this.getAssets().openFd("Concrete.tflite")
        val assetFileDescriptor: AssetFileDescriptor = this.getAssets().openFd("swimming.tflite")
        val fileInputStream: FileInputStream =
            FileInputStream(assetFileDescriptor.getFileDescriptor())
        val fileChannel = fileInputStream.channel
        val startOffset: Long = assetFileDescriptor.getStartOffset()
        val length: Long = assetFileDescriptor.getLength()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }
}

