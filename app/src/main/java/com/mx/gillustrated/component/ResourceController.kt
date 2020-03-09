package com.mx.gillustrated.component

import com.mx.gillustrated.activity.BaseActivity

/**
 * Created by maoxin on 2019/5/31.
 */
class ResourceController(private val context:BaseActivity, private val gameId:Int) {

    var number1:String
        get() = context.mSP.getString("${gameId}_row_number1", "HP")!!
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number1", value).apply()
        }

    var number2:String
        get() = context.mSP.getString("${gameId}_row_number2", "A.")!!
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number2", value).apply()
        }

    var number3:String
        get() = context.mSP.getString("${gameId}_row_number3", "D.")!!
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number3", value).apply()
        }
    var number4:String
        get() = context.mSP.getString("${gameId}_row_number4", "E1")!!
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number4", value).apply()
        }
    var number5:String
        get() = context.mSP.getString("${gameId}_row_number5", "E2")!!
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number5", value).apply()
        }
    var eventImagesGap:Boolean
        get() = context.mSP.getBoolean("${gameId}_event_images_gap", false)
        set(value) {
            context.mSP.edit().putBoolean("${gameId}_event_images_gap", value).apply()
        }
}