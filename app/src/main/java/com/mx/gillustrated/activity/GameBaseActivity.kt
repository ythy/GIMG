@file:Suppress("DEPRECATION")
package com.mx.gillustrated.activity

import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Clan
import com.mx.gillustrated.vo.cultivation.Nation
import com.mx.gillustrated.vo.cultivation.Person
import java.util.concurrent.ConcurrentHashMap


abstract class GameBaseActivity: BaseActivity() {

    var mPersons: ConcurrentHashMap<String, Person> = ConcurrentHashMap()
    var mAlliance: ConcurrentHashMap<String, Alliance> = ConcurrentHashMap()
    var mClans: ConcurrentHashMap<String, Clan> = ConcurrentHashMap()
    var mNations:ConcurrentHashMap<String, Nation> = ConcurrentHashMap()

    fun getOnlinePersonDetail(id:String?):Person?{
        if(id == null)
            return null
        return mPersons[id]
    }

    fun getPersonData(id: String?):Person?{
        if(id == null)
            return null
        return  getOnlinePersonDetail(id)
    }

    fun getAlliancePersonList(allianceId:String):ConcurrentHashMap<String, Person>{
        return ConcurrentHashMap(mPersons.filter { it.value.allianceId ==  allianceId})
    }

    fun getClanPersonList(clanId:String):ConcurrentHashMap<String, Person>{
        return ConcurrentHashMap(mPersons.filter { it.value.clanId ==  clanId})
    }

    abstract fun killPerson(id:String)
    abstract fun addPersonLifetime(id:String):Boolean
    abstract fun combinedPersonRelationship(person: Person, log:Boolean = true)
    abstract fun showToast(content:String)
    abstract fun loadSkin()
}
