package com.example.fftanalyzer

import android.util.Log
import kotlin.math.pow

class MyFFT {

    // 数据基于二进制编码反序
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

}