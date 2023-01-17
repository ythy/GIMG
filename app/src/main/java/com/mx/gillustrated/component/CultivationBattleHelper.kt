package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import com.mx.gillustrated.component.CultivationHelper.mConfig

object CultivationBattleHelper {

    var mBattles:ConcurrentHashMap<String, BattleInfo> = ConcurrentHashMap()

    // allPersons pass null if partner don`t join battle
    fun battlePerson(allPersons:ConcurrentHashMap<String, Person>?, person1: Person, person2: Person, round:Int):Boolean{

        val props1 = CultivationHelper.getProperty(person1)
        val props2 = CultivationHelper.getProperty(person2)
        val battlePerson1 = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, getAllTeji(person1))
        val battlePerson2 = BattleObject(props2[0], props2[1], props2[2], props2[3], props2[4], 0, getAllTeji(person2))
        val battleId = UUID.randomUUID().toString()
        battlePerson1.battleId = battleId
        replenishInfo(allPersons, person1, battlePerson1)
        battlePerson2.battleId = battleId
        replenishInfo(allPersons, person2, battlePerson2)
        mBattles[battleId] = BattleInfo(battleId, person1, person2, battlePerson1, battlePerson2)
        addBattleDetail(battleId, "\u6218\u6597\u5f00\u59cb")
        val firstList = mutableListOf(battlePerson1)
        firstList.addAll(battlePerson1.follower)
        val secondList = mutableListOf(battlePerson2)
        secondList.addAll(battlePerson2.follower)
        startBattle(firstList, secondList,100, round)

        mBattles[battleId]!!.round = 0
        val firstWin = battlePerson1.hp > battlePerson2.hp
        if(firstWin){
            mBattles[battleId]!!.winnerName = person1.name
            mBattles[battleId]!!.looserName = person2.name
            printBattleInfo(battleId, battlePerson1, 0, "\u6b8bHP:${battlePerson1.hp}")
            CultivationHelper.writeHistory("${person1.name}(${battlePerson1.hp})  ${props1[0] - battlePerson1.hp}🔪${props2[0] - battlePerson2.hp}  ${person2.name}(${battlePerson2.hp})", battleId)
        }else{
            mBattles[battleId]!!.winnerName = person2.name
            mBattles[battleId]!!.looserName = person1.name
            printBattleInfo(battleId, battlePerson2, 0, "\u6b8bHP:${battlePerson2.hp}")
            CultivationHelper.writeHistory("${person2.name}(${battlePerson2.hp})  ${props2[0] - battlePerson2.hp}🔪${props1[0] - battlePerson1.hp}  ${person1.name}(${battlePerson1.hp})", battleId)
        }
        person1.HP = battlePerson1.hp - props1[5]
        person2.HP = battlePerson2.hp - props2[5]

        return firstWin
    }

    //0: 2-3-4
    private fun startBattle(props1: MutableList<BattleObject>, props2: MutableList<BattleObject>, randomBasis:Int, round: Int){
        var loopCount = 0
        val battleId = props1[0].battleId
        val battlePersons = mutableListOf(props1, props2)
        preBattleRound(battlePersons)
        if(afterHPChangedPossibleEveryPoint(battlePersons))
            return
        while (loopCount++ < round){
            mBattles[battleId]!!.round++
            mBattles[battleId]!!.seq = 0
            preBattleEveryRound(battlePersons)
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            val attackFirstIndex = if ( getSpeed(props1, randomBasis) > getSpeed(props2, randomBasis)) 0 else 1
            val attackLaterIndex = Math.abs(attackFirstIndex - 1)
            printBattleInfo(battleId, getKeyBattleObject(battlePersons[attackFirstIndex]), 6, "")
            battlingOpponent(battlePersons[attackFirstIndex], battlePersons[attackLaterIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            printBattleInfo(battleId, getKeyBattleObject(battlePersons[attackLaterIndex]), 6, "")
            battlingOpponent(battlePersons[attackLaterIndex], battlePersons[attackFirstIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            //回合最后 处理状态轮数
            battlePersons.forEach { l-> l.forEach { p->
                p.statusRound.forEach { m->
                    if(m.value > 0){
                        p.statusRound[m.key] = m.value - 1
                    }
                }
            }}
        }
    }

    private fun preBattleRound(battlePersons:MutableList<MutableList<BattleObject>>){
        val battleId = battlePersons[0][0].battleId
        battlePersons.forEachIndexed { index, currentList ->
            val allOpponent = battlePersons[Math.abs(index - 1)]
            currentList.forEach { current ->
                if (hasTeji("8003003", current)) {
                    current.minDamage = tejiDetail("8003003").power
                }
                if (hasTeji("8001008", current)) {
                    current.maxInjure = tejiDetail("8001008").power
                }
                if (hasTeji("8001005", current)) {
                    current.extraDamage += tejiDetail("8001005").power
                    printBattleInfo(battleId, current, 1, "\u9644\u52a0\u4f24\u5bb3", "8001005")
                } else if (hasTeji("8001004", current)) {
                    current.extraDamage += tejiDetail("8001004").power
                    printBattleInfo(battleId, current, 1, "\u9644\u52a0\u4f24\u5bb3", "8001004")
                }
                if(hasTeji("8002004", current)){
                    allOpponent.forEach { opponent ->
                        opponent.hp -= tejiDetail("8002004").power
                    }
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002004")
                }else if(hasTeji("8002003", current)){
                    allOpponent.forEach { opponent ->
                        opponent.hp -= tejiDetail("8002003").power
                    }
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002003")
                }
                if(hasTeji("8002007", current)){//weakness 50
                    allOpponent.forEach { opponent ->
                        val multi = tejiDetail("8002007").power.toFloat() / 100
                        opponent.attack -= Math.round(opponent.attackBasis * multi)
                        opponent.defence -= Math.round(opponent.defenceBasis * multi)
                        opponent.speed -=  Math.round(opponent.speedBasis * multi)
                    }
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u865a\u5f31", "8002007")
                }else if(hasTeji("8002006", current)){//weakness 20
                    allOpponent.forEach { opponent->
                        val multi = tejiDetail("8002006").power.toFloat() / 100
                        opponent.attack -= Math.round(opponent.attackBasis * multi)
                        opponent.defence -= Math.round(opponent.defenceBasis * multi)
                        opponent.speed -= Math.round(opponent.speedBasis * multi)
                    }
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u865a\u5f31", "8002006")
                }
                if(hasTeji("8002009", current)){//gain 50
                    val multi = tejiDetail("8002009").power.toFloat() / 100
                    current.attack += Math.round(current.attackBasis * multi)
                    current.defence += Math.round(current.defenceBasis * multi)
                    current.speed += Math.round(current.speedBasis * multi)
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8002009")
                }else if(hasTeji("8002008", current)){//gain 20
                    val multi = tejiDetail("8002008").power.toFloat() / 100
                    current.attack += Math.round(current.attackBasis * multi)
                    current.defence += Math.round(current.defenceBasis * multi)
                    current.speed += Math.round(current.speedBasis * multi)
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8002008")
                }
                if(hasTeji("8004001", current)){
                    val multi = tejiDetail("8004001").power.toFloat() / 100
                    current.attack += Math.round(current.attackBasis * multi)
                    current.defence += Math.round(current.defenceBasis * multi)
                    current.speed += Math.round(current.speedBasis * multi)
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004001")
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8004001", status)
                }
                if(hasTeji("8004002", current)){
                    val multi = tejiDetail("8004002").power.toFloat() / 100
                    current.speed += Math.round(current.speedBasis * multi)
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u9644\u52A0\u72B6\u6001${statusDetail(tejiDetail("8004002").status).name}, \u81EA\u8EAB\u901F\u5EA6\u5F3A\u5316", "8004002")
                    allOpponent.forEach { opponent->
                        addStatus(current, opponent, "8004002")
                    }
                }
                if(hasTeji("8004003", current)){
                    val multi = tejiDetail("8004003").power.toFloat() / 100
                    current.speed += Math.round(current.speedBasis * multi)
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u9644\u52A0\u72B6\u6001${statusDetail(tejiDetail("8004003").status).name}, \u81EA\u8EAB\u901F\u5EA6\u5F3A\u5316", "8004003")
                    allOpponent.forEach { opponent->
                        addStatus(current, opponent, "8004003")
                    }
                }
            }
        }
    }

    private fun preBattleEveryRound(battlePersons:MutableList<MutableList<BattleObject>>){
        val battleId = battlePersons[0][0].battleId
        battlePersons.forEachIndexed { index, currentList ->
            val allOpponent = battlePersons[Math.abs(index - 1)]
            currentList.forEach { current ->
                if(hasTeji("8002002", current)){
                    allOpponent.forEach { opponent ->
                        opponent.hp -= tejiDetail("8002002").power
                    }
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002002")
                }else if(hasTeji("8002001", current)){
                    allOpponent.forEach { opponent ->
                        opponent.hp -= tejiDetail("8002001").power
                    }
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002001")
                }
                if(hasTeji("8002005", current)){
                    current.hp += tejiDetail("8002005").power
                    current.hp = Math.min(current.hp, current.maxhp)
                    printBattleInfo(battleId, current, 1, "HP+", "8002005")
                }
                if(isStatuTrigger(current, "8100003")){
                    val multi = statusDetail("8100003").power.toFloat() / 100
                    val reduced = Math.round( current.hp * multi)
                    current.hp -= reduced
                    printBattleInfo(battleId, current, 4, "${statusDetail("8100003").name}\u53d1\u52a8 , HP-$reduced")
                }
                val opponent8001007 = getBattleObject(allOpponent)
                if(hasTeji("8001007", current) && current.hp > 0 && isTrigger(tejiDetail("8001007").chance, current, opponent8001007) ){//kill immediately
                    if(opponent8001007.hp > 0) {
                        opponent8001007.hp = 0
                        printBattleInfo(battleId, current, 3, "${showName(opponent8001007)}HP0", "8001007")
                    }
                }
            }
        }
    }

    //return 是否结束
    private fun afterHPChangedPossibleEveryPoint(battlePersons:MutableList<MutableList<BattleObject>>):Boolean{
        val battleId = battlePersons[0][0].battleId
        val round = getRound(battleId)
        battlePersons.forEachIndexed { index, currentList ->
            val opponentList = battlePersons[Math.abs(index - 1)]
            currentList.forEach { current ->
                if(hasTeji("8001003", current) && current.hp <= 0 && isTrigger(tejiDetail("8001003").chance, current)){
                    current.hp = current.maxhp
                    current.goneCount++
                    printBattleInfo(battleId, current, 3, "HP\u5168\u6062\u590d", "8001003")
                }else if(hasTeji("8001001", current) && current.hp <= 0 && isTrigger(tejiDetail("8001001").chance, current)){
                    current.hp = 1
                    current.goneCount++
                    printBattleInfo(battleId, current, 3, "HP1", "8001001")
                }
                val opponent8001002 = getBattleObject(opponentList)
                if(hasTeji("8001002", current) && current.hp <= 0 && isTrigger(tejiDetail("8001002").chance, current, opponent8001002)){
                    opponent8001002.hp = 1
                    printBattleInfo(battleId, current, 3, "${showName(opponent8001002, round > 0)}HP1", "8001002")
                }
            }
        }

        if(getKeyBattleObject(battlePersons[0]).hp <= 0 && getKeyBattleObject(battlePersons[1]).hp <= 0){
            getKeyBattleObject(battlePersons[Random().nextInt(2)]).hp = 1
        }

        battlePersons.forEach { b->
           b.forEach {
               if(it.type >= 2 && it.hp <= 0 && !it.isDead){
                   it.isDead = true
                   if(it.followerReference != null)
                       it.followerReference!!.isDead = true
                   printBattleInfo(battleId, it, 7, "")
               }
           }
        }

        return getKeyBattleObject(battlePersons[0]).hp <= 0 || getKeyBattleObject(battlePersons[1]).hp <= 0
    }

    private fun battlingOpponent(attackerList: MutableList<BattleObject>, defenderList: MutableList<BattleObject>){
        attackerList.filter { it.hp > 0 }.forEach { attacker ->
            val defender1 = getBattleObject(defenderList)
            battleCycle(attacker, defender1)
            val defender2 = getBattleObject(defenderList)
            if(hasTeji("8003007", attacker) && defender2.hp > 0 && attacker.hp > 0 && isTrigger(tejiDetail("8003007").chance, attacker) ){
                printBattleInfo(attacker.battleId, attacker, 3, "", "8003007")
                battleCycle(attacker, defender2)
            }
        }
    }

    private fun battleCycle(attacker: BattleObject, defender: BattleObject){
        inBattles(attacker, defender)
        while (isStatuTrigger(attacker, "8100001")){
            if(attacker.hp <= 0 || defender.hp <= 0)
                break
            printBattleInfo(attacker.battleId, attacker, 4, "${statusDetail("8100001").name}\u53d1\u52a8")
            inBattles(attacker, defender)
        }
        if(hasTeji("8003008", defender) && defender.hp > 0 && attacker.hp > 0 && isTrigger(tejiDetail("8003008").chance, defender) ){//anti-attack
            printBattleInfo(defender.battleId, defender, 3, "", "8003008")
            inBattles(defender, attacker)
        }
    }

    private fun inBattles(attacker: BattleObject, defender: BattleObject){
        if(attacker.hp <= 0 || defender.hp <= 0)
            return
        val battleId = attacker.battleId
        if(isStatuTrigger(attacker, "8100002")){//封
            printBattleInfo(battleId, attacker, 4, "${statusDetail("8100002").name}\u53d1\u52a8, \u65e0\u6cd5\u884c\u52a8")
            return
        }
        if(hasTeji("8003001", defender) && isTrigger(tejiDetail("8003001").chance, defender)){
            printBattleInfo(battleId, defender, 3, "${showName(attacker)}\u653b\u51fb\u88ab\u56de\u907f", "8003001")
            return
        }
        val attackerValue = getBattleValue(attacker.attack, attacker)
        var defenderValue = getBattleValue(defender.defence, defender)
        val triggerBaseList:MutableList<String> = mutableListOf()
        if(hasTeji("8003005", attacker) && isTrigger(tejiDetail("8003005").chance, attacker)){
            defenderValue = 0
            triggerBaseList.add(tejiDetail("8003005").name)
        }
        var attackResult = attackerValue - defenderValue
        if(hasTeji("8003002", attacker) && attackResult > 0 && isTrigger(tejiDetail("8003002").chance, attacker) ){
            val multi = tejiDetail("8003002").power.toFloat() / 100
            attackResult = Math.round( attackResult * multi)
            triggerBaseList.add(tejiDetail("8003002").name)
        }
        val hpReduced = Math.min(defender.maxInjure, Math.max(attacker.minDamage, attackResult))
        defender.hp -= hpReduced + attacker.extraDamage
        printBattleInfo(battleId, attacker, 5, "${triggerBaseList.joinToString()} $attackerValue - $defenderValue ${showName(defender)}HP-${hpReduced + attacker.extraDamage}")

        magicInBattle(battleId, attacker, defender)

        if(hasTeji("8003009", defender) && isTrigger(tejiDetail("8003009").chance, defender, attacker) ){// anti-shake
            val multi = tejiDetail("8003009").power.toFloat() / 100
            val antiValue = Math.round( hpReduced * multi)
            attacker.hp -= antiValue
            printBattleInfo(battleId, defender, 3, "${showName(attacker)}HP-$antiValue", "8003009")
        }
        if(hasTeji("8003006", attacker) && isTrigger(tejiDetail("8003006").chance, attacker )){
            val multi = tejiDetail("8003006").power.toFloat() / 100
            val xiValue = Math.round( hpReduced * multi)
            attacker.hp += xiValue
            attacker.hp = Math.min(attacker.hp, attacker.maxhp)
            printBattleInfo(battleId, attacker, 3, "HP+$xiValue", "8003006")
        }
    }

    //zhao shi
    private fun magicInBattle(battleId:String, attacker:BattleObject, defender: BattleObject){
        mutableListOf("8006001", "8006003", "8006004", "8006006", "8006008", "8006009").forEach {
            if(hasTeji(it, attacker) && isTrigger(tejiDetail(it, attacker).chance, attacker) ){
                defender.hp -= tejiDetail(it, attacker).power
                printBattleInfo(battleId, attacker, 1, "${showName(defender)}HP-", it)
            }
        }
        if(hasTeji("8006002", attacker) && isTrigger(tejiDetail("8006002").chance, attacker) ){
            defender.hp -= tejiDetail("8006002").power
            val extraPower = tejiDetail("8006002").extraPower
            defender.speed -= extraPower[3]
            defender.speed = Math.max(1, defender.speed)
            printBattleInfo(battleId, attacker, 1, "${showName(defender)} SPEED-${extraPower[3]},HP-", "8006002")
        }
        if(hasTeji("8006005", attacker) && isTrigger(tejiDetail("8006005").chance, attacker) ){
            defender.hp -= tejiDetail("8006005").power
            val status = addStatus(attacker, defender, "8006005")
            printBattleInfo(battleId, attacker, 1, "${showName(defender)}HP-", "8006005", status)
        }
        if(hasTeji("8006007", attacker) && isTrigger(tejiDetail("8006007").chance, attacker) ){
            defender.hp -= tejiDetail("8006007").power
            val extraPower = tejiDetail("8006007").extraPower
            defender.speed -= extraPower[3]
            defender.defence -= extraPower[2]
            defender.speed = Math.max(1, defender.speed)
            defender.defence = Math.max(1, defender.defence)
            printBattleInfo(battleId, attacker, 1, "${showName(defender)} SPEED-${extraPower[3]} DEF-${extraPower[2]}, HP-", "8006007")
        }
    }

    private fun getSpeed(propsList:MutableList<BattleObject>, randomBasis:Int):Int{
        val speedList = propsList.map { props->
            var propsSpeed = props.speed
            if(hasTeji("8003004", props) && isTrigger(tejiDetail("8003004").chance, props)){
                val multi = tejiDetail("8003004").power.toFloat() / 100
                propsSpeed = Math.round( props.speed.toFloat() * multi)
                printBattleInfo(props.battleId, props, 3, "\u901f\u5ea6\u63d0\u5347\u4e3a$propsSpeed", "8003004")
            }
            propsSpeed
        }
        return Math.round((speedList.maxBy { it } ?: 1).toFloat() *  (100 - Random().nextInt(50)) / 100) + Random().nextInt(randomBasis)
    }

    private fun getBattleValue(origin:Int, person:BattleObject):Int{
        return if(hasTeji("8003010", person)){
            origin
        }else{
            val calculatorValue =  origin.toFloat() * (100 - Random().nextInt(50)) / 100
            Math.max(1, Math.round(calculatorValue))
        }
    }

    private fun getBattleObject(list: MutableList<BattleObject>):BattleObject{
        return if(list.size == 1)
            list[0]
        else{
            val live = list.filter { it.hp >0 }
            if (live.isEmpty()) getKeyBattleObject(list) else live.shuffled()[0]
        }
    }

    private fun getKeyBattleObject(list: MutableList<BattleObject>):BattleObject{
        return if(list.size == 1)
            list[0]
        else{
            list.find { it.type < 2 }!!
        }
    }


    private fun getRound(id:String):Int{
        return  mBattles[id]!!.round
    }

    private fun addStatus(current:BattleObject, opponent:BattleObject, tejiId:String):Status?{
        val teji = tejiDetail(tejiId)
        if(teji.status != ""){
            val status = statusDetail(teji.status)
            if(status.target == 0){
                current.statusRound[status.id] = current.statusRound[status.id] ?: 0 + teji.statusRound
                return status
            }else if(status.target == 1){
                opponent.statusRound[status.id] = opponent.statusRound[status.id] ?: 0 + teji.statusRound
                return status
            }
        }
        return null
    }

    private fun isStatusExpire (person: BattleObject, id:String):Boolean{
        return  person.statusRound[id] == null || person.statusRound[id] == 0
    }

    private fun remainStatusRound (person: BattleObject, id:String):Int{
        return person.statusRound[id] ?: 0
    }

    private fun isStatuTrigger (person: BattleObject, statusId:String):Boolean{
        val status = statusDetail(statusId)
        return !isStatusExpire(person, statusId) && isTrigger(status.chance)
    }

    //type 1,2,3 需要teji, 4 显示, 5 gongji 6 round 7 jidao
    private fun printBattleInfo(battleId: String, attacker: BattleObject, type:Int, content: String, teji:String? = null, status:Status? = null){
        val nameSuffix = when(type){
            0 -> "\u83b7\u80dc"
            1,2,3-> "\u53D1\u52A8${tejiDetail(teji!!, attacker).name}${if(status != null) ",\u9644\u52A0\u72B6\u6001${status.name}" else ""}"
            4 -> ""
            5 -> "\u7684\u653B\u51FB"
            6 -> "\u7684\u56DE\u5408"
            7 -> "\u88ab\u51fb\u5012"
            else -> ""
        }
        val contentSuffix = when(type){
            0,3,4,5,6,7 -> ""
            1 -> "${tejiDetail(teji!!, attacker).power}"
            2 -> "${tejiDetail(teji!!, attacker).power}%"
            else -> ""
        }
        val gap = if(content == "") "" else ", "
        addBattleDetail(battleId, "${showName(attacker,  getRound(battleId) > 0)}$nameSuffix$gap$content$contentSuffix", teji)
    }

    private fun addBattleDetail(id:String, content:String, teji:String? = null){
        val battleInfo =  mBattles[id]!!
        battleInfo.seq++
        if(teji == null)
            battleInfo.details.add(BattleInfoSeq(battleInfo.round, battleInfo.seq, content))
        else
            battleInfo.details.add(BattleInfoSeq(battleInfo.round, battleInfo.seq, content, teji))
    }

    private fun replenishInfo(allPersons:ConcurrentHashMap<String, Person>?, person: Person, battlePerson:BattleObject){
        battlePerson.name = person.name
        battlePerson.attackBasis -= person.equipmentProperty[1]
        battlePerson.defenceBasis -= person.equipmentProperty[2]
        battlePerson.speedBasis -= person.equipmentProperty[3]

        if(person.followerList.isNotEmpty()){
            battlePerson.follower.addAll(person.followerList.map { follower->
                val props = follower.property
                val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.teji.map { f-> convertTejiObject(f) }.toMutableList())
                result.name = "${person.name}-${follower.name}${follower.uniqueName}"
                result.battleId = battlePerson.battleId
                result
            })
        }
        person.equipmentList.forEach {
            it.follower.forEach { id->
                val follower = mConfig.follower.find { f-> f.id == id }!!.toFollower()
                val props = follower.property
                val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.teji.map { f-> convertTejiObject(f) }.toMutableList())
                result.name = "${person.name}-${follower.name}"
                result.battleId = battlePerson.battleId
                battlePerson.follower.add(result)
            }
        }
        person.label.forEach {
            val label = mConfig.label.find { f-> f.id == it }!!.copy()
            label.follower.forEach { id->
                val follower = mConfig.follower.find { f-> f.id == id }!!.toFollower()
                val props = follower.property
                val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.teji.map { f-> convertTejiObject(f) }.toMutableList())
                result.name = "${person.name}-${follower.name}"
                result.battleId = battlePerson.battleId
                battlePerson.follower.add(result)
            }
        }

        if(allPersons == null)
            return
        val partner = allPersons[person.partner ?: "none"]
        if(partner != null){
            val partnerProps = CultivationHelper.getProperty(partner)
            val partnerBattleObject = BattleObject(partnerProps[0], partnerProps[0], partnerProps[1], partnerProps[2], partnerProps[3],
                    3, getAllTeji(partner))
            partnerBattleObject.name = "${person.name}-${partner.name}"
            partnerBattleObject.battleId = battlePerson.battleId
            battlePerson.follower.add(partnerBattleObject)
        }
    }


    fun getAllTeji(person: Person):MutableList<TeJiObject>{
        val result = mutableListOf<String>()
        result.addAll(person.teji)
        person.equipmentList.map {
            if(it.type == 5){
                CultivationSetting.getEquipmentCustom(Pair(it.id, it.seq))
            }else{
                it
            }
        }.filter { it.teji.size > 0 }.forEach {
            result.addAll(it.teji)
        }
        val exclusives =  mConfig.equipment.filter { it.type == 8 && it.spec.contains(person.specIdentity) && it.teji.size > 0}
        exclusives.forEach {
            result.addAll(it.teji)
        }
        result.addAll(mConfig.teji.filter { it.type == 6 && it.spec.contains(person.specIdentity)}.map { it.id })
        person.label.map {
            mConfig.label.find { e-> e.id == it}!!.copy()
        }.filter { it.teji.isNotEmpty() }.forEach {
            result.addAll(it.teji)
        }

        return result.map {
            convertTejiObject(it, person)
        }.toMutableList()
    }

    //特技有可能包含多个名称
    fun convertTejiObject(id:String, person: Person? = null):TeJiObject{
        val tejiObject = TeJiObject(id)
        val tejiDetail = tejiDetail(id)
        tejiObject.name = tejiDetail.name
        if(person != null && tejiDetail.specName.isNotEmpty()){
            val index = tejiDetail.spec.indexOf(person.specIdentity)
            if(index < tejiDetail.specName.size){
                tejiObject.name = tejiDetail.specName[index]
            }
        }
        tejiObject.type = tejiDetail.type
        tejiObject.power = tejiDetail.power
        tejiObject.extraPower = tejiDetail.extraPower
        tejiObject.chance = tejiDetail.chance
        tejiObject.status = tejiDetail.status
        tejiObject.statusRound = tejiDetail.statusRound
        return tejiObject
    }

    fun tejiDetail(id:String):TeJi{
        return CultivationHelper.mConfig.teji.find { it.id == id }!!.toTeji()
    }

    fun tejiDetail(id:String, person:BattleObject):TeJiObject{
        return person.kills.find { it.id == id }!!
    }

    private fun statusDetail(id:String):Status{
        return CultivationHelper.mConfig.status.find { it.id == id }!!
    }

    private fun hasTeji(id: String, person:BattleObject):Boolean{
        return person.kills.find { it.id == id } != null
    }

    private fun isTrigger(chance:Int = 50, current: BattleObject? = null, opponent:BattleObject? = null):Boolean{
        val exception = if(opponent != null) opponent.type == 1 || hasTeji("8001006", opponent) else false
        val guiyi = if(current != null ) !isStatusExpire(current, "8100005") else false
        return !guiyi && !exception && ( Random().nextInt(100) < chance )
    }

    private fun showName(person:BattleObject, showStatus:Boolean = true):String{
        val status = mutableListOf<String>()
        CultivationHelper.mConfig.status.forEach {
            if(!isStatusExpire(person, it.id)){
                status.add("${it.name}${remainStatusRound(person, it.id)}")
            }
        }
        val name = if (person.type == 2) "<${person.name}>" else "<${person.name}>"
        return if (status.isEmpty() || !showStatus) name else  "$name(${status.joinToString()})"
    }

    class BattleObject(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int) {

        constructor(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int, k:MutableList<TeJiObject>):this(h, m, a,d,s,t){
            kills = k
        }

        var attack: Int = a
        var defence:Int = d
        var speed:Int = s
        var hp:Int = h

        var goneCount:Int = 0//gone次数
        var statusRound:HashMap<String, Int> = HashMap()//0 或者 null 不发动
        var extraDamage:Int = 0
        var minDamage:Int = 1
        var maxInjure:Int = 9999
        var isDead:Boolean = false

        var follower = mutableListOf<BattleObject>()
        var attackBasis:Int = a
        var defenceBasis:Int = d
        var speedBasis:Int = s
        val hpBasis:Int = h
        val maxhp:Int = m
        val type:Int = t //normal = 0, enemy = 1, follower = 2, partner = 3
        var kills:MutableList<TeJiObject> = mutableListOf()
        var battleId:String = ""
        var name: String = ""
        val attackInit:Int = a
        val defenceInit:Int = d
        val speedInit:Int = s
        var followerReference:Follower? = null
    }

    class TeJiObject(val id:String){
        lateinit var name:String
        var type:Int = 0
        var power:Int = 0
        var extraPower = mutableListOf<Int>()
        var chance:Int = 100
        var status:String = ""
        var statusRound:Int = 0 // combining with status
    }

}