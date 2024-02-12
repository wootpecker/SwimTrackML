package com.example.swimtrackml

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class concreteML() : AppCompatActivity() {
    var interpreter: Interpreter? = null
    var etxt_cement: EditText? = null
    var etxt_blastFurnace: EditText? = null
    var etxt_flyAsh: EditText? = null
    var etxt_water: EditText? = null
    var etxt_superplasticizer: EditText? = null
    var etxt_coarseAggregate: EditText? = null
    var etxt_fineAggregate: EditText? = null
    var etxt_age: EditText? = null
    var txt_resultBox: TextView? = null
    var predictButton: Button? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.concrete_main)

        //Arrays for performing MinMaxScaler()
        val min = floatArrayOf(
            102.000000f, 0.000000f, 0.000000f, 121.750000f, 0.000000f, 801.000000f, 594.000000f,
            1.000000f, 2.331808f
        )
        val max = floatArrayOf(
            540.000000f,
            359.400000f,
            200.100000f,
            247.000000f,
            32.200000f,
            1145.000000f,
            992.600000f,
            365.000000f,
            82.599225f
        )

        //Loading the tflite model.
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        etxt_cement = findViewById(R.id.et_cement)
        etxt_blastFurnace = findViewById(R.id.et_BlastFurnace)
        etxt_flyAsh = findViewById(R.id.et_FlyAsh)
        etxt_water = findViewById(R.id.et_Water)
        etxt_superplasticizer = findViewById(R.id.et_Superplasticizer)
        etxt_coarseAggregate = findViewById(R.id.et_CoarseAggregate)
        etxt_fineAggregate = findViewById(R.id.et_FineAggregate)
        etxt_age = findViewById(R.id.et_Age)
        txt_resultBox = findViewById(R.id.txt_results)
        predictButton = findViewById(R.id.btn_predict)
        predictButton!!.setOnClickListener(View.OnClickListener {
            if (etxt_cement?.length() == 0) {
                etxt_cement?.requestFocus()
                etxt_cement?.setError("Please fill this field")
            } else if (etxt_blastFurnace?.length() == 0) {
                etxt_blastFurnace?.requestFocus()
                etxt_blastFurnace?.setError("Please fill this field")
            } else if (etxt_flyAsh?.length() == 0) {
                etxt_flyAsh?.requestFocus()
                etxt_flyAsh?.setError("Please fill this field")
            } else if (etxt_water?.length() == 0) {
                etxt_water?.requestFocus()
                etxt_water?.setError("Please fill this field")
            } else if (etxt_superplasticizer?.length() == 0) {
                etxt_superplasticizer?.requestFocus()
                etxt_superplasticizer?.setError("Please fill this field")
            } else if (etxt_coarseAggregate?.length() == 0) {
                etxt_coarseAggregate?.requestFocus()
                etxt_coarseAggregate?.setError("Please fill this field")
            } else if (etxt_fineAggregate?.length() == 0) {
                etxt_fineAggregate?.requestFocus()
                etxt_fineAggregate?.setError("Please fill this field")
            } else if (etxt_age?.length() == 0) {
                etxt_age?.requestFocus()
                etxt_age?.setError("Please fill this field")
            } else {
                val concreteArray = Array(1) { FloatArray(8) }
                concreteArray[0][0] =
                    ((etxt_cement?.getText().toString().toFloat() - min[0]) / (max[0] - min[0]))
                concreteArray[0][1] = ((etxt_blastFurnace?.getText().toString()
                    .toFloat() - min[1]) / (max[1] - min[1]))
                concreteArray[0][2] =
                    ((etxt_flyAsh?.getText().toString().toFloat() - min[2]) / (max[2] - min[2]))
                concreteArray[0][3] =
                    ((etxt_water?.getText().toString().toFloat() - min[3]) / (max[3] - min[3]))
                concreteArray[0][4] = ((etxt_superplasticizer?.getText().toString()
                    .toFloat() - min[4]) / (max[4] - min[4]))
                concreteArray[0][5] = ((etxt_coarseAggregate?.getText().toString()
                    .toFloat() - min[5]) / (max[5] - min[5]))
                concreteArray[0][6] = ((etxt_fineAggregate?.getText().toString()
                    .toFloat() - min[6]) / (max[6] - min[6]))
                concreteArray[0][7] =
                    ((etxt_age?.getText().toString().toFloat() - min[7]) / (max[7] - min[7]))
                var predictions = doInference(concreteArray)
                predictions = Math.round(predictions).toFloat()
                txt_resultBox!!.text = "Estimated Concrete compressive strength is $predictions MPa"
            }
        })
    }

    fun doInference(input: Array<FloatArray>?): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter?.run(input, output)
        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor: AssetFileDescriptor = this.getAssets().openFd("Concrete.tflite")
        val fileInputStream: FileInputStream =
            FileInputStream(assetFileDescriptor.getFileDescriptor())
        val fileChannel = fileInputStream.channel
        val startOffset: Long = assetFileDescriptor.getStartOffset()
        val length: Long = assetFileDescriptor.getLength()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }
}