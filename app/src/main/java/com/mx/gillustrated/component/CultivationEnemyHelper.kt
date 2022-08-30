package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Enemy
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*

@SuppressLint("SetTextI18n")
object CultivationEnemyHelper {

    fun generateEnemy(type:Int): Enemy {
        val basis = type + 1
        val random = Random()
        val enemy = Enemy()
        enemy.id = UUID.randomUUID().toString()
        enemy.seq = CultivationHelper.mBattleRound.enemy[type]
        enemy.name = "${CultivationHelper.EnemyNames[type]}${enemy.seq}号"
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

    fun generateLiYuanBa(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("李", "\u5143\u9738"), NameUtil.Gender.Male, 20000, null, false,
                CultivationHelper.PersonFixedInfoMix(null, null, 1000, 1000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.teji.addAll(listOf("8001004", "8001006", "8002001", "8002003", "8002008", "8003002", "8003007"))
        person.equipmentList.addAll(listOf(Triple("7002802", 0, "")))
        repeat(4) {
            person.followerList.add(Triple("9000002", "${it + 1}号", ""))
            if(it == 0)
                person.followerList.add(Triple("9000003", "${it + 1}号", ""))
        }
        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, 50)
        person.HP = 1200
        person.maxHP = 1200
        person.type = 1
        person.remainHit = 60
        return person
    }

    fun generateShadowMao(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("毛", "\u6b23(\u6697\u5f71)"), NameUtil.Gender.Male, 20000, null, false,
                CultivationHelper.PersonFixedInfoMix(null, null, 4000, 4000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.teji.addAll(listOf("8001005", "8001006", "8002002", "8002004", "8002009", "8003002", "8003007"))//必连
        person.equipmentList.addAll(listOf(Triple("7002801", 0, "")))
        repeat(5) {
            person.followerList.add(Triple("9000002", "${it + 1}号", ""))
            if(it == 0)
                person.followerList.add(Triple("9000003", "${it + 1}号", ""))
        }
        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, 60)
        person.HP = 1400
        person.maxHP = 1400
        person.type = 2
        person.remainHit = 80
        return person
    }

    fun generateShadowQiu(alliance: Alliance): Person {
        val person = CultivationHelper.getPersonInfo(Pair("\u7403\u7403", "(\u6eda\u5706)"), NameUtil.Gender.Female, 20000, null, false,
                CultivationHelper.PersonFixedInfoMix(null, null, 4000, 4000))
        CultivationHelper.joinFixedAlliance(person, alliance)
        person.teji.addAll(listOf("8001005", "8001006", "8003002", "8004001"))//必
        person.equipmentList.addAll(listOf(Triple("7002801", 0, "")))
        repeat(6) {
            person.followerList.add(Triple("9000002", "${it + 1}号", ""))
            if(it == 0)
                person.followerList.add(Triple("9000003", "${it + 1}号", ""))
        }

        CultivationHelper.updatePersonEquipment(person)
        CultivationHelper.setPersonJingjie(person, 50)
        person.HP = 1600
        person.maxHP = 1600
        person.type = 3
        person.remainHit = 100
        return person
    }

}