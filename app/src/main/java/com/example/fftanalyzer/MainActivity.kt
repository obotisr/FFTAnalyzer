package com.example.fftanalyzer

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.fftanalyzer.chart.ChartItem
import com.example.fftanalyzer.chart.LineChartItem
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*
import kotlin.math.cos
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private val pi: Float = 3.141592653589793F
    private val tpi: Float = 2F * pi

    val xLabels = arrayOfNulls<String>(1)

    private var mLineChart: LineChart? = null
//    private val mPI: Float = 3.1415926535898F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(tag, "onCreate")

        // 阻止屏幕休眠
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // 全屏，隐藏状态栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // 隐藏标题栏，必须放在getWindow之后
        supportActionBar?.hide()

        //
        val lv: ListView = findViewById(R.id.listView1)

        val list: ArrayList<ChartItem> = ArrayList<ChartItem>()


        // 最大比特位数
        val maxBit = 8
        // 数据点数，由于需要进行码位倒叙，因此所有与index相关的变量使用Unsigned Int类型
        val pntNum = 2F.pow(maxBit).toUInt()
        // 采样频率
        val smpFrq = pntNum.toFloat() * 10F
        Log.d(tag, "pntNum = $pntNum smpFrq = $smpFrq")

        // 数据生成
        val timeData: Array<Float> =
            Array(pntNum.toInt()) { i -> i.toFloat() / smpFrq }
        var pointData: Array<Float> =
            Array(pntNum.toInt()) { i ->
                cos(tpi * 50F * timeData[i]) - 0.3F * cos(tpi * 150F * timeData[i] + tpi / 3F) - 0.1F * cos(
                    tpi * 450F * timeData[i])
            }
        // 画图
        list.add(LineChartItem(generateDataLine(timeData, pointData, "原始数据 时间（s）"),
            applicationContext))

        // 计算
        val mFFT = MyFFT()
        // 反向编码转化
        pointData = mFFT.binaryBitReverse(pointData, pntNum, maxBit)
        list.add(LineChartItem(generateDataLine(Array(pntNum.toInt()) { i -> i.toFloat() },
            pointData,
            "反向编码 数据索引"),
            applicationContext))

        // FFT运算
        pointData = mFFT.butterflyOperation(pointData, pntNum, maxBit)
        val freqData: Array<Float> = mFFT.freqResolution(pntNum.toFloat(), smpFrq)
        list.add(LineChartItem(generateDataLine(freqData, pointData, "FFT 频率（Hz）"),
            applicationContext))

        //
        val cda = ChartDataAdapter(applicationContext, list)
        lv.adapter = cda

    }


    private fun generateDataLine(
        dataX: Array<Float>,
        dataY: Array<Float>,
        title: String,
    ): LineData {
        val values = ArrayList<Entry>()
        val num = dataX.size
        for (i in 0 until num) {
            values.add(Entry(dataX[i], dataY[i]))
        }
        val d1 = LineDataSet(values, title)
        d1.color = Color.BLUE
        d1.setDrawValues(true)
        d1.setDrawCircles(false)

        val sets = ArrayList<ILineDataSet>()
        sets.add(d1)
        return LineData(sets)
    }


//    /**
//     * generates a random ChartData object with just one DataSet
//     *
//     * @return Line data
//     */
//    private fun generateDataLine(cnt: Int): LineData {
//        val values1 = ArrayList<Entry>()
//        for (i in 0..11) {
//            values1.add(Entry(i.toFloat(),
//                ((Math.random() * 65).toInt() + 40).toFloat()))
//        }
//        val d1 = LineDataSet(values1, "New DataSet $cnt, (1)")
//        d1.lineWidth = 2.5f
//        d1.circleRadius = 4.5f
//        d1.highLightColor = Color.rgb(244, 117, 117)
//        d1.color = Color.BLUE
//        d1.setDrawValues(false)
//        d1.setDrawCircles(false)
//
//        val values2 = ArrayList<Entry>()
//        for (i in 0..11) {
//            values2.add(Entry(i.toFloat(), values1[i].y - 30))
//        }
//        val d2 = LineDataSet(values2, "New DataSet $cnt, (2)")
//        d2.lineWidth = 2.5f
//        d2.circleRadius = 4.5f
//        d2.highLightColor = Color.rgb(244, 117, 117)
//        d2.color = Color.RED
//        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
//        d2.setDrawValues(false)
//        d2.setDrawCircles(false)
//
//        val sets = ArrayList<ILineDataSet>()
//        sets.add(d1)
//        sets.add(d2)
//        return LineData(sets)
//    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private fun generateDataBar(cnt: Int): BarData {
        val entries = ArrayList<BarEntry>()
        for (i in 0..11) {
            entries.add(BarEntry(i.toFloat(), ((Math.random() * 70).toInt() + 30).toFloat()))
        }
        val d = BarDataSet(entries, "New DataSet $cnt")
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        d.highLightAlpha = 255
        val cd = BarData(d)
        cd.barWidth = 0.9f
        return cd
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Pie data
     */
    private fun generateDataPie(): PieData {
        val entries = ArrayList<PieEntry>()
        for (i in 0..3) {
            entries.add(PieEntry((Math.random() * 70 + 30).toFloat(), "Quarter " + (i + 1)))
        }
        val d = PieDataSet(entries, "")

        // space between slices
        d.sliceSpace = 2f
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        return PieData(d)
    }

    /** adapter that supports 3 different item types  */
    private class ChartDataAdapter(
        context: Context?,
        objects: List<ChartItem?>?,
    ) :
        ArrayAdapter<ChartItem?>(context!!, 0, objects!!) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getItem(position)!!.getView(position, convertView, context)
        }

        override fun getItemViewType(position: Int): Int {
            // return the views type
            val ci = getItem(position)
            return ci?.itemType ?: 0
        }

        override fun getViewTypeCount(): Int {
            return 3 // we have 3 different item-types
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(tag, "onRestart")
    }

}
