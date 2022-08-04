package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.Enemy
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*

object CultivationBattleHelper {

    fun battleEnemy(person: Person, enemy: Enemy, xiuwei:Int):Boolean{
        val props1 = CultivationHelper.getProperty(person)
        val battlePerson = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, person.teji)
        val battleEnemy = BattleObject(enemy.HP, enemy.maxHP, enemy.attack, enemy.defence, enemy.speed, 1)
        startBattle(battlePerson, battleEnemy,100, 1000)

        val firstWin = battlePerson.hp > 0
        if(firstWin){
            CultivationHelper.writeHistory("${person.name}(${battlePerson.hp})  ${props1[0] - battlePerson.hp}🔪${enemy.HP - battleEnemy.hp}  ${enemy.name}(${battleEnemy.hp})", person)
            person.xiuXei += xiuwei
        }else{
            CultivationHelper.writeHistory("${enemy.name}(${battleEnemy.hp}/${enemy.remainHit}-${enemy.attack}:${enemy.defence}:${enemy.speed})  ${enemy.HP - battleEnemy.hp}🔪${props1[0] - battlePerson.hp}  ${person.name}(${battlePerson.hp})", person)
            person.xiuXei -= xiuwei
        }
        person.HP = battlePerson.hp -  props1[5]
        enemy.HP = battleEnemy.hp

        return firstWin
    }

    fun battlePerson(person1: Person, person2: Person, round:Int, xiuwei:Int):Boolean{

        val props1 = CultivationHelper.getProperty(person1)
        val props2 = CultivationHelper.getProperty(person2)
        val battlePerson1 = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, person1.teji)
        val battlePerson2 = BattleObject(props2[0], props1[1], props2[2], props2[3], props2[4], 0, person2.teji)
        startBattle(battlePerson1, battlePerson2,40, round)

        val firstWin = battlePerson1.hp > battlePerson2.hp
        if(firstWin){
            CultivationHelper.writeHistory("${person1.name}(${battlePerson1.hp})  ${props1[0] - battlePerson1.hp}🔪${props2[0] - battlePerson2.hp}  ${person2.name}(${battlePerson2.hp})", person1)
            person1.xiuXei += xiuwei / 4
            person2.xiuXei -= xiuwei
        }else{
            CultivationHelper.writeHistory("${person2.name}(${battlePerson2.hp})  ${props2[0] - battlePerson2.hp}🔪${props1[0] - battlePerson1.hp}  ${person1.name}(${battlePerson1.hp})", person2)
            person2.xiuXei += xiuwei / 4
            person1.xiuXei -= xiuwei
        }
        person1.HP = battlePerson1.hp - props1[5]
        person2.HP = battlePerson2.hp - props2[5]

        return firstWin
    }

    //0: 2-3-4
    private fun startBattle(props1: BattleObject, props2: BattleObject, randomBasis:Int, round: Int){
        val random = Random()
        var loopCount = 0
        val battlePersons = mutableListOf(props1, props2)
        while (loopCount++ < round){
            preBattleEveryRound(battlePersons, loopCount)
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            val attackFirstIndex = if ( getSpeed(props1, randomBasis) > getSpeed(props2, randomBasis)) 0 else 1
            val attackLaterIndex = Math.abs(attackFirstIndex - 1)
            battlingOpponent(battlePersons[attackFirstIndex], battlePersons[attackLaterIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            battlingOpponent(battlePersons[attackLaterIndex], battlePersons[attackFirstIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
        }
    }

    private fun preBattleEveryRound(battlePersons:MutableList<BattleObject>, round:Int){
        battlePersons.forEachIndexed { index, current ->
            val opponent = battlePersons[Math.abs(index - 1)]
            if(current.kills.find { it == "8002001" } != null){
                opponent.hp -= 10
            }
            if(current.kills.find { it == "8002002" } != null){
                opponent.hp -= 20
            }
            if(current.kills.find { it == "8002003" } != null && round == 1){
                opponent.hp -= 30
            }
            if(current.kills.find { it == "8002004" } != null && round == 1){
                opponent.hp -= 50
            }
            if(current.kills.find { it == "8002005" } != null){
                current.hp += 20
                if(current.hp > current.maxhp)
                    current.hp = current.maxhp
            }

        }
    }

    //return 是否结束
    private fun afterHPChangedPossibleEveryPoint(battlePersons:MutableList<BattleObject>):Boolean{
        battlePersons.forEachIndexed { index, current ->
            val opponent = battlePersons[Math.abs(index - 1)]
            if(current.kills.find { it == "8001003" } != null && current.goneCount < 1 && current.hp <= 0){
                current.hp = 1
                current.goneCount++
            }else  if(current.kills.find { it == "8001004" } != null && current.goneCount < 2 && current.hp <= 0){
                current.hp = 1
                current.goneCount++
            }else  if(current.kills.find { it == "8001005" } != null && current.goneCount < 3 && current.hp <= 0){
                current.hp = 1
                current.goneCount++
            }
            if(current.kills.find { it == "8001002" } != null && current.hp <= 0 && opponent.type == 0){
                opponent.hp = 1
            }
        }

        if(battlePersons[0].hp <= 0 && battlePersons[1].hp <= 0){
            battlePersons[Random().nextInt(2)].hp = 1
        }

        return battlePersons[0].hp <= 0 || battlePersons[1].hp <= 0
    }

    private fun battlingOpponent(attacker: BattleObject, defender: BattleObject){
        inBattles(attacker, defender)
        if(attacker.kills.find { it == "8001001" } != null && isTrigger() && defender.hp > 0){
            inBattles(attacker, defender)
        }
    }

    private fun inBattles(attacker: BattleObject, defender: BattleObject){
        if(defender.kills.find { it == "8003001" } != null && isTrigger()){
            return
        }
        var minReduce = 1
        var attackerValue = attacker.attack
        if(attacker.kills.find { it == "8003002" } != null && isTrigger()){
            attackerValue = Math.round( attacker.attack.toFloat() * 2.0f)
        }
        if(attacker.kills.find { it == "8003003" } != null ){
            minReduce = 10
        }
        val hpReduced = Math.max(minReduce, attackerValue - defender.defence)
        defender.hp -= hpReduced
    }

    private fun getSpeed(props:BattleObject, randomBasis:Int):Int{
        var propsSpeed = props.speed
        if(props.kills.find { it == "8003004" } != null && isTrigger()){
            propsSpeed = Math.round( props.speed.toFloat() * 2.0f)
        }
        return propsSpeed + Random().nextInt(randomBasis)

    }

    private fun isTrigger(chance:Int = 50):Boolean{
        return Random().nextInt(100) < chance
    }

    private class BattleObject(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int) {

        constructor(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int, k:MutableList<String>):this(h, m, a,d,s,t){
            kills = k
        }

        var attack: Int = a
        var defence:Int = d
        var speed:Int = s
        var hp:Int = h
        var maxhp:Int = m
        var type:Int = t
        var kills:MutableList<String> = mutableListOf()
        var goneCount:Int = 0//gone次数


    }
}