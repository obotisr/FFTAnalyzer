package com.example.fftanalyzer

import android.util.Log
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


// 参考 https://zhuanlan.zhihu.com/p/135259438
// 2021年9月17日10:54:53 by Liu He
// 默认只能处理实数数据

class MyFFT {

    private val pi: Float = 3.141592653589793F

    /*  数据基于二进制编码反序
    例程：
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
    Log.d("mainActivity", "二进制反序${pointData.contentToString()}") */
    fun binaryBitReverse(pointData: Array<Float>, pointNum: UInt, maxBit: Int): Array<Float> {
        Log.d("binaryBitReverse", "开始执行码位倒序")
        // 由于需要进行码位倒叙，因此所有与index相关的变量使用Unsigned Int类型
        // 二进制前数据
        Log.d("binaryBitReverse", "原始数据${pointData.contentToString()}")
        // 倒序前后索引
        var dataIndex: UInt = 0u
        var revIndex: UInt
        // 根据规律四，需要对数组全部元素执行码间倒序
        Log.d("binaryBitReverse", "|->开始遍历所有元素索引，maxBit=$maxBit，pointNum=$pointNum")
        while (dataIndex < pointNum) {
            Log.d("binaryBitReverse", "|--->开始遍历dataIndex=${dataIndex}元素位")
            // 获取下标dataIndex的反序revIndex数值
            revIndex = 0u
            // 利用Float转Int的截断效果，实现7位循环4次，8位循环4次
            for (bitPointer in 0..(maxBit.toFloat() / 2F - 0.5F).toInt()) {
                // 反序操作
                // 最高位为1的二进制数右移k位
                val maskSta = 2F.pow(maxBit - 1).toUInt().shr(bitPointer)
                // 最低位为1的二进制数左移k位
                val maskEnd = 1u.shl(bitPointer)

                // dataIndex与lShBit 按位与 提取出前半部分第bitCount位
                val maskStaIndex = dataIndex.and(maskSta)
                // dataIndex与rShBit 按位与 提取出后半部分第bitCount位
                val maskEndIndex = dataIndex.and(maskEnd)

                // 当提取的dataIndex前位为1，则将对应后位设为1；反之没有动作，因为本来就全是0
                if (maskStaIndex != 0u) {
                    revIndex = revIndex.or(maskEnd)
                }
                // 当提取的dataIndex后位为1，则将对应前位设为1；反之没有动作，因为本来就全是0
                if (maskEndIndex != 0u) {
                    revIndex = revIndex.or(maskSta)
                }

                Log.d("binaryBitReverse", "|----->bitPointer=${bitPointer}")
                Log.d(
                    "binaryBitReverse",
                    "|----->dataIndex= ${dataIndex.toString(2).padStart(maxBit, '0')}," +
                            " maskSta=${maskSta.toString(2).padStart(maxBit, '0')}," +
                            " maskEnd=${maskEnd.toString(2).padStart(maxBit, '0')}," +
                            " maskStaIndex=${maskStaIndex.toString(2).padStart(maxBit, '0')}," +
                            " maskEndIndex=${maskEndIndex.toString(2).padStart(maxBit, '0')}"
                )
            }
            // 只有当前索引小于反序索引后，才进行调换
            if (dataIndex < revIndex) {
                pointData[dataIndex.toInt()] = pointData[revIndex.toInt()].apply {
                    pointData[revIndex.toInt()] = pointData[dataIndex.toInt()]
                }
            }

            Log.d("binaryBitReverse", "|--->计算结果revIndex=${revIndex}")
            dataIndex += 1u
        }
        // 二进制反序后数据
        Log.d("binaryBitReverse", "二进制反序数据${pointData.contentToString()}")
        return pointData
    }

    fun butterflyOperation(dataR: Array<Float>, pntNum: UInt, maxBit: Int): Array<Float> {

        val dataI = Array(pntNum.toInt()) { 0F }

        val M = maxBit
        val N = pntNum.toInt()
        // FFT蝶形级数L从1到M
        for (L in 1..M) {
            // 第L级运算
            // 首先计算间隔 B=2^(L-1)
            val B = 2F.pow(L - 1).toInt()
            for (j in 0 until B) {
                // 同种蝶形运算
                // 先计算增量 k=2^(M-L)
                val k = 2F.pow(M - L).toInt()
                //计算旋转指数p，增量为k时，则p=j*k
                val p = j * k
                for (i in 0 until k) {
                    /*进行蝶形运算*/
                    //数组下标定为r
                    val r = j + 2 * B * i

                    val Tr =
                        dataR[r + B] * cos(2F * pi * p / N) + dataI[r + B] * sin(2F * pi * p / N);
                    val Ti =
                        dataI[r + B] * cos(2F * pi * p / N) - dataR[r + B] * sin(2F * pi * p / N);
                    dataR[r + B] = dataR[r] - Tr
                    dataI[r + B] = dataI[r] - Ti
                    dataR[r] = dataR[r] + Tr
                    dataI[r] = dataI[r] + Ti
                }
            }
        }
        val arrayA =
            Array(pntNum.toInt()) { i -> sqrt(dataR[i] * dataR[i] + dataI[i] * dataI[i]) * 2F / pntNum.toFloat() }
        return arrayA.take(pntNum.toInt() / 2 + 1).toTypedArray()
    }

    fun freqResolution(pntNum: Float, smpFrq: Float): Array<Float> {
        val arrayF = Array(pntNum.toInt()) { i -> smpFrq * i.toFloat() / pntNum }
        return arrayF.take(pntNum.toInt() / 2 + 1).toTypedArray()
    }

}