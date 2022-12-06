package com.mx.gillustrated.listener

import android.os.Environment
import android.util.Log
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.util.CommonUtil
import java.io.File
import java.io.IOException
import java.sql.Timestamp

class ExceptionHandler: Thread.UncaughtExceptionHandler {

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, ex: Throwable?) {
        if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState()) {
            val fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_ERROR_PATH)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val file = File(fileDir.path, "error.txt")
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            val message = "${Timestamp(System.currentTimeMillis())}: ${ex?.cause?.toString() ?: ""} ${ex?.message ?: ""}  \n"
            CommonUtil.printFile(message, file, true)
            System.exit(0)
        }

    }




}
