package com.mx.gillustrated.vo.cultivation

import java.util.concurrent.ConcurrentHashMap

class Nation{
    lateinit var id:String
    lateinit var name:String

    var nationPersonList: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
}