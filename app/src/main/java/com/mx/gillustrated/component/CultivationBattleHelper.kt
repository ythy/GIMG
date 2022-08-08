package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

object CultivationBattleHelper {

    var mBattles:ConcurrentHashMap<String, BattleInfo> = ConcurrentHashMap()

    fun battleEnemy(person: Person, enemy: Enemy, xiuwei:Int):Boolean{
        val props1 = CultivationHelper.getProperty(person)
        val battlePerson = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, person.teji)
        val battleEnemy = BattleObject(enemy.HP, enemy.maxHP, enemy.attack, enemy.defence, enemy.speed, 1)
        val battleId = UUID.randomUUID().toString()
        battlePerson.battleId = battleId
        battlePerson.name = person.name
        battleEnemy.battleId = battleId
        battleEnemy.name = enemy.name
        mBattles[battleId] = BattleInfo(battleId, person, null, battlePerson, battleEnemy)
        addBattleDetail(battleId, "\u6218\u6597\u5f00\u59cb")
        startBattle(battlePerson, battleEnemy,100, 1000)

        val firstWin = battlePerson.hp > 0
        if(firstWin){
            addBattleDetail(battleId, "${person.name}\u83b7\u80dc, \u6b8bHP:${battlePerson.hp}")
            CultivationHelper.writeHistory("${person.name}(${battlePerson.hp})  ${props1[0] - battlePerson.hp}🔪${enemy.HP - battleEnemy.hp}  ${enemy.name}(${battleEnemy.hp})", person, 2, battleId)
            person.xiuXei += xiuwei
        }else{
            addBattleDetail(battleId, "${enemy.name}\u83b7\u80dc, \u6b8bHP:${battleEnemy.hp}")
            CultivationHelper.writeHistory("${enemy.name}(${battleEnemy.hp}/${enemy.remainHit}-${enemy.attack}:${enemy.defence}:${enemy.speed})  ${enemy.HP - battleEnemy.hp}🔪${props1[0] - battlePerson.hp}  ${person.name}(${battlePerson.hp})", person, 2, battleId)
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
        val battlePerson2 = BattleObject(props2[0], props2[1], props2[2], props2[3], props2[4], 0, person2.teji)
        val battleId = UUID.randomUUID().toString()
        battlePerson1.battleId = battleId
        battlePerson1.name = person1.name
        battlePerson2.battleId = battleId
        battlePerson2.name = person2.name
        mBattles[battleId] = BattleInfo(battleId, person1, person2, battlePerson1, battlePerson2)
        addBattleDetail(battleId, "\u6218\u6597\u5f00\u59cb")
        startBattle(battlePerson1, battlePerson2,40, round)

        mBattles[battleId]!!.round = 0
        val firstWin = battlePerson1.hp > battlePerson2.hp
        if(firstWin){
            addBattleDetail(battleId, "${person1.name}\u83b7\u80dc, \u6b8bHP:${battlePerson1.hp}")
            CultivationHelper.writeHistory("${person1.name}(${battlePerson1.hp})  ${props1[0] - battlePerson1.hp}🔪${props2[0] - battlePerson2.hp}  ${person2.name}(${battlePerson2.hp})", person1, 2, battleId)
            person1.xiuXei += xiuwei / 4
            person2.xiuXei -= xiuwei
        }else{
            addBattleDetail(battleId, "${person2.name}\u83b7\u80dc, \u6b8bHP:${battlePerson2.hp}")
            CultivationHelper.writeHistory("${person2.name}(${battlePerson2.hp})  ${props2[0] - battlePerson2.hp}🔪${props1[0] - battlePerson1.hp}  ${person1.name}(${battlePerson1.hp})", person2, 2, battleId)
            person2.xiuXei += xiuwei / 4
            person1.xiuXei -= xiuwei
        }
        person1.HP = battlePerson1.hp - props1[5]
        person2.HP = battlePerson2.hp - props2[5]

        return firstWin
    }

    //0: 2-3-4
    private fun startBattle(props1: BattleObject, props2: BattleObject, randomBasis:Int, round: Int){
        var loopCount = 0
        val battlePersons = mutableListOf(props1, props2)
        while (loopCount++ < round){
            mBattles[props1.battleId]!!.round++
            mBattles[props1.battleId]!!.seq = 0
            preBattleEveryRound(battlePersons, loopCount)
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            val attackFirstIndex = if ( getSpeed(props1, randomBasis) > getSpeed(props2, randomBasis)) 0 else 1
            val attackLaterIndex = Math.abs(attackFirstIndex - 1)
            addBattleDetail(props1.battleId, "${battlePersons[attackFirstIndex].name}\u7684\u56de\u5408")
            battlingOpponent(battlePersons[attackFirstIndex], battlePersons[attackLaterIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            addBattleDetail(props1.battleId, "${battlePersons[attackLaterIndex].name}\u7684\u56de\u5408")
            battlingOpponent(battlePersons[attackLaterIndex], battlePersons[attackFirstIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
        }
    }

    private fun preBattleEveryRound(battlePersons:MutableList<BattleObject>, round:Int){
        val battleId = battlePersons[0].battleId
        battlePersons.forEachIndexed { index, current ->
            val opponent = battlePersons[Math.abs(index - 1)]
            if(current.kills.find { it == "8002001" } != null){
                opponent.hp -= 10
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002001").name}\u6548\u679c, ${opponent.name}HP-10")
            }
            if(current.kills.find { it == "8002002" } != null){
                opponent.hp -= 20
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002002").name}\u6548\u679c, ${opponent.name}HP-20")
            }
            if(current.kills.find { it == "8002003" } != null && round == 1){
                opponent.hp -= 30
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002003").name}\u6548\u679c, ${opponent.name}HP-30")
            }
            if(current.kills.find { it == "8002004" } != null && round == 1){
                opponent.hp -= 50
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002004").name}\u6548\u679c, ${opponent.name}HP-50")
            }
            if(current.kills.find { it == "8002006" } != null && round == 1){//weakness 20
                opponent.attack -= Math.round(opponent.attackBasis * 0.2f)
                opponent.defence -= Math.round(opponent.defenceBasis * 0.2f)
                opponent.speed -= Math.round(opponent.speedBasis * 0.2f)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002006").name}\u6548\u679c, ${opponent.name}\u865a\u5f3120%")
            }
            if(current.kills.find { it == "8002007" } != null && round == 1){//weakness 50
                opponent.attack -= Math.round(opponent.attackBasis * 0.5f)
                opponent.defence -= Math.round(opponent.defenceBasis * 0.5f)
                opponent.speed -=  Math.round(opponent.speedBasis * 0.5f)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002007").name}\u6548\u679c, ${opponent.name}\u865a\u5f3150%")
            }
            if(current.kills.find { it == "8002008" } != null && round == 1){//gain 20
                current.attack += Math.round(current.attackBasis * 0.2f)
                current.defence += Math.round(current.defenceBasis * 0.2f)
                current.speed += Math.round(current.speedBasis * 0.2f)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002008").name}\u6548\u679c, \u5c5e\u6027\u589e\u5f3a20%")
            }
            if(current.kills.find { it == "8002009" } != null && round == 1){//gain 50
                current.attack += Math.round(current.attackBasis * 0.5f)
                current.defence += Math.round(current.defenceBasis * 0.5f)
                current.speed += Math.round(current.speedBasis * 0.5f)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002009").name}\u6548\u679c, \u5c5e\u6027\u589e\u5f3a50%")
            }
            if(current.kills.find { it == "8004001" } != null && current.status.none { it in 1..100 }){
                current.attack += Math.round(current.attackBasis * 1f)
                current.defence += Math.round(current.defenceBasis * 1f)
                current.speed += Math.round(current.speedBasis * 1f)
                current.status.add(1)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8004001").name}\u53d1\u52a8, 80%\u6982\u7387\u8ffd\u51fb")
            }
            if(current.kills.find { it == "8004002" } != null && current.status.none { it in 1..100 }){
                current.speed += Math.round(current.speedBasis * 1f)
                current.status.add(2)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8004002").name}\u53d1\u52a8, 80%\u6982\u7387\u5c01\u5370")
            }
            if(current.kills.find { it == "8002005" } != null){
                current.hp += 20
                current.hp = Math.min(current.hp, current.maxhp)
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8002005").name}\u6548\u679c, HP+20")
            }
            if(current.kills.find { it == "8001007" } != null && opponent.kills.find { it == "8001006" } == null
                    && opponent.type == 0 && isTrigger(20) ){//kill immediately
                if(opponent.hp > 0) {
                    opponent.hp = 0
                    addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8001007").name}\u53d1\u52a8, HP0")
                }
            }
        }
    }

    //return 是否结束
    private fun afterHPChangedPossibleEveryPoint(battlePersons:MutableList<BattleObject>):Boolean{
        val battleId = battlePersons[0].battleId
        battlePersons.forEachIndexed { index, current ->
            val opponent = battlePersons[Math.abs(index - 1)]
            if(current.kills.find { it == "8001003" } != null && current.hp <= 0 && isTrigger(20)){
                current.hp = current.maxhp
                current.goneCount++
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8001003").name}\u53d1\u52a8, HP\u5168\u6062\u590d")
            }else if(current.kills.find { it == "8001001" } != null && current.hp <= 0 && isTrigger()){
                current.hp = 1
                current.goneCount++
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8001001").name}\u53d1\u52a8, HP1")
            }
            if(current.kills.find { it == "8001002" } != null  && current.hp <= 0
               && opponent.kills.find { it == "8001006" } == null && opponent.type == 0){
                opponent.hp = 1
                addBattleDetail(battleId, "${current.name}\u7279\u6280:${tejiDetail("8001002").name}\u53d1\u52a8, ${opponent.name}HP1")
            }
        }

        if(battlePersons[0].hp <= 0 && battlePersons[1].hp <= 0){
            battlePersons[Random().nextInt(2)].hp = 1
        }

        return battlePersons[0].hp <= 0 || battlePersons[1].hp <= 0
    }

    private fun battlingOpponent(attacker: BattleObject, defender: BattleObject){
        battleCycle(attacker, defender)
        if(attacker.kills.find { it == "8003007" } != null && defender.hp > 0 && attacker.hp > 0 && isTrigger() ){
            addBattleDetail(attacker.battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8003007").name}\u53d1\u52a8")
            battleCycle(attacker, defender)
        }
    }

    private fun battleCycle(attacker: BattleObject, defender: BattleObject){
        inBattles(attacker, defender)
        if(attacker.status.contains(1)){
            while (true){
                if(!isTrigger(80))
                    break
                if(attacker.hp <= 0 || defender.hp <= 0)
                    break
                addBattleDetail(attacker.battleId, "${attacker.name}\u8ffd\u51fb\u53d1\u52a8")
                inBattles(attacker, defender)
            }
        }
        if(defender.kills.find { it == "8003008" } != null && defender.hp > 0 && attacker.hp > 0 && isTrigger() ){//anti-attack
            addBattleDetail(defender.battleId, "${defender.name}\u7279\u6280:${tejiDetail("8003008").name}\u53d1\u52a8")
            inBattles(defender, attacker)
        }
    }

    private fun inBattles(attacker: BattleObject, defender: BattleObject){
        val battleId = attacker.battleId
        if(defender.status.contains(2) && isTrigger(80)){//封
            addBattleDetail(battleId, "${defender.name}\u5c01\u5370\u53d1\u52a8, ${attacker.name}\u65e0\u6cd5\u884c\u52a8")
            return
        }
        if(defender.kills.find { it == "8003001" } != null && isTrigger()){
            addBattleDetail(battleId, "${defender.name}\u7279\u6280:${tejiDetail("8003001").name}\u53d1\u52a8, ${attacker.name}\u653b\u51fb\u88ab\u56de\u907f")
            return
        }
        val attackerValue = attacker.attack
        var defenderValue = defender.defence
        if(attacker.kills.find { it == "8003005" } != null && isTrigger()){
            defenderValue = 0
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8003005").name}\u53d1\u52a8, ${defender.name}\u9632\u5fa10")
        }
        var attackResult = attackerValue - defenderValue
        if(attacker.kills.find { it == "8003002" } != null && attackResult > 0 && isTrigger() ){
            attackResult = Math.round( attackResult * 2.0f)
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8003002").name}\u53d1\u52a8, \u4f24\u5bb3\u7ed3\u679c2\u500d")
        }
        var minReduce = 1
        if(attacker.kills.find { it == "8003003" } != null ){
            minReduce = 10
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8003003").name}\u6548\u679c, \u6700\u5c0f\u4f24\u5bb310")
        }
        val hpReduced = Math.max(minReduce, attackResult)

        var extraReduce = 0
        if(attacker.kills.find { it == "8001005" } != null ){
            extraReduce += 40
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8001005").name}\u6548\u679c, \u9644\u52a0\u4f24\u5bb340")
        }
        if(attacker.kills.find { it == "8001004" } != null ){
            extraReduce += 20
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8001004").name}\u6548\u679c, \u9644\u52a0\u4f24\u5bb320")
        }
        defender.hp -= hpReduced + extraReduce
        addBattleDetail(battleId, "${attacker.name}\u7684\u653b\u51fb，${defender.name}HP-${hpReduced + extraReduce}")

        if(defender.kills.find { it == "8003009" } != null && attacker.type == 0 && attacker.kills.find { it == "8001006" } == null && isTrigger() ){// anti-shake
            val antiValue = Math.round( hpReduced * 0.5f)
            attacker.hp -= antiValue
            addBattleDetail(battleId, "${defender.name}\u7279\u6280:${tejiDetail("8003009").name}\u53d1\u52a8, ${attacker.name}HP-$antiValue")
        }
        if(attacker.kills.find { it == "8003006" } != null ){
            val xiValue = Math.round( hpReduced * 0.5f)
            attacker.hp += xiValue
            attacker.hp = Math.min(attacker.hp, attacker.maxhp)
            addBattleDetail(battleId, "${attacker.name}\u7279\u6280:${tejiDetail("8003006").name}\u53d1\u52a8, HP+$xiValue")
        }
    }

    private fun getSpeed(props:BattleObject, randomBasis:Int):Int{
        var propsSpeed = props.speed
        if(props.kills.find { it == "8003004" } != null && isTrigger()){
            propsSpeed = Math.round( props.speed.toFloat() * 2.0f)
            addBattleDetail(props.battleId, "${props.name}\u7279\u6280:${tejiDetail("8003004").name}\u53d1\u52a8, \u901f\u5ea6\u63d0\u5347\u4e3a$propsSpeed")
        }
        return propsSpeed + Random().nextInt(randomBasis)

    }

    private fun addBattleDetail(id:String, content:String){
        val battleInfo =  mBattles[id]!!
        battleInfo.seq++
        battleInfo.details.add(BattleInfoSeq(battleInfo.round, battleInfo.seq, content))
    }

    private fun tejiDetail(id:String):TeJi{
        return CultivationHelper.mConfig.teji.find { it.id == id }!!
    }

    private fun isTrigger(chance:Int = 50):Boolean{
        return Random().nextInt(100) < chance
    }

    class BattleObject(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int) {

        constructor(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int, k:MutableList<String>):this(h, m, a,d,s,t){
            kills = k
        }

        var attack: Int = a
        var defence:Int = d
        var speed:Int = s
        val attackBasis:Int = a
        val defenceBasis:Int = d
        val speedBasis:Int = s
        var hp:Int = h
        val hpBasis:Int = h
        val maxhp:Int = m
        val type:Int = t
        var kills:MutableList<String> = mutableListOf()
        var goneCount:Int = 0//gone次数
        var status:MutableList<Int> = mutableListOf() // 1 ~ 100，变
        var battleId:String = ""
        var name: String = ""
    }
}