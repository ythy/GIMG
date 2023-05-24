package com.mx.gillustrated.component

import android.annotation.SuppressLint
import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import com.mx.gillustrated.component.CultivationHelper.mConfig
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
object CultivationBattleHelper {

    var mBattles:ConcurrentHashMap<String, BattleInfo> = ConcurrentHashMap()
    private val BaGua = listOf("\u4E7E","\u574E","\u826E", "\u9707", "\u5DFD", "\u79BB", "\u5764", "\u5151")
    val SpecPositiveWords = listOf("\u6076\u62A5", "\u591C\u821E\u503E\u57CE", "\u820D\u8EAB\u4E00\u640F")
    val SpecNegativeWords = listOf("\u5584\u62A5")

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
        100.startBattle(firstList, secondList, round)

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
    private fun Int.startBattle(props1: MutableList<BattleObject>, props2: MutableList<BattleObject>, round: Int){
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
            val attackFirstIndex = if ( getSpeed(props1, this) > getSpeed(props2, this)) 0 else 1
            val attackLaterIndex = abs(attackFirstIndex - 1)
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
            val allOpponent = battlePersons[abs(index - 1)]
            currentList.forEach { current ->
                if (hasTeji("8005001", current)) {
                    printBattleInfo(battleId, current, 4, "${tejiDetail("8005001").name}开启", "8005001")
                }
                if (hasTeji("8003003", current)) {
                    current.minDamage = tejiDetail("8003003").power
                }
                if (hasTeji("8001008", current)) {
                    current.maxInjure = tejiDetail("8001008").power
                }
                if (hasTeji("8001005", current)) {
                    current.extraDamage += tejiDetail("8001005").power
                    printBattleInfo(battleId, current, 1, "\u9644\u52a0\u4f24\u5bb3", "8001005")
                }else if (hasTeji("8001004", current)) {
                    current.extraDamage += tejiDetail("8001004").power
                    printBattleInfo(battleId, current, 1, "\u9644\u52a0\u4f24\u5bb3", "8001004")
                }
                if(hasTeji("8004001", current)){
                    val multi = tejiDetail("8004001").power.toFloat() / 100
                    current.attack += (current.attackBasis * multi).roundToInt()
                    current.defence += (current.defenceBasis * multi).roundToInt()
                    current.speed += (current.speedBasis * multi).roundToInt()
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004001")
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8004001", status)
                }
                if(hasTeji("8002004", current)){
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002004")
                    allOpponent.forEach { opponent ->
                        if (isStatuTrigger(opponent, "8100006")){
                            printBattleInfo(battleId, current, 4, "领域无效, ${opponent.name}${statusDetail("8100006").name}中")
                        }else{
                            opponent.hp -= tejiDetail("8002004").power
                        }
                    }
                }else if(hasTeji("8002003", current)){
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002003")
                    allOpponent.forEach { opponent ->
                        if (isStatuTrigger(opponent, "8100006")){
                            printBattleInfo(battleId, current, 4, "领域无效, ${opponent.name}${statusDetail("8100006").name}中")
                        }else{
                            opponent.hp -= tejiDetail("8002003").power
                        }
                    }
                }
                if(hasTeji("8002007", current)){//weakness 50
                    allOpponent.forEach { opponent ->
                        val multi = tejiDetail("8002007").power.toFloat() / 100
                        opponent.attack -= (opponent.attackBasis * multi).roundToInt()
                        opponent.defence -= (opponent.defenceBasis * multi).roundToInt()
                        opponent.speed -= (opponent.speedBasis * multi).roundToInt()
                    }
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u865a\u5f31", "8002007")
                }else if(hasTeji("8002006", current)){//weakness 20
                    allOpponent.forEach { opponent->
                        val multi = tejiDetail("8002006").power.toFloat() / 100
                        opponent.attack -= (opponent.attackBasis * multi).roundToInt()
                        opponent.defence -= (opponent.defenceBasis * multi).roundToInt()
                        opponent.speed -= (opponent.speedBasis * multi).roundToInt()
                    }
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u865a\u5f31", "8002006")
                }
                if(hasTeji("8002009", current)){//gain 50
                    val multi = tejiDetail("8002009").power.toFloat() / 100
                    current.attack += (current.attackBasis * multi).roundToInt()
                    current.defence += (current.defenceBasis * multi).roundToInt()
                    current.speed += (current.speedBasis * multi).roundToInt()
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8002009")
                }else if(hasTeji("8002008", current)){//gain 20
                    val multi = tejiDetail("8002008").power.toFloat() / 100
                    current.attack += (current.attackBasis * multi).roundToInt()
                    current.defence += (current.defenceBasis * multi).roundToInt()
                    current.speed += (current.speedBasis * multi).roundToInt()
                    printBattleInfo(battleId, current, 2, "\u5c5e\u6027\u5F3A\u5316", "8002008")
                }

                if(hasTeji("8004004", current)){
                    val multi = tejiDetail("8004004").power.toFloat() / 100
                    current.defence += (current.defenceBasis * multi).roundToInt()
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004004")
                    printBattleInfo(battleId, current, 2, "防御\u5F3A\u5316", "8004004", status)
                }
                if(hasTeji("8004002", current)){
                    val multi = tejiDetail("8004002").power.toFloat() / 100
                    current.speed += (current.speedBasis * multi).roundToInt()
                    printBattleInfo(battleId, current, 2, "\u654C\u65B9\u5168\u4F53\u9644\u52A0\u72B6\u6001${statusDetail(tejiDetail("8004002").status).name}, \u81EA\u8EAB\u901F\u5EA6\u5F3A\u5316", "8004002")
                    allOpponent.forEach { opponent->
                        addStatus(current, opponent, "8004002")
                    }
                }
                if(hasTeji("8004003", current)){
                    val multi = tejiDetail("8004003").power.toFloat() / 100
                    current.speed += (current.speedBasis * multi).roundToInt()
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
            val allOpponent = battlePersons[abs(index - 1)]
            currentList.forEach { current ->
                if(hasTeji("8002002", current) && isTrigger(current)){
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002002")
                    allOpponent.forEach { opponent ->
                        if (isStatuTrigger(opponent, "8100006")){
                            printBattleInfo(battleId, current, 4, "领域无效, ${opponent.name}${statusDetail("8100006").name}中")
                        }else{
                            opponent.hp -= tejiDetail("8002002").power
                        }
                    }
                }else if(hasTeji("8002001", current) && isTrigger(current)){
                    printBattleInfo(battleId, current, 1, "\u654C\u65B9\u5168\u4F53HP-", "8002001")
                    allOpponent.forEach { opponent ->
                        if (isStatuTrigger(opponent, "8100006")){
                            printBattleInfo(battleId, current, 4, "领域无效, ${opponent.name}${statusDetail("8100006").name}中")
                        }else{
                            opponent.hp -= tejiDetail("8002001").power
                        }
                    }
                }
                if(hasTeji("8002005", current) && isTrigger(current)){
                    current.hp += tejiDetail("8002005").power
                    current.hp = current.hp.coerceAtMost(current.maxhp)
                    printBattleInfo(battleId, current, 1, "HP+", "8002005")
                }
                if(isStatuTrigger(current, "8100003") && !isStatuTrigger(current, "8100006")){
                    val multi = statusDetail("8100003").power.toFloat() / 100
                    val reduced = (current.hp * multi).roundToInt()
                    current.hp -= reduced
                    printBattleInfo(battleId, current, 4, "${statusDetail("8100003").name}\u53d1\u52a8 , HP-$reduced")
                }
                val opponent8001007 = getBattleObject(allOpponent)
                if(hasTeji("8001007", current) && current.hp > 0 && isTrigger(tejiDetail("8001007").chance, current, opponent8001007)
                        && !isStatuTrigger(opponent8001007, "8100006") ){//kill immediately
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
            val opponentList = battlePersons[abs(index - 1)]
            currentList.forEach { current ->
                if(hasTeji("8001003", current) && current.hp <= 0 && isTrigger(tejiDetail("8001003").chance, current)){
                    current.hp = current.maxhp
                    printBattleInfo(battleId, current, 3, "HP\u5168\u6062\u590d", "8001003")
                }else if(hasTeji("8001001", current) && current.hp <= 0 && isTrigger(tejiDetail("8001001").chance, current)){
                    current.hp = 1
                    printBattleInfo(battleId, current, 3, "HP1", "8001001")
                }else if(hasTeji("8001012", current) && current.hp <= 0 && current.goneCount <  tejiDetail("8001012").power
                        && isTrigger(tejiDetail("8001012").chance, current)){
                    current.hp = current.maxhp
                    current.goneCount++
                    printBattleInfo(battleId, current, 3, "HP\u5168\u6062\u590d", "8001012")
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
        if (isStatuTrigger(defender, "8100006")){
            printBattleInfo(attacker.battleId, attacker, 4, "攻击无效, ${defender.name}${statusDetail("8100006").name}中")
            return
        }
        inBattles(attacker, defender)
        while (isStatuTrigger(attacker, "8100001")){
            if(attacker.hp <= 0 || defender.hp <= 0)
                break
            printBattleInfo(attacker.battleId, attacker, 4, "${statusDetail("8100001").name}\u53d1\u52a8")
            inBattles(attacker, defender)
        }
        if(hasTeji("8003008", defender) && defender.hp > 0 && attacker.hp > 0 && isTrigger(tejiDetail("8003008").chance, defender, attacker) ){//anti-attack
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
        var attackerValue = getBattleValue(attacker.attack, attacker)
        var defenderValue = getBattleValue(defender.defence, defender)
        val triggerBaseList:MutableList<String> = mutableListOf()
        if(hasTeji("8003005", attacker) && isTrigger(tejiDetail("8003005").chance, attacker)){
            defenderValue = 0
            triggerBaseList.add(tejiDetail("8003005").name)
        }
        if(hasTeji("8003011", attacker) && isTrigger(attacker)){
            val multi = tejiDetail("8003011").power.toFloat() / 100
            attackerValue = (attackerValue * multi).roundToInt()
            triggerBaseList.add(tejiDetail("8003011").name)
        }
        var attackResult = attackerValue - defenderValue
        if(hasTeji("8003002", attacker) && attackResult > 0 && isTrigger(tejiDetail("8003002").chance, attacker) ){
            val multi = tejiDetail("8003002").power.toFloat() / 100
            attackResult = (attackResult * multi).roundToInt()
            triggerBaseList.add(tejiDetail("8003002").name)
        }
        var hpReduced = attackResult.coerceAtLeast(attacker.minDamage).coerceAtMost(defender.maxInjure)
        //八Gua
        if (hasTeji("8005001", defender)) {
            val door = Random().nextInt(BaGua.size)
            if (door == BaGua.size - 1){
                hpReduced *= tejiDetail("8005001").power
                triggerBaseList.add("\u8FDB\u5165${BaGua[door]}\u95E8, ${tejiDetail("8005001").power}\u500D\u4F24\u5BB3")
            }else{
                printBattleInfo(battleId, attacker, 4, "\u8FDB\u5165${tejiDetail("8005001").name}${BaGua[door]}\u95E8, \u65E0\u6CD5\u884C\u52A8", "8005001")
                return
            }
        }
        //Shan E You Bao
        if(hasTeji("8001009", attacker) && isTrigger(tejiDetail("8001009").chance, attacker) ){
            if(isTrigger(80)){
                hpReduced *= 2
                triggerBaseList.add(SpecPositiveWords[0])
            }else{
                hpReduced = -(hpReduced * 0.2f).roundToInt()
                triggerBaseList.add("${SpecNegativeWords[0]}${-hpReduced}")
            }
        }
        //Ye Wu Qing Cheng
        if(hasTeji("8001010", attacker) && isTrigger(tejiDetail("8001010").chance, attacker) ){
            val multi = tejiDetail("8001010").power.toFloat() / 100
            val extraDamage = ((attacker.attack + attacker.speed) * multi).roundToInt()
            hpReduced += extraDamage
            triggerBaseList.add("${SpecPositiveWords[1]}$extraDamage")
        }
        //she shen yi bo
        if(hasTeji("8001011", attacker) && isTrigger(tejiDetail("8001011").chance, attacker) ){
            val multi = tejiDetail("8001011").power.toFloat() / 100
            val extraDamage = (attacker.attack * multi).roundToInt()
            hpReduced += extraDamage
            val self = (attacker.maxhp * 0.2f).roundToInt()
            attacker.hp -= self
            triggerBaseList.add("${SpecPositiveWords[2]}$extraDamage HP-$self")
        }

        val finalReduced = attacker.extraDamage + hpReduced
        defender.hp -= finalReduced
        printBattleInfo(battleId, attacker, 5, "${triggerBaseList.joinToString()} ${showName(defender)}HP${if(finalReduced > 0) "-" else "+"}${abs(finalReduced)},  ${attacker.hp} vs ${defender.hp}  ")

        magicInBattle(battleId, attacker, defender)

        if(hasTeji("8003009", defender) && isTrigger(tejiDetail("8003009").chance, defender, attacker) ){// anti-shake
            val multi = tejiDetail("8003009").power.toFloat() / 100
            val antiValue = (hpReduced * multi).roundToInt()
            attacker.hp -= antiValue
            printBattleInfo(battleId, defender, 3, "${showName(attacker)}HP-$antiValue", "8003009")
        }
        if(hasTeji("8003006", attacker) && isTrigger(tejiDetail("8003006").chance, attacker )){
            val multi = tejiDetail("8003006").power.toFloat() / 100
            val xiValue = (hpReduced * multi).roundToInt()
            attacker.hp += xiValue
            attacker.hp = attacker.hp.coerceAtMost(attacker.maxhp)
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
            defender.speed = defender.speed.coerceAtLeast(1)
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
            defender.speed = defender.speed.coerceAtLeast(1)
            defender.defence = defender.defence.coerceAtLeast(1)
            printBattleInfo(battleId, attacker, 1, "${showName(defender)} SPEED-${extraPower[3]} DEF-${extraPower[2]}, HP-", "8006007")
        }
        if(hasTeji("8006010", attacker) && isTrigger(tejiDetail("8006010").chance, attacker) ){
            defender.hp -= tejiDetail("8006010").power
            val extraPower = tejiDetail("8006010").extraPower
            attacker.speed += extraPower[3]
            attacker.attack += extraPower[1]
            printBattleInfo(battleId, attacker, 1, "${showName(attacker)} SPEED+${extraPower[3]} ATTACK+${extraPower[1]}, ${showName(defender)}HP-", "8006010")
        }
    }

    private fun getSpeed(propsList:MutableList<BattleObject>, randomBasis:Int):Int{
        val speedList = propsList.map { props->
            var propsSpeed = props.speed
            if(hasTeji("8003004", props) && isTrigger(tejiDetail("8003004").chance, props)){
                val multi = tejiDetail("8003004").power.toFloat() / 100
                propsSpeed = (props.speed.toFloat() * multi).roundToInt()
                printBattleInfo(props.battleId, props, 3, "\u901f\u5ea6\u63d0\u5347\u4e3a$propsSpeed", "8003004")
            }
            propsSpeed
        }
        return ((speedList.maxByOrNull { it }
                ?: 1).toFloat() * (100 - Random().nextInt(50)) / 100).roundToInt() + Random().nextInt(randomBasis)
    }

    private fun getBattleValue(origin:Int, person:BattleObject):Int{
        return if(hasTeji("8003010", person) && isTrigger(person)){
            origin
        }else{
            val calculatorValue =  origin.toFloat() * (100 - Random().nextInt(50)) / 100
            calculatorValue.roundToInt().coerceAtLeast(1)
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
                current.statusRound[status.id] = (current.statusRound[status.id] ?: 0) + teji.statusRound
                return status
            }else if(status.target == 1){
                opponent.statusRound[status.id] = (opponent.statusRound[status.id] ?: 0) + teji.statusRound
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

        person.followerList.forEach { follower->
            setFollower(battlePerson, follower.id, follower)
        }

        person.equipmentList.forEach {
            it.detail.follower.forEach { id->
                setFollower(battlePerson, id)
            }
        }
        person.label.forEach {
            val label = mConfig.label.find { f-> f.id == it }!!.copy()
            label.follower.forEach { id->
                setFollower(battlePerson, id)
            }
        }
        person.tipsList.forEach { tips->
            tips.detail.follower.forEach { id->
                setFollower(battlePerson, id)
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

    private fun setFollower(battlePerson:BattleObject, id:String, followerFixed: Follower? = null){
        val follower = followerFixed ?: Follower(id)
        val props = follower.detail.property
        val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.detail.teji.map { f-> convertTejiObject(Pair(f, "")) }.toMutableList())
        result.name = "${battlePerson.name}-${follower.detail.name}${follower.uniqueName }"
        result.battleId = battlePerson.battleId
        battlePerson.follower.add(result)
    }

    private fun getAllTeji(person: Person):MutableList<TeJiObject>{
        val result = mutableListOf<Pair<String,String>>()
        result.addAll(person.teji.map { Pair(it, "") })
        person.equipmentList.filter { it.detail.teji.size > 0 }.forEach {
            result.addAll(it.detail.teji.map { m-> Pair(m, "") })
        }
        person.equipmentList.filter { it.detail.specTeji.size > 0 }.forEach {
            val index = it.detail.spec.indexOf(person.specIdentity)
            result.add(Pair(it.detail.specTeji[index], it.detail.specTejiName[index]) )
        }
        person.label.map {
            mConfig.label.find { e-> e.id == it}!!.copy()
        }.filter { it.teji.isNotEmpty() }.forEach {
            result.addAll(it.teji.map { m-> Pair(m, "") })
        }
        person.tipsList.filter { it.detail.teji.isNotEmpty() }.forEach {
            result.addAll(it.detail.teji.map { m-> Pair(m, "") })
        }

        return result.map {
            convertTejiObject(it)
        }.toMutableList()
    }

    //特技有可能包含多个名称
    private fun convertTejiObject(id:Pair<String, String>):TeJiObject{
        val tejiObject = TeJiObject(id.first)
        val tejiDetail = tejiDetail(id.first)
        tejiObject.name = tejiDetail.name + if (id.second == "" || id.second == tejiDetail.name) "" else "-${id.second}"
        tejiObject.type = tejiDetail.type
        tejiObject.power = tejiDetail.power
        tejiObject.extraPower = tejiDetail.extraPower
        tejiObject.chance = tejiDetail.chance
        tejiObject.status = tejiDetail.status
        tejiObject.statusRound = tejiDetail.statusRound
        return tejiObject
    }

    fun tejiDetail(id:String):TeJi{
        return mConfig.teji.find { it.id == id }!!.toTeji()
    }

    private fun tejiDetail(id:String, person:BattleObject):TeJiObject{
        return person.kills.find { it.id == id }!!
    }

    private fun statusDetail(id:String):Status{
        return mConfig.status.find { it.id == id }!!
    }

    private fun hasTeji(id: String, person:BattleObject):Boolean{
        return person.kills.find { it.id == id } != null
    }

    private fun isTrigger(chance:Int = 50, current: BattleObject, opponent:BattleObject? = null):Boolean{
        val exception = if(opponent != null) opponent.type == 1 || hasTeji("8001006", opponent) else false
        return !exception && isTrigger(current) && isTrigger(chance)
    }

    private fun isTrigger(current: BattleObject):Boolean{
        return isStatusExpire(current, "8100005")
    }

    private fun isTrigger(chance:Int = 50):Boolean{
        return Random().nextInt(100) < chance
    }

    private fun showName(person:BattleObject, showStatus:Boolean = true):String{
        val status = mutableListOf<String>()
        mConfig.status.forEach {
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