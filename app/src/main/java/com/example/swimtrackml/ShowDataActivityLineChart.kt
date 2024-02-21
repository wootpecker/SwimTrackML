package com.example.swimtrackml

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.concurrent.TimeUnit


class ShowDataActivityLineChart : DemoBase(), OnSeekBarChangeListener {

    private var chart: LineChart? = null
    private var seekBarX: SeekBar? = null
    private var tvX: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.show_data_linechart)
        title = "LineChartTime"
        tvX = findViewById(R.id.tvXMax)
        seekBarX = findViewById(R.id.seekBar1)
        seekBarX!!.setOnSeekBarChangeListener(this)
        chart = findViewById(R.id.chart1)

        // no description text
        chart!!.description.isEnabled = false

        // enable touch gestures
        chart!!.setTouchEnabled(true)
        chart!!.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging
        chart!!.isDragEnabled = true
        chart!!.setScaleEnabled(true)
        chart!!.setDrawGridBackground(false)
        chart!!.isHighlightPerDragEnabled = true

        // set an alternative background color
        chart!!.setBackgroundColor(Color.WHITE)
        chart!!.setViewPortOffsets(0f, 0f, 0f, 0f)

        // add data
        seekBarX!!.progress = 100

        // get the legend (only possible after setting data)
        val l = chart!!.legend
        l.isEnabled = true
        val xAxis = chart!!.xAxis
        //xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        //xAxis.typeface = tfLight
        xAxis.textSize = 24f
        xAxis.textColor = Color.BLACK
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.textColor = Color.rgb(255, 192, 56)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f // one hour
        xAxis.isEnabled=true


        /*xAxis.valueFormatter = (MyValueFormatter(xValsDateLabel))
        class MyValueFormatter(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

            override fun getFormattedValue(value: Float): String {
                return value.toString()
            }

            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1) {
                    return xValsDateLabel[value.toInt()]
                } else {
                    return ("").toString()
                }
            }
        }



        xAxis.valueFormatter = IAxisValueFormatter { value, axis ->
            if (xValsDateLabel != null && value.toInt() >= 0
                && value.toInt() <= xValsDateLabel!!.size - 1) {
                xValsDateLabel!![value.toInt()].toString()
            } else {
                ("").toString()
            }
        } as ValueFormatter
*/

        //xAxis.valueFormatter=SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH).format(Date(TimeUnit.HOURS.toMillis()))


        /*xAxis.setValueFormatter(object : IAxisValueFormatter {
            private val mFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)
            @Deprecated("Deprecated in Java")
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                val millis = TimeUnit.HOURS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        })*/
        val leftAxis = chart!!.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis.typeface = tfRegular
        leftAxis.textColor = Color.BLACK  //ColorTemplate.getHoloBlue()
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 170f
        leftAxis.yOffset = -9f
        val rightAxis = chart!!.axisRight
        rightAxis.isEnabled = true
    }



    private fun setData(count: Int, range: Float) {

        // now in hours
        val now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())
        val values = ArrayList<Entry>()

        // count = hours
        val to = (now + count).toFloat()

        // increment by 1 hour
        var x = now.toFloat()
        while (x < to) {
            val y: Float = getRandom(range, 50f)
            values.add(Entry(x, y)) // add one entry per hour
            x++
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")
        set1.axisDependency = AxisDependency.LEFT
        set1.color = ColorTemplate.getHoloBlue()
        set1.valueTextColor = ColorTemplate.getHoloBlue()
        set1.lineWidth = 1.5f
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.fillAlpha = 65
        set1.fillColor = ColorTemplate.getHoloBlue()
        set1.highLightColor = Color.rgb(244, 117, 117)
        set1.setDrawCircleHole(false)

        // create a data object with the data sets
        val data = LineData(set1)
        data.setValueTextColor(Color.BLACK)
        data.setValueTextSize(9f)

        // set data
        chart!!.data = data
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.make_ml, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartTime.java"))
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                val sets = chart!!.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.setDrawValues(!set.isDrawValuesEnabled)
                }
                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data.isHighlightEnabled = !chart!!.data.isHighlightEnabled
                    chart!!.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                val sets = chart!!.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawFilledEnabled) set.setDrawFilled(false) else set.setDrawFilled(
                        true
                    )
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCircles -> {
                val sets = chart!!.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawCirclesEnabled) set.setDrawCircles(false) else set.setDrawCircles(
                        true
                    )
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCubic -> {
                val sets = chart!!.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.mode == LineDataSet.Mode.CUBIC_BEZIER) set.mode =
                        LineDataSet.Mode.LINEAR else set.mode =
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                chart!!.invalidate()
            }

            R.id.actionToggleStepped -> {
                val sets = chart!!.data
                    .dataSets
                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.mode == LineDataSet.Mode.STEPPED) set.mode =
                        LineDataSet.Mode.LINEAR else set.mode =
                        LineDataSet.Mode.STEPPED
                }
                chart!!.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled) chart!!.setPinchZoom(false) else chart!!.setPinchZoom(
                    true
                )
                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }

            R.id.animateX -> {
                chart!!.animateX(2000)
            }

            R.id.animateY -> {
                chart!!.animateY(2000)
            }

            R.id.animateXY -> {
                chart!!.animateXY(2000, 2000)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveToGallery(chart,"name")
                } else {
                    requestStoragePermission(chart)
                }
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()
        setData(seekBarX!!.progress, 50f)

        // redraw
        chart!!.invalidate()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    protected fun saveToGallery(chart: LineChart?, name: String) {
        if (chart != null) {
            if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70)) Toast.makeText(
                applicationContext, "Saving SUCCESSFUL!",
                Toast.LENGTH_SHORT
            ).show() else Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT)
                .show()
        }
    }

}


