package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Enemy
import com.mx.gillustrated.vo.cultivation.JingJie
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*

@SuppressLint("SetTextI18n")
object CultivationEnemyHelper {

    //暂时弃用
    fun generateEnemy(type:Int): Enemy {
        val basis = type + 1
        val random = Random()
        val enemy = Enemy()
        enemy.id = UUID.randomUUID().toString()
        enemy.seq = CultivationHelper.mBattleRound.enemy[type]
        enemy.name = "${CultivationSetting.EnemyNames[type]}${enemy.seq}号"
        enemy.type = type
        enemy.birthDay = CultivationHelper.mCurrentXun
        enemy.HP = 10 + 10 * random.nextInt(50 * basis)
        enemy.maxHP = enemy.HP
        enemy.attack = 100 * basis + 10 * random.nextInt(20 * basis)
        enemy.defence = 10 + 1 * random.nextInt(20 * basis)
        enemy.speed = 10 + 1 * random.nextInt(40 * basis)
        enemy.attackFrequency = 10 + 10 * random.nextInt(10) // max 100
        enemy.maxHit = 50 + random.nextInt(51)
        enemy.remainHit = enemy.maxHit
        for (index in 1 until basis * 4 + 1){
            val follower = CultivationHelper.mConfig.follower.find { f-> f.id == "9000007" }!!.copy()
            follower.uniqueName = "${index}号"
            enemy.followerList.add(follower)
        }
        if (type >= 2){
            for (index in 1 until basis + 1){
                val follower = CultivationHelper.mConfig.follower.find { f-> f.id == "9000008" }!!.copy()
                follower.uniqueName = "${index}号"
                enemy.followerList.add(follower)
            }
        }
        return enemy
    }

    private fun updateBossProps(person: Person, tejiQuantity:Int, followerQuantity:Pair<Int, Int>, jingJieLevel:Int, hp:Int, hit:Int, specTeji:List<String> = listOf()){
        val tejiList = CultivationHelper.mConfig.teji.filter { it.type != 4 }.shuffled()
        person.teji.addAll(tejiList.subList(0, tejiQuantity).map { it.id })
        if(!person.teji.contains("8001006")){//神迹
            person.teji.add("8001006")
        }
        specTeji.forEach {
            if(!person.teji.contains(it)){
                person.teji.add(it)
            }
        }
        person.equipmentListPair.addAll(listOf(Pair("7002901", 0)))
        repeat(followerQuantity.first) {
            person.followerList.add(Triple("9000007", "${it + 1}号", ""))
        }
        repeat(followerQuantity.second) {
            person.followerList.add(Triple("9000008", "${it + 1}号", ""))
        }
        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, jingJieLevel)
        person.HP = hp
        person.maxHP = hp
        person.remainHit = hit
    }

    fun generateLiYuanBa(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("李", "\u5143\u9738"), NameUtil.Gender.Male, 20000, null, false,
                CultivationSetting.PersonFixedInfoMix(null, null, 1000, 1000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 1
        updateBossProps(person, 4, Pair(4, 1), 50,1200, 60, listOf("8001004", "8002001"))
        return person
    }

    fun generateShadowMao(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("毛", "\u6b23(\u6697\u5f71)"), NameUtil.Gender.Male, 20000, null, false,
                CultivationSetting.PersonFixedInfoMix(null, null, 4000, 4000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 2
        updateBossProps(person, 6, Pair(4, 1), 55,1400, 80, listOf("8001005", "8002002"))
        return person
    }

    fun generateShadowQiu(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("\u7403\u7403", "(\u5706\u6eda\u6eda)"), NameUtil.Gender.Female, 20000, null, false,
                CultivationSetting.PersonFixedInfoMix(null, null, 6000, 6000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 3
        updateBossProps(person, 8, Pair(4, 1), 60,1600, 100, listOf("8004001", "8002002"))
        return person
    }

    fun generateYaoWang(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("王", "一"), NameUtil.Gender.Female, 20000, null, false,
                CultivationSetting.PersonFixedInfoMix(null, null, 8000, 80000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.type = 4
        updateBossProps(person, 10, Pair(6, 2), 60,2000, 120, listOf("8004002", "8002002"))
        return person
    }

}