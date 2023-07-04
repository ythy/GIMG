@file:Suppress("DEPRECATION")
package com.mx.gillustrated.activity

import android.app.Activity
import com.mx.gillustrated.MyApplication
import com.mx.gillustrated.database.DataBaseHelper
import com.mx.gillustrated.di.components.DaggerBaseActivityComponent
import com.mx.gillustrated.di.modules.BaseActivityModule
import android.app.ProgressDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mx.gillustrated.R
import com.mx.gillustrated.util.CommonUtil
import java.io.File
import java.net.URI

import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity() {

    @Inject
    lateinit var mOrmHelper: DataBaseHelper

    @Inject
    lateinit var mProgressDialog: ProgressDialog

    @Inject
    lateinit var mSP: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        doInject()
        when (mSP.getString("theme", "Blue")){
            "Green"-> theme.applyStyle(R.style.AppTheme_Green, true)
            "Blue"-> theme.applyStyle(R.style.AppTheme_Blue, true)
            "Orange"-> theme.applyStyle(R.style.AppTheme_Orange, true)
        }
        super.onCreate(savedInstanceState)
    }

    private fun doInject() {
        DaggerBaseActivityComponent.builder()
                .appComponent((application as MyApplication).appComponent)
                .baseActivityModule(BaseActivityModule(this))
                .build()
                .inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_DELETE || requestCode == REQUEST_PERMISSION_DELETE2 ||
                requestCode == REQUEST_PERMISSION_DELETE3 ) {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this,"删除成功", Toast.LENGTH_SHORT).show()
                    deleteSuccessHanlder(requestCode)
                }else{
                    Toast.makeText(this,"删除失败", Toast.LENGTH_SHORT).show()
                }
        }
    }

    open fun deleteSuccessHanlder(code:Int){

    }

    //method only call from API 30 onwards
    @RequiresApi(Build.VERSION_CODES.R)
    fun deleteMediaBulk(context: Context, media: List<Uri>): IntentSender {
        return MediaStore.createDeleteRequest(context.contentResolver,
            media.toMutableList()).intentSender
    }

    fun deleteImages(mediaFiles: List<File>, callback:OnCallback? = null, code:Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uriList = mediaFiles.mapNotNull {
                CommonUtil.getImageContentUri(this, it)
            }
            startIntentSenderForResult(deleteMediaBulk(this, uriList), code, null, 0, 0, 0)
        }else {
            mediaFiles.forEach {
                CommonUtil.deleteImages(this, it)
            }
            callback?.deleted()
        }

    }

    fun deleteImages(mediaFile: File, callback:OnCallback? = null, code:Int = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uri = CommonUtil.getImageContentUri(this, mediaFile) ?: return
            startIntentSenderForResult(deleteMediaBulk(this, listOf(uri)), code, null, 0, 0, 0)
        }else {
            CommonUtil.deleteImages(this, mediaFile)
            callback?.deleted()
        }

    }

    interface OnCallback{
        fun deleted()
    }

   companion object {

        const val SHARE_IMAGE_ORIENTATION = "gameinfo_image_orientation"
        const val SHARE_IMAGE_DATE = "gameinfo_image_date"
        const val SHARE_IMAGE_ORIENTATION_EVENT = "gameinfo_image_orientation_event"
        const val SHARE_IMAGES_MATRIX = "add_images_matrix"
        const val SHARE_IMAGES_MATRIX_NUMBER = "add_images_matrix_number"
        const val SHARE_SHOW_HEADER_IMAGES = "show_header_images"
        const val SHARE_PAGE_SIZE = "list_page_size"
        const val SHARE_ASSOCIATION_GAME_ID = "association_game_id"
        const val SHARE_IMAGES_HEADER_SCALE_NUMBER = "header_images_scale_float_number"
        const val SHARE_SHOW_COST_COLUMN = "gameinfo_show_cost_column"
        const val REQUEST_PERMISSION_DELETE = 4044
        const val REQUEST_PERMISSION_DELETE2 = 4046
        const val REQUEST_PERMISSION_DELETE3 = 4048
    }
}
