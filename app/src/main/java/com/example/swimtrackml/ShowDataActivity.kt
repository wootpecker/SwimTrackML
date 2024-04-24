package com.example.swimtrackml

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.swimtrackml.ml.Swim
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.model.GradientColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.logging.Logger
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


class ShowDataActivity : Activity() {
    private var chart: BarChart? = null
    private var chart_input: LineChart? = null
    private var chart_output: LineChart? = null
    private val seekBarX: SeekBar? = null
    private var seekBarY:SeekBar? = null
    private val tvX: TextView? = null
    private var tvY:TextView? = null


    val Log = Logger.getLogger(ShowDataActivity::class.java.name)

    private val CSV_ARRAY_START = 2
    private val TIME_START = 1
    private val LABEL_START = 11
    val maximum = floatArrayOf(Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE)
    val minimum = floatArrayOf(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE)

    private val MY_MAX_ARRAY_SIZE = 18000
    val floaterArray= Array(MY_MAX_ARRAY_SIZE){ Array(11){ 0F} }
    //val values_array= List<Array(11){0f}>MutableList()
    val time_input = Array(MY_MAX_ARRAY_SIZE) { 0F}
    val label_output = Array(MY_MAX_ARRAY_SIZE){ 0}
    val label_ml_output = Array(MY_MAX_ARRAY_SIZE){ 0}
    val durchschnitt = Array(11){0F}
    val rms = Array(11){0F}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.smiw_ml_linechart)
        title = "Swim ML"
        chart_input = findViewById(R.id.chart_input)
        chart_output = findViewById(R.id.chart_output)



        chart_input?.description?.isEnabled = false
        chart_output?.description?.isEnabled = false
        chart_input!!.setBackgroundColor(Color.WHITE)
        chart_output!!.setBackgroundColor(Color.WHITE)
        // if more than 60 entries are displayed in the chart, no values will be drawn
        //chart?.setMaxVisibleValueCount(180)

        chart_input?.setPinchZoom(false)// scaling can now only be done on x- and y-axis separately
        val l: Legend? = chart_input?.legend
        if (l != null) {
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
            l.form = Legend.LegendForm.SQUARE
            l.formSize = 9f
            l.textSize = 11f
            l.xEntrySpace = 4f
        }
        //val set_label_output = LineDataSet(values, "DataSet 1")

        if(floaterArray[0][0]==0F){


            Thread(Runnable {
                readDaten2()
                chart_input!!.data=setDaten_input()
                chart_input!!.notifyDataSetChanged();
                chart_input!!.invalidate();
                chart_output!!.data=setDaten_output()
                chart_output!!.notifyDataSetChanged();
                chart_output!!.invalidate();
            }).start()






       /*     GlobalScope.launch {
                withContext(Dispatchers.Default) {
                    readDaten()
                    chart_input!!.data=setDaten_input()
                    chart_output!!.data=setDaten_output()
                }
            }*/
        }





        //setData(5, 100);

        /*val button=findViewById<Button>(R.id.btn_load_data)
        button.setOnClickListener {
            startActivity(Intent(this, ShowDataSeecondTestActivity::class.java))
        }*/
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setData(count: Int, range: Int) {
        val start = 1f
        val values = ArrayList<BarEntry>()
        var i = start.toInt()
        while (i < start + count) {
            val `val` = (Math.random() * (range + 1)).toFloat()
            if (Math.random() * 100 < 25) {
                values.add(BarEntry(i.toFloat(), `val`, resources.getDrawable(R.drawable.star)))
            } else {
                values.add(BarEntry(i.toFloat(), `val`))
            }
            i++
        }
        val set1: BarDataSet
        if (chart!!.data != null &&
            chart!!.data.dataSetCount > 0
        ) {
            set1 = chart!!.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            chart!!.data.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "The year 2017")
            set1.setDrawIcons(false)
            val startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light)
            val startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light)
            val startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light)
            val endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple)
            val endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark)
            val endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            val endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            val gradientFills: MutableList<GradientColor> = ArrayList()
            gradientFills.add(GradientColor(startColor1, endColor1))
            gradientFills.add(GradientColor(startColor2, endColor2))
            gradientFills.add(GradientColor(startColor3, endColor3))
            gradientFills.add(GradientColor(startColor4, endColor4))
            gradientFills.add(GradientColor(startColor5, endColor5))
            set1.gradientColors = gradientFills
            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.barWidth = 0.9f
            chart!!.data = data
        }
    }


    private fun setDaten_output(): LineData {
        var count = 0
        val values = ArrayList<Entry>()
        while (time_input[count] != 0F) {
            values.add(Entry(round((time_input[count]-time_input[0])/1000000), label_output[count].toFloat()))
            count++
        }

        val values_ml = ArrayList<Entry>()
        var ml_counter = 0
        val nextsample=10
        val windowArray = floaterArray.sliceArray(IntRange(ml_counter,ml_counter+179))
        val durchschnitt = (0 until 11).map { col ->
            windowArray.map { it[col] }.average()
        }

        val rmsArray = (0 until 11).map { col ->
            windowArray.map { (it[col]-durchschnitt[col] ).pow(2)}.average()
        }
        val rms= rmsArray.map{sqrt(it)}


        while(time_input[ml_counter+180]!=0F) {

            val input = ByteBuffer.allocateDirect(1 * 180 * 11 * 4).order(ByteOrder.nativeOrder())
            for (y in ml_counter until ml_counter + 180) {
                for (x in 0 until 11) {
                    if (x < 20) {//9
                        input.putFloat(((floaterArray[y][x] - durchschnitt[x]) / (rms[x])).toFloat())
                    } else {
                        input.putFloat(floaterArray[y][x])
                        //input.putFloat((floaterArray[y][x]-minimum[x])/(maximum[x]-minimum[x]))
                    }
                }
            }

            val model = Swim.newInstance(baseContext)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 180, 11, 1), DataType.FLOAT32)
            inputFeature0.loadBuffer(input)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val results = outputFeature0.floatArray

            // Releases model resources if no longer used.
            model.close()

            val index=results.indexOfFirst { it==results.max() }
            for(i in 0..nextsample){
                values_ml.add(Entry(round((time_input[ml_counter+i]-time_input[0])/1000000),index.toFloat()))
            }
            ml_counter+=nextsample
        }
        val set1 = LineDataSet(values, "Output")
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
        val set2 = LineDataSet(values_ml, "ML_Output")
        //val lines = ArrayList<LineDataSet>()
        val lines = ArrayList<ILineDataSet>()
        lines.add(set1)
        lines.add(set2)
        return LineData(lines)
    }

    private fun setDaten_input(): LineData {
        var count=0
        val values = ArrayList<Entry>()
        while(time_input[count]!=0F){
            values.add(Entry(round((time_input[count]-time_input[0])/1000000), floaterArray[count][0]))
            count++
        }
        val set1 = LineDataSet(values, "Input")
        val data = LineData(set1)
        return data
    }

     fun readDaten2() {
        val `is` = resources.openRawResource(R.raw.user13butterfly2)
        //user13butterfly2
        //user13freestyle6
        //val `is` = resources.openRawResource(R.raw.swimmingfreestyle)
        val reader = BufferedReader(
            InputStreamReader(`is`, Charset.forName("UTF-8"))
        )
        var count=0
        var line = ""
        try {
            //while ((reader.readLine()!=null)) {
            while (reader.readLine().also { line = it } != null) {
                // Split the line into different tokens (using the comma as a separator).
                if(count==0) {
                    count++
                    continue
                }
                val tokens = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if(tokens[0].isBlank() or(count==MY_MAX_ARRAY_SIZE+1)) {
                    break
                }

                if(count>3000){
                    tokens[0]
                }
                if(count>3380){
                    tokens[0]
                }
                time_input[count-1]=tokens[1].toFloat()
                label_output[count-1]= tokens[CSV_ARRAY_START+11].toFloat().toInt()
                //Set Array with values
                for(j in CSV_ARRAY_START..CSV_ARRAY_START+10){
                    //tempList.add(tokens[j].toFloat())
                    val currentvalue = tokens[j].toFloat()
                    val counterarray = j-CSV_ARRAY_START
                    floaterArray[count-1][counterarray]=currentvalue

                    if(currentvalue>maximum[counterarray]){
                        maximum[counterarray]=currentvalue
                    }
                    if(currentvalue<minimum[counterarray]){
                        minimum[counterarray]=currentvalue
                    }
                }
                count++

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    suspend fun readDaten(): Unit = withContext(Dispatchers.IO){
        val `is` = resources.openRawResource(R.raw.user13butterfly2)
        //user13butterfly2
        //user13freestyle6
        //val `is` = resources.openRawResource(R.raw.swimmingfreestyle)
        val reader = BufferedReader(
            InputStreamReader(`is`, Charset.forName("UTF-8"))
        )
        var count=0
        var line = ""
        try {
            //while ((reader.readLine()!=null)) {
            while (reader.readLine().also { line = it } != null) {
                // Split the line into different tokens (using the comma as a separator).
                if(count==0) {
                    count++
                    continue
                }
                val tokens = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if(tokens[0].isBlank() or(count==MY_MAX_ARRAY_SIZE+1)) {
                    break
                }

                if(count>3000){
                    tokens[0]
                }
                if(count>3380){
                    tokens[0]
                }
                time_input[count-1]=tokens[1].toFloat()
                label_output[count-1]= tokens[CSV_ARRAY_START+11].toFloat().toInt()
                //Set Array with values
                for(j in CSV_ARRAY_START..CSV_ARRAY_START+10){
                    //tempList.add(tokens[j].toFloat())
                    val currentvalue = tokens[j].toFloat()
                    val counterarray = j-CSV_ARRAY_START
                    floaterArray[count-1][counterarray]=currentvalue

                    if(currentvalue>maximum[counterarray]){
                        maximum[counterarray]=currentvalue
                    }
                    if(currentvalue<minimum[counterarray]){
                        minimum[counterarray]=currentvalue
                    }
                }
                count++

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}

