package com.mx.gillustrated.vo.cultivation

import android.os.Parcel
import android.os.Parcelable

data class LingGen constructor(var id:String, var name:String, var type:Int, var randomBasic:Int, var qiBasic:Int, var color:Int, var inherit:Int = 1)