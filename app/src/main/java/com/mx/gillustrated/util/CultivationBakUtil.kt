package com.mx.gillustrated.util

import android.os.Environment
import com.mx.gillustrated.common.MConfig
import java.io.File

object CultivationBakUtil {

    private val BakFileName = "cultivation_1.json"

    fun saveDataToFiles(json:String) {
        try {
            CommonUtil.printFile(json, CommonUtil.generateDataFile(BakFileName))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getDataFromFiles():String? {
        val fileDir = File(Environment.getExternalStorageDirectory(),
                MConfig.SD_DATA_PATH)
        val jsonFile = File(fileDir.path, BakFileName)
        if (jsonFile.exists()) {
            return JsonFileReader.getJson(jsonFile)
        }
        return null
    }

    fun findFemaleHeaderSize():Int {
        val imageDir = File(Environment.getExternalStorageDirectory(),
                MConfig.SD_CULTIVATION_HEADER_PATH + "/" + NameUtil.Gender.Female)
        if (imageDir.exists()) {
            return  imageDir.list().size
        }
        return 0
    }

    fun findMaleHeaderSize():Int {
        val imageDir = File(Environment.getExternalStorageDirectory(),
                MConfig.SD_CULTIVATION_HEADER_PATH + "/" + NameUtil.Gender.Male)
        if (imageDir.exists()) {
            return  imageDir.list().size
        }
        return 0
    }


}