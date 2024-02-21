package com.example.swimtrackml

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.model.GradientColor
import java.io.FileNotFoundException
import java.io.IOException
import java.util.logging.Logger


class ShowDataActivity : Activity() {
    private var chart: BarChart? = null
    private val seekBarX: SeekBar? = null
    private var seekBarY:SeekBar? = null
    private val tvX: TextView? = null
    private var tvY:TextView? = null

    val Log = Logger.getLogger(MainActivity::class.java.name)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.show_graph)
        title = "BarChartActivity"
        chart = findViewById(R.id.chart1)
        chart?.setDrawBarShadow(false)

        chart?.setDrawValueAboveBar(true)
        chart?.description?.isEnabled = false
        // if more than 60 entries are displayed in the chart, no values will be drawn
        chart?.setMaxVisibleValueCount(180)

        chart?.setPinchZoom(false)// scaling can now only be done on x- and y-axis separately
        chart?.setDrawGridBackground(false);
        val l: Legend? = chart?.legend
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
        setData(5, 100);

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



    private fun saveData() {
        val txtinput=findViewById<EditText>(R.id.txtinput)
        val filename = "ourfile.txt"
        Thread(Runnable {
            try {
                val out = openFileOutput(filename, Context.MODE_PRIVATE)
                out.use {
                    out.write(txtinput.text.toString().toByteArray())
                }
                runOnUiThread(Runnable {
                    Toast.makeText(this,"Saved", Toast.LENGTH_LONG).show()
                })
            }
            catch(ioe:IOException) {
                Log.warning("Error while saving ${filename} : ${ioe}")
            }
        }).start()
    }

    override fun onPause() {
        super.onPause()
       // saveData()
    }

    override fun onResume() {
        super.onResume()
       // loadData()
    }

    private fun loadData() {
        val txtinput=findViewById<EditText>(R.id.txtinput)

        val filename = "ourfile.txt"
        Thread(Runnable{
            try {
                val input = openFileInput(filename)
                input.use {
                    var buffer = StringBuilder()
                    var bytes_read = input.read()

                    while(bytes_read != -1) {
                        buffer.append(bytes_read.toChar())
                        bytes_read = input.read()
                    }
                    runOnUiThread(Runnable{
                        txtinput.setText(buffer.toString())
                    })
                }
            }
            catch(fnfe:FileNotFoundException) {
                Log.warning("file not found, occurs only once")
            }
            catch(ioe: IOException) {
                Log.warning("IOException : $ioe")
            }
        }).start()
    }
}

