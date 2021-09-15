package com.example.fftanalyzer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private var mLineChart: LineChart? = null
//    private val mPI: Float = 3.1415926535898F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        list.add(LineChartItem(generateDataLine(1), applicationContext))
        list.add(LineChartItem(generateDataLine(2), applicationContext))

        val cda = ChartDataAdapter(applicationContext, list)
        lv.adapter = cda


//        initChart()
//        setData(500)
    }

    private fun setData(count: Int) {
        /* 生成一个数据 */
        val values = ArrayList<Entry>()
        val frequency = 500F

        for (i in 0..count) {
            val time: Float = i.toFloat() / frequency
            val signal: Float =
                cos(2F * PI.toFloat() * time) - 0.3F * cos(2F * PI.toFloat() * 3F * time)
            values.add(Entry(time, signal))
        }


        /* 码位倒序 */

        // 最大比特位数
        val maxBit = 3
        // 数据点数，由于需要进行码位倒叙，因此所有与index相关的变量使用Unsigned Int类型
        val pointNum = 2F.pow(maxBit).toUInt()
        // 数据初始化
        var pointData: Array<Float> = Array(pointNum.toInt()) { it.toFloat() }
        Log.d("mainActivity", "原始数据${pointData.contentToString()}")
        // 类初始化
        val mFFT = MyFFT()
        // 反向编码转化
        pointData = mFFT.binaryBitReverse(pointData, pointNum, maxBit)
        Log.d("mainActivity", "二进制反序${pointData.contentToString()}")

        /* 码位倒序end */

        val set: LineDataSet?
        if (mLineChart!!.data != null &&
            mLineChart!!.data.dataSetCount > 0
        ) {
            set = mLineChart!!.data.getDataSetByIndex(0) as LineDataSet?
            set!!.values = values
            mLineChart!!.data.notifyDataChanged()
            mLineChart!!.notifyDataSetChanged()
        } else {
            set = LineDataSet(values, "测试线")

//            使线条以虚线模式绘制，例如 “ - - - - - ”。
//             如果硬件加速关闭，这个工作就可以了。请记住，硬件加速提高了性能。
//             lineLength线段的长度     spaceLength之间的空格长度
            set.mode = LineDataSet.Mode.LINEAR
//            set!!.enableDashedLine(10f, 5f, 0f)
//            set.enableDashedHighlightLine(10f, 5f, 0f)
            set.setCircleColor(Color.BLACK)
            set.color = Color.BLACK
            set.lineWidth = 2f
            set.circleRadius = 2f
            // 将其设置为true可在每个数据圈中绘制一个圆
            set.setDrawCircleHole(false)
            // 是否显示每点数据
            set.setDrawValues(false)
            set.valueTextSize = 9f
            // 是否显示每点圈圈
            set.setDrawCircles(false)
            //是否填充，默认false
            set.setDrawFilled(false)
            set.fillColor = Color.RED
            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set)
            mLineChart!!.data = LineData(dataSets)

        }
    }


    private fun initChart() {
        //设置背景颜色
        mLineChart!!.setBackgroundColor(Color.WHITE)
        //启用时，将渲染边框矩形。如果启用，则无法绘制x轴和y轴的轴线。
        mLineChart!!.setDrawBorders(true)
        //绘制网格背景
        mLineChart!!.setDrawGridBackground(false)
        //设置背景图片
        mLineChart!!.background = Drawable.createFromPath("")


//        val myMarkerView = MyMarkerView(this@LineChartActivity, R.layout.custom_marker_view)
//        myMarkerView.chartView=mLineChart
//        mLineChart!!.marker=myMarkerView

        //设置图表下面的描述
        val description = Description()
        description.text = "pika"
        description.textColor = Color.RED
        description.textSize = 20f
        mLineChart!!.description = description

        // 不显示描述
        mLineChart!!.description.isEnabled = false
        //x轴
        val xAxis = mLineChart!!.xAxis
        mLineChart!!.defaultValueFormatter
        //x轴的位置在底部
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE

        //是否画x轴上的轴线
        xAxis.setDrawAxisLine(true)
        //是否画x轴上的网格线
        xAxis.setDrawGridLines(true)
        //是否绘制x轴上的标签(不会影响轴线和网格线)
        xAxis.setDrawLabels(true)
        //是否绘制x轴的标签(不会影响轴线和网格线)
        xAxis.setDrawLabels(true)
        //设置轴线的颜色
        xAxis.axisLineColor = Color.BLACK
        //轴线的宽度
        xAxis.axisLineWidth = 2f
        /*
        设置此轴的自定义最大值。 如果设置，则不会计算此值自动取决于提供的数据.使用resetAxisMaxValue（）来撤销此操作。
         */
        xAxis.axisMaximum = 10f
        xAxis.resetAxisMaximum()
        //网格颜色
        xAxis.gridColor = Color.BLACK
        //左边的Y轴
        val axisLeft = mLineChart!!.axisLeft

        axisLeft.setDrawGridLines(true)
        //将其设置为true可绘制零线
        axisLeft.setDrawZeroLine(false)
        //是否画Y轴上的轴线
        axisLeft.setDrawAxisLine(true)
        //是否绘制Y轴的标签(不会影响轴线和网格线)
        axisLeft.setDrawLabels(true)
        //是否画x轴上的网格线
        axisLeft.setDrawGridLines(false)


        // 限制线
//        val ll1 = LimitLine(150f, "Upper Limit")
//        ll1.lineWidth = 4f
//        ll1.enableDashedLine(10f, 10f, 0f)
//        ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
//        ll1.textSize = 10f
//        axisLeft.addLimitLine(ll1)
        //x轴动画
        mLineChart!!.animateX(200)
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

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private fun generateDataLine(cnt: Int): LineData {
        val values1 = ArrayList<Entry>()
        for (i in 0..11) {
            values1.add(Entry(i.toFloat(),
                ((Math.random() * 65).toInt() + 40).toFloat()))
        }
        val d1 = LineDataSet(values1, "New DataSet $cnt, (1)")
        d1.lineWidth = 2.5f
        d1.circleRadius = 4.5f
        d1.highLightColor = Color.rgb(244, 117, 117)
        d1.setDrawValues(false)
        val values2 = ArrayList<Entry>()
        for (i in 0..11) {
            values2.add(Entry(i.toFloat(), values1[i].y - 30))
        }
        val d2 = LineDataSet(values2, "New DataSet $cnt, (2)")
        d2.lineWidth = 2.5f
        d2.circleRadius = 4.5f
        d2.highLightColor = Color.rgb(244, 117, 117)
        d2.color = ColorTemplate.VORDIPLOM_COLORS[0]
        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        d2.setDrawValues(false)
        val sets = ArrayList<ILineDataSet>()
        sets.add(d1)
        sets.add(d2)
        return LineData(sets)
    }

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

}
