package com.mx.gillustrated.dialog

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import androidx.appcompat.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.BaseActivity
import com.mx.gillustrated.activity.BaseActivity.Companion.SHARE_IMAGES_HEADER_SCALE_NUMBER
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.MatrixInfo

object DialogExportImg {

    fun show(context: BaseActivity, nid: Int, gameId: Int,
             handler: Handler) {
        val dlg = AlertDialog.Builder(context).create()
        dlg.show()
        val window = dlg.window
        window!!.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window.setContentView(R.layout.dialog_export_img)

        val x1 = window.findViewById<View>(R.id.etX1) as EditText
        val y1 = window.findViewById<View>(R.id.etY1) as EditText
        val width1 = window.findViewById<View>(R.id.etWidth1) as EditText
        val height1 = window.findViewById<View>(R.id.etHeight1) as EditText
        val etScale = window.findViewById<View>(R.id.etScale) as EditText

        val sets = CommonUtil.getMatrixInfo(context, 6, gameId)
        x1.setText(sets.x.toString())
        y1.setText(sets.y.toString())
        width1.setText(sets.width.toString())
        height1.setText(sets.height.toString())
        etScale.setText(getScale(context, gameId).toString())

        var compress: Bitmap? = null
        if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState()) {
            val fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_PATH + "/" + gameId)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val imageFile = File(fileDir.path, CommonUtil.getImageFrontName(nid, 1))
            if (imageFile.exists()) {
                try {
                    compress = MediaStore.Images.Media.getBitmap(
                            context.contentResolver, Uri.fromFile(imageFile))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        val imageView = window
                .findViewById<View>(R.id.imgHeader) as ImageView
        val btnSave = window.findViewById<View>(R.id.btnSave) as ImageButton
        if (compress != null) {
            val scaleNumOrigin = getScale(context, gameId)
            if (scaleNumOrigin == 0f)
                imageView.setImageBitmap(CommonUtil.toRoundBitmap(CommonUtil.cutBitmap(compress,
                        sets, false)))
            else
                imageView.setImageBitmap(CommonUtil.scaleBitmap(CommonUtil.cutBitmap(compress,
                        sets, false), scaleNumOrigin))

            btnSave.setOnClickListener {
                val matrixInfo1 = MatrixInfo(Integer.parseInt(x1
                        .text.toString()), Integer.parseInt(y1.text
                        .toString()), Integer.parseInt(width1.text
                        .toString()), Integer.parseInt(height1.text
                        .toString()))
                CommonUtil.setMatrixInfo(context, 6, matrixInfo1, gameId)

                val cutBitMap = CommonUtil.cutBitmap(compress, matrixInfo1, false)
                val scaleNumForSave = java.lang.Float.valueOf(etScale.text.toString())
                setScale(context, gameId, scaleNumForSave)
                if (scaleNumForSave == 0f)
                    imageView.setImageBitmap(CommonUtil.toRoundBitmap(cutBitMap))
                else
                    imageView.setImageBitmap(CommonUtil.scaleBitmap(cutBitMap, scaleNumForSave))
            }
        } else {
            btnSave.visibility = View.GONE
        }


        val btnOk = window.findViewById<View>(R.id.btnOk) as Button
        btnOk.setOnClickListener { dlg.dismiss() }

        val btnExport = window.findViewById<View>(R.id.btnExport) as Button
        btnExport.setOnClickListener {
            handler.sendEmptyMessage(2)
            dlg.dismiss()
        }

        val btnClear = window.findViewById<View>(R.id.btnCover) as Button
        btnClear.setOnClickListener {
            handler.sendEmptyMessage(4)
            dlg.dismiss()
        }


    }

    private fun getScale(activity: BaseActivity, gameType: Int): Float {
        return activity.mSP.getFloat(SHARE_IMAGES_HEADER_SCALE_NUMBER + gameType, 0f)
    }

    private fun setScale(activity: BaseActivity, gameType: Int, scale: Float) {
        activity.mSP.edit().putFloat(SHARE_IMAGES_HEADER_SCALE_NUMBER + gameType, scale).apply()
    }

}
