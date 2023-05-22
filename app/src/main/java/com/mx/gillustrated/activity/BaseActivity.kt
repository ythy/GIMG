@file:Suppress("DEPRECATION")
package com.mx.gillustrated.activity

import com.mx.gillustrated.MyApplication
import com.mx.gillustrated.database.DataBaseHelper
import com.mx.gillustrated.di.components.DaggerBaseActivityComponent
import com.mx.gillustrated.di.modules.BaseActivityModule
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mx.gillustrated.R

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
        when (mSP.getString("theme", "Green")){
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
    }
}
