package com.mx.gillustrated.util

import android.content.Context
import android.os.Environment
import com.mx.gillustrated.common.MConfig
import java.io.File

object CultivationBakUtil {

    private val BakFileName = "cultivation_1.json"

    fun saveDataToFiles(context: Context, json:String, filename:String = BakFileName) {
        try {
            CommonUtil.printFile(json, CommonUtil.generateDataFileNew(context, filename))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getDataFromFiles(context: Context, filename:String = BakFileName):String? {
        val fileDir = context.getExternalFilesDir(MConfig.SD_DATA_PATH_NEW)
        val jsonFile = File(fileDir?.path, filename)
        if (jsonFile.exists()) {
            return JsonFileReader.getJson(jsonFile)
        }
        return null
    }

    private fun getFileName(path:String):String{
        val temp = path.substringBeforeLast(".").split("//")
        return temp[temp.size - 1]
    }

    fun findFemaleHeaderSize():Int {
        val imageDir = File(Environment.getExternalStorageDirectory(),
                MConfig.SD_CULTIVATION_HEADER_PATH_OLD + "/" + NameUtil.Gender.Female)
        if (imageDir.exists()) {
            return  imageDir.list().filter { it != null && getFileName(it).length < 4  }.size
        }
        return 0
    }

    fun findMaleHeaderSize():Int {
        val imageDir = File(Environment.getExternalStorageDirectory(),
                MConfig.SD_CULTIVATION_HEADER_PATH_OLD + "/" + NameUtil.Gender.Male)
        if (imageDir.exists()) {
            return  imageDir.list().filter { it != null && getFileName(it).length < 4  }.size
        }
        return 0
    }


}