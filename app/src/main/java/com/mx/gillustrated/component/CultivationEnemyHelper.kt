package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    fun generateBoss(mAlliance:ConcurrentHashMap<String, Alliance>):Person{
        return when (Random().nextInt(10)) { // 1 2 3 4 = 10
            in 0 .. 3 -> generateLiYuanBa(mAlliance["6000101"]!!)
            in 4 .. 6 -> generateShadowMao(mAlliance["6000104"]!!)
            in 7 .. 8  -> generateShadowQiu(mAlliance["6000105"]!!)
            else -> generateYaoWang(mAlliance["6000102"]!!)
        }
    }

    //jingJieLevel  total 92
    private fun updateBossProps(person: Person, tejiQuantity:Int, followerQuantity:Pair<Int, Int>, jingJieLevel:Int, property:List<Int>, hit:Int, specTeji:List<String> = listOf()){
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
        person.equipmentList.addAll(listOf(Equipment("7002901"), Equipment("7003701"),
                Equipment("7004901")))//weapon 200, armor 100, belt 500
        repeat(followerQuantity.first) {
            person.followerList.add(Follower("9000101", "${it + 1}号"))
        }
        repeat(followerQuantity.second) {
            person.followerList.add(Follower("9000102", "${it + 1}号"))
        }
        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, jingJieLevel)
        person.lifetime = person.birthtime + CultivationHelper.getLifetimeBonusInitial(person, null, true)
        person.HP = property[0]
        person.maxHP = property[0]
        person.attack = property[1]
        person.defence = property[2]
        person.speed = property[3]
        person.remainHit = hit
    }

    private fun getPowerMultiple():Int{
        //千万年为单位
        return (CultivationHelper.mCurrentXun.toFloat() / 12 / 10000 / 1000).toInt()
    }

    private fun generateLiYuanBa(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("李", "\u5143\u9738"), NameUtil.Gender.Male,null,
                CultivationSetting.PersonFixedInfoMix(null, null, 2000, 200000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 1
        val multi = getPowerMultiple()
        updateBossProps(person, 4, Pair(4, 1), 60,
            listOf(1500 + 10 * multi, 100 + multi, 50, 100 + multi), 50, listOf("8001004", "8002001"))
        return person
    }

    private fun generateShadowMao(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("毛", "\u6b23(\u6697\u5f71)"), NameUtil.Gender.Male,null,
                CultivationSetting.PersonFixedInfoMix(null, null, 4000, 400000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 2
        val multi = getPowerMultiple()
        updateBossProps(person, 6, Pair(4, 2), 65,
            listOf(1800 + 20 * multi, 150 + 2 * multi, 100, 200 + 2 * multi), 60, listOf("8001005", "8002002"))
        return person
    }

    private fun generateShadowQiu(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("\u7403\u7403", "(\u5706\u6eda\u6eda)"), NameUtil.Gender.Female, null,
                CultivationSetting.PersonFixedInfoMix(null, null, 6000, 6000000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 3
        val multi = getPowerMultiple()
        updateBossProps(person, 8, Pair(6, 2), 70,
            listOf(2200 + 30 * multi, 200 + 3 * multi, 150, 300 + 3 * multi), 80, listOf("8004001", "8002002"))
        return person
    }

    private fun generateYaoWang(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("大小乔", ""), NameUtil.Gender.Female, null,
                CultivationSetting.PersonFixedInfoMix(null, null, 8000, 30000000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 4
        val multi = getPowerMultiple()
        updateBossProps(person, 10, Pair(8, 3), 75,
            listOf(2700 + 40 * multi, 250 + 4 * multi, 200, 400 + 4 * multi), 100, listOf("8004002", "8002002"))
        return person
    }

}