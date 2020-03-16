package com.mx.gillustrated.util

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

/**
 * Created by maoxin on 2018/7/27.
 */

object PinyinUtil {

    @JvmStatic
    fun convert(input: String): String {
        val pinyin = StringBuilder()
        for (i in 0 until input.length) {
            val defaultFormat = HanyuPinyinOutputFormat()
            defaultFormat.toneType = HanyuPinyinToneType.WITHOUT_TONE
            defaultFormat.vCharType = HanyuPinyinVCharType.WITH_V
            val c = input[i]
            var pinyinArray: Array<String>? = null
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)
            } catch (e: BadHanyuPinyinOutputFormatCombination) {
                e.printStackTrace()
            }

            if (pinyinArray != null) {
                pinyin.append(pinyinArray[0])
            } else if (c != ' ') {
                pinyin.append(input[i])
            }
        }
        return pinyin.toString()
    }
}
