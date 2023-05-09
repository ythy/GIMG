package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.*
import java.util.*

@SuppressLint("SetTextI18n")
object CultivationEnemyHelper {

    data class BossSetting(val name:String, val type:Int, val bonus: Int, val ratity:Int)

    //暂定取消
    val bossSettings = mutableListOf(
            BossSetting("\u674e\u5143\u9738\u4e4b\u9b42", 6,20, 5),
            BossSetting("\u6697\u5f71\u4e4b\u9b42", 6,30, 7),
            BossSetting("\u7403\u4e4b\u9b42", 6,40, 8),
            BossSetting("\u989C\u4e4b\u9b42", 6,50, 9)
    )

    //暂定取消
    fun getEquipmentOfBoss(index:Int, count:Int):Pair<EquipmentConfig, String>{
        return Pair(EquipmentConfig(
                "",
                bossSettings[index].name,
                bossSettings[index].type,
                bossSettings[index].ratity,
                bossSettings[index].bonus * CultivationHelper.getValidBonus(count),
                0,
                mutableListOf(0,0,0,0),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
        ), "${CultivationHelper.showing(bossSettings[index].name)}($count/${CultivationHelper.getValidBonus(count)})")
    }


    private fun updateBossProps(person: Person, tejiQuantity:Int, followerQuantity:Pair<Int, Int>, jingJieLevel:Int, hp:Int, hit:Int, specTeji:List<String> = listOf()){
        val tejiList = CultivationHelper.mConfig.teji.filter { it.type != 4 && it.type != 6 }.shuffled()
        person.teji.addAll(tejiList.subList(0, tejiQuantity).map { it.id })
        if(!person.teji.contains("8001006")){//神迹
            person.teji.add("8001006")
        }
        specTeji.forEach {
            if(!person.teji.contains(it)){
                person.teji.add(it)
            }
        }
        person.equipmentList.addAll(listOf(Equipment("7002901")))
        repeat(followerQuantity.first) {
            person.followerList.add(Follower("9000101", "${it + 1}号"))
        }
        repeat(followerQuantity.second) {
            person.followerList.add(Follower("9000102", "${it + 1}号"))
        }
        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, jingJieLevel)
        person.lifetime = person.birthtime + CultivationHelper.getLifetimeBonusInitial(person, null, true)
        person.HP = hp
        person.maxHP = hp
        person.remainHit = hit
    }

    fun generateLiYuanBa(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("李", "\u5143\u9738"), NameUtil.Gender.Male,null,
                CultivationSetting.PersonFixedInfoMix(null, null, 1000, 1000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 1
        updateBossProps(person, 4, Pair(4, 1), 60,1200, 60, listOf("8001004", "8002001"))
        return person
    }

    fun generateShadowMao(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("毛", "\u6b23(\u6697\u5f71)"), NameUtil.Gender.Male,null,
                CultivationSetting.PersonFixedInfoMix(null, null, 4000, 4000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 2
        updateBossProps(person, 6, Pair(4, 1), 65,1400, 80, listOf("8001005", "8002002"))
        return person
    }

    fun generateShadowQiu(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("\u7403\u7403", "(\u5706\u6eda\u6eda)"), NameUtil.Gender.Female, null,
                CultivationSetting.PersonFixedInfoMix(null, null, 6000, 6000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 3
        updateBossProps(person, 8, Pair(4, 1), 70,1600, 100, listOf("8004001", "8002002"))
        return person
    }

    fun generateYaoWang(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("大小乔", ""), NameUtil.Gender.Female, null,
                CultivationSetting.PersonFixedInfoMix(null, null, 8000, 80000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 4
        updateBossProps(person, 10, Pair(6, 2), 75,2000, 120, listOf("8004002", "8002002"))
        return person
    }

}