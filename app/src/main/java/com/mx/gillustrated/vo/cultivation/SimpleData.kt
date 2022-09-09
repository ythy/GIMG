package com.mx.gillustrated.vo.cultivation

data class SimpleData constructor(
        var id:String,
        var name:String,
        var remark:String?,
        var type:Int = 0,
        var property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0),
        var seq:Int = 0
)