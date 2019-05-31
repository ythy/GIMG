package com.mx.gillustrated.component

import com.mx.gillustrated.activity.BaseActivity

/**
 * Created by maoxin on 2019/5/31.
 */
class ResourceController(private val context:BaseActivity, private val gameId:Int) {

    var number1:String
        get() = context.mSP.getString("${gameId}_row_number1", "HP")
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number1", value).commit()
        }

    var number2:String
        get() = context.mSP.getString("${gameId}_row_number2", "A.")
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number2", value).commit()
        }

    var number3:String
        get() = context.mSP.getString("${gameId}_row_number3", "D.")
        set(value) {
            context.mSP.edit().putString("${gameId}_row_number3", value).commit()
        }


}