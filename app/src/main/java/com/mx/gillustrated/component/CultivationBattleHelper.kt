package com.mx.gillustrated.component

import com.mx.gillustrated.vo.cultivation.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import com.mx.gillustrated.component.CultivationHelper.mConfig

object CultivationBattleHelper {

    var mBattles:ConcurrentHashMap<String, BattleInfo> = ConcurrentHashMap()

    fun battleEnemy(allPersons:ConcurrentHashMap<String, Person>, person: Person, enemy: Enemy, xiuwei:Int):Boolean{
        val props1 = CultivationHelper.getProperty(person)
        val battlePerson = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, person.teji)
        val battleEnemy = BattleObject(enemy.HP, enemy.maxHP, enemy.attack, enemy.defence, enemy.speed, 1)
        val battleId = UUID.randomUUID().toString()
        battlePerson.battleId = battleId
        replenishInfo(allPersons, person, battlePerson)
        battleEnemy.battleId = battleId
        replenishEnemyInfo(enemy, battleEnemy)
        mBattles[battleId] = BattleInfo(battleId, person, null, battlePerson, battleEnemy)
        addBattleDetail(battleId, "\u6218\u6597\u5f00\u59cb")
        val firstList = mutableListOf(battlePerson)
        firstList.addAll(battlePerson.follower)
        val enemyList = mutableListOf(battleEnemy)
        enemyList.addAll(battleEnemy.follower)
        startBattle(firstList, enemyList ,100, 1000)

        val firstWin = battlePerson.hp > 0
        if(firstWin){
            mBattles[battleId]!!.winnerName = person.name
            mBattles[battleId]!!.looserName = enemy.name
            addBattleDetail(battleId, "${showName(battlePerson, false)}\u83b7\u80dc, \u6b8bHP:${battlePerson.hp}")
            CultivationHelper.writeHistory("${person.name}(${battlePerson.hp})  ${props1[0] - battlePerson.hp}🔪${enemy.HP - battleEnemy.hp}  ${enemy.name}(${battleEnemy.hp})", person, 2, battleId)
            person.xiuXei += xiuwei
        }else{
            mBattles[battleId]!!.winnerName = enemy.name
            mBattles[battleId]!!.looserName = person.name
            addBattleDetail(battleId, "${enemy.name}\u83b7\u80dc, \u6b8bHP:${battleEnemy.hp}")
            CultivationHelper.writeHistory(" ${enemy.name}(${battleEnemy.hp}/${enemy.remainHit}-${enemy.attack}:${enemy.defence}:${enemy.speed})  ${enemy.HP - battleEnemy.hp}🔪${props1[0] - battlePerson.hp}  ${person.name}(${battlePerson.hp})", person, 2, battleId)
            person.xiuXei -= xiuwei
        }
        person.HP = battlePerson.hp -  props1[5]
        enemy.HP = battleEnemy.hp

        return firstWin
    }

    fun battlePerson(allPersons:ConcurrentHashMap<String, Person>?, person1: Person, person2: Person, round:Int, xiuwei:Int):Boolean{

        val props1 = CultivationHelper.getProperty(person1)
        val props2 = CultivationHelper.getProperty(person2)
        val battlePerson1 = BattleObject(props1[0], props1[1], props1[2], props1[3], props1[4], 0, person1.teji)
        val battlePerson2 = BattleObject(props2[0], props2[1], props2[2], props2[3], props2[4], 0, person2.teji)
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
        startBattle(firstList, secondList,40, round)

        mBattles[battleId]!!.round = 0
        val firstWin = battlePerson1.hp > battlePerson2.hp
        if(firstWin){
            mBattles[battleId]!!.winnerName = person1.name
            mBattles[battleId]!!.looserName = person2.name
            addBattleDetail(battleId, "${showName(battlePerson1, false)}\u83b7\u80dc, \u6b8bHP:${battlePerson1.hp}")
            CultivationHelper.writeHistory("${person1.name}(${battlePerson1.hp})  ${props1[0] - battlePerson1.hp}🔪${props2[0] - battlePerson2.hp}  ${person2.name}(${battlePerson2.hp})", person1, 2, battleId)
            person1.xiuXei += xiuwei / 4
            person2.xiuXei -= xiuwei
        }else{
            mBattles[battleId]!!.winnerName = person2.name
            mBattles[battleId]!!.looserName = person1.name
            addBattleDetail(battleId, "${showName(battlePerson2, false)}\u83b7\u80dc, \u6b8bHP:${battlePerson2.hp}")
            CultivationHelper.writeHistory("${person2.name}(${battlePerson2.hp})  ${props2[0] - battlePerson2.hp}🔪${props1[0] - battlePerson1.hp}  ${person1.name}(${battlePerson1.hp})", person2, 2, battleId)
            person2.xiuXei += xiuwei / 4
            person1.xiuXei -= xiuwei
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
            addBattleDetail(battleId, "${showName(getKeyBattleObject(battlePersons[attackFirstIndex]))}\u7684\u56de\u5408")
            battlingOpponent(battlePersons[attackFirstIndex], battlePersons[attackLaterIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
            addBattleDetail(battleId, "${showName(getKeyBattleObject(battlePersons[attackLaterIndex]))}\u7684\u56de\u5408")
            battlingOpponent(battlePersons[attackLaterIndex], battlePersons[attackFirstIndex])
            if(afterHPChangedPossibleEveryPoint(battlePersons))
                break
        }
    }

    private fun preBattleRound(battlePersons:MutableList<MutableList<BattleObject>>){
        val battleId = battlePersons[0][0].battleId
        battlePersons.forEachIndexed { index, currentList ->
            val allOpponent = battlePersons[Math.abs(index - 1)]
            currentList.forEach { current ->
                if (hasTeji("8003003", current)) {
                    current.minDamage = 10
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8003003").name}\u6548\u679c, \u6700\u5c0f\u4f24\u5bb310", "8003003")
                }
                if (hasTeji("8001005", current)) {
                    current.extraDamage += 40
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8001005").name}\u6548\u679c, \u9644\u52a0\u4f24\u5bb340", "8001005")
                }
                if (hasTeji("8001004", current)) {
                    current.extraDamage += 20
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8001004").name}\u6548\u679c, \u9644\u52a0\u4f24\u5bb320", "8001004")
                }
                if(hasTeji("8002003", current)){
                    val opponent = getBattleObject(allOpponent)
                    opponent.hp -= 30
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002003").name}\u6548\u679c, ${showName(opponent, false)}HP-30", "8002003")
                }
                if(hasTeji("8002004", current)){
                    val opponent = getBattleObject(allOpponent)
                    opponent.hp -= 50
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002004").name}\u6548\u679c, ${showName(opponent, false)}HP-50", "8002004")
                }
                if(hasTeji("8002006", current)){//weakness 20
                    allOpponent.forEach { opponent->
                        opponent.attack -= Math.round(opponent.attackBasis * 0.2f)
                        opponent.defence -= Math.round(opponent.defenceBasis * 0.2f)
                        opponent.speed -= Math.round(opponent.speedBasis * 0.2f)
                        addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002006").name}\u6548\u679c, ${showName(opponent, false)}\u865a\u5f3120%", "8002006")
                    }
                }
                if(hasTeji("8002007", current)){//weakness 50
                    allOpponent.forEach { opponent ->
                        opponent.attack -= Math.round(opponent.attackBasis * 0.5f)
                        opponent.defence -= Math.round(opponent.defenceBasis * 0.5f)
                        opponent.speed -=  Math.round(opponent.speedBasis * 0.5f)
                        addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002007").name}\u6548\u679c, ${showName(opponent, false)}\u865a\u5f3150%", "8002007")
                    }
                }
                if(hasTeji("8002008", current)){//gain 20
                    current.attack += Math.round(current.attackBasis * 0.2f)
                    current.defence += Math.round(current.defenceBasis * 0.2f)
                    current.speed += Math.round(current.speedBasis * 0.2f)
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002008").name}\u6548\u679c, \u5c5e\u6027\u589e\u5f3a20%", "8002008")
                }
                if(hasTeji("8002009", current)){//gain 50
                    current.attack += Math.round(current.attackBasis * 0.5f)
                    current.defence += Math.round(current.defenceBasis * 0.5f)
                    current.speed += Math.round(current.speedBasis * 0.5f)
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8002009").name}\u6548\u679c, \u5c5e\u6027\u589e\u5f3a50%", "8002009")
                }
                if(hasTeji("8004001", current)){
                    current.attack += Math.round(current.attackBasis * 1f)
                    current.defence += Math.round(current.defenceBasis * 1f)
                    current.speed += Math.round(current.speedBasis * 1f)
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004001")
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8004001").name}\u53d1\u52a8 ${if (status == null) "" else ", ${status.name}发动"}", "8004001")
                }
                if(hasTeji("8004002", current)){
                    current.speed += Math.round(current.speedBasis * 1f)
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004002")
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8004002").name}\u53d1\u52a8 ${if (status == null) "" else ", ${status.name}发动"}", "8004002")
                }
                if(hasTeji("8004003", current)){
                    current.speed += Math.round(current.speedBasis * 1f)
                    val opponent = getBattleObject(allOpponent)
                    val status = addStatus(current, opponent, "8004003")
                    addBattleDetail(battleId, "${showName(current, false)}\u7279\u6280:${tejiDetail("8004003").name}\u53d1\u52a8 ${if (status == null) "" else ", ${status.name}发动"}", "8004003")
                }
            }
        }
    }

    private fun preBattleEveryRound(battlePersons:MutableList<MutableList<BattleObject>>){
        val battleId = battlePersons[0][0].battleId
        battlePersons.forEachIndexed { index, currentList ->
            val allOpponent = battlePersons[Math.abs(index - 1)]
            currentList.forEach { current ->
                if(hasTeji("8002001", current) && isTrigger(tejiDetail("8002001").chance, current)){
                    val opponent = getBattleObject(allOpponent)
                    opponent.hp -= 10
                    addBattleDetail(battleId, "${showName(current)}\u7279\u6280:${tejiDetail("8002001").name}\u6548\u679c, ${showName(opponent)}HP-10", "8002001")
                }
                if(hasTeji("8002002", current) && isTrigger(tejiDetail("8002002").chance, current)){
                    val opponent = getBattleObject(allOpponent)
                    opponent.hp -= 20
                    addBattleDetail(battleId, "${showName(current)}\u7279\u6280:${tejiDetail("8002002").name}\u6548\u679c, ${showName(opponent)}HP-20", "8002002")
                }
                if(hasTeji("8002005", current) && isTrigger(tejiDetail("8002005").chance, current)){
                    current.hp += 20
                    current.hp = Math.min(current.hp, current.maxhp)
                    addBattleDetail(battleId, "${showName(current)}\u7279\u6280:${tejiDetail("8002005").name}\u6548\u679c, HP+20", "8002005")
                }
                if(isStatuTrigger(current, "8100003")){
                    val reduced = Math.round( current.hp * 0.2f)
                    current.hp -= reduced
                    addBattleDetail(battleId, "${showName(current)} ${statusDetail("8100003").name}\u53d1\u52a8 , HP-$reduced")
                }
                if(hasTeji("8001007", current) && current.hp > 0 && isTrigger(tejiDetail("8001007").chance, current, getBattleObject(allOpponent)) ){//kill immediately
                    val opponent = getBattleObject(allOpponent)
                    if(opponent.hp > 0) {
                        opponent.hp = 0
                        addBattleDetail(battleId, "${showName(current)}\u7279\u6280:${tejiDetail("8001007").name}\u53d1\u52a8, ${showName(opponent)}HP0", "8001007")
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
                    addBattleDetail(battleId, "${showName(current, round > 0)}\u7279\u6280:${tejiDetail("8001003").name}\u53d1\u52a8, HP\u5168\u6062\u590d", "8001003")
                }else if(hasTeji("8001001", current) && current.hp <= 0 && isTrigger(tejiDetail("8001001").chance, current)){
                    current.hp = 1
                    current.goneCount++
                    addBattleDetail(battleId, "${showName(current, round > 0)}\u7279\u6280:${tejiDetail("8001001").name}\u53d1\u52a8, HP1", "8001001")
                }
                if(hasTeji("8001002", current) && current.hp <= 0 && isTrigger(tejiDetail("8001002").chance, current, getBattleObject(opponentList))){
                    val opponent = getBattleObject(opponentList)
                    opponent.hp = 1
                    addBattleDetail(battleId, "${showName(current, round > 0)}\u7279\u6280:${tejiDetail("8001002").name}\u53d1\u52a8, ${showName(opponent, round > 0)}HP1", "8001002")
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
                   addBattleDetail(battleId, "${showName(it)}\u88ab\u51fb\u5012")
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
                addBattleDetail(attacker.battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8003007").name}\u53d1\u52a8", "8003007")
                battleCycle(attacker, defender2)
            }
        }
    }

    private fun battleCycle(attacker: BattleObject, defender: BattleObject){
        inBattles(attacker, defender)
        while (isStatuTrigger(attacker, "8100001")){
            if(attacker.hp <= 0 || defender.hp <= 0)
                break
            addBattleDetail(attacker.battleId, "${showName(attacker)} ${statusDetail("8100001").name}\u53d1\u52a8 ")
            inBattles(attacker, defender)
        }
        if(hasTeji("8003008", defender) && defender.hp > 0 && attacker.hp > 0 && isTrigger(tejiDetail("8003008").chance, defender) ){//anti-attack
            addBattleDetail(defender.battleId, "${showName(defender)}\u7279\u6280:${tejiDetail("8003008").name}\u53d1\u52a8", "8003008")
            inBattles(defender, attacker)
        }
    }

    private fun inBattles(attacker: BattleObject, defender: BattleObject){
        val battleId = attacker.battleId
        if(isStatuTrigger(attacker, "8100004")){//盲
            addBattleDetail(battleId, "${showName(attacker)} ${statusDetail("8100004").name}\u53d1\u52a8")
            return
        }
        if(isStatuTrigger(attacker, "8100002")){//封
            addBattleDetail(battleId, "${showName(attacker)} ${statusDetail("8100002").name}\u53d1\u52a8, \u65e0\u6cd5\u884c\u52a8")
            return
        }
        if(hasTeji("8003001", defender) && isTrigger(tejiDetail("8003001").chance, defender)){
            addBattleDetail(battleId, "${showName(defender)}\u7279\u6280:${tejiDetail("8003001").name}\u53d1\u52a8, ${showName(attacker)}\u653b\u51fb\u88ab\u56de\u907f", "8003001")
            return
        }
        val attackerValue = getBattleValue(attacker.attack)
        var defenderValue = getBattleValue(defender.defence)
        if(hasTeji("8003005", attacker) && isTrigger(tejiDetail("8003005").chance, attacker)){
            defenderValue = 0
            addBattleDetail(battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8003005").name}\u53d1\u52a8, ${showName(defender)}\u9632\u5fa10", "8003005")
        }
        var attackResult = attackerValue - defenderValue
        if(hasTeji("8003002", attacker) && attackResult > 0 && isTrigger(tejiDetail("8003002").chance, attacker) ){
            attackResult = Math.round( attackResult * 2.0f)
            addBattleDetail(battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8003002").name}\u53d1\u52a8, \u4f24\u5bb3\u7ed3\u679c2\u500d", "8003002")
        }

        val hpReduced = Math.max(attacker.minDamage, attackResult)
        defender.hp -= hpReduced + attacker.extraDamage
        addBattleDetail(battleId, "${showName(attacker)}\u7684\u653b\u51fb，${showName(defender)}HP-${hpReduced + attacker.extraDamage}")

        if(hasTeji("8003009", defender) && isTrigger(tejiDetail("8003009").chance, defender, attacker) ){// anti-shake
            val antiValue = Math.round( hpReduced * 0.5f)
            attacker.hp -= antiValue
            addBattleDetail(battleId, "${showName(defender)}\u7279\u6280:${tejiDetail("8003009").name}\u53d1\u52a8, ${showName(attacker)}HP-$antiValue", "8003009")
        }
        if(hasTeji("8003006", attacker) && isTrigger(tejiDetail("8003006").chance, attacker )){
            val xiValue = Math.round( hpReduced * 0.5f)
            attacker.hp += xiValue
            attacker.hp = Math.min(attacker.hp, attacker.maxhp)
            addBattleDetail(battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8003006").name}\u53d1\u52a8, HP+$xiValue", "8003006")
        }
        if(hasTeji("8005001", attacker) && isTrigger(tejiDetail("8005001").chance, attacker)){
            val status = addStatus(attacker, defender, "8005001")
            if(status != null)
                addBattleDetail(battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8005001").name}\u53d1\u52a8, ${status.name}发动", "8005001")
        }
        if(hasTeji("8005002", attacker) && isTrigger(tejiDetail("8005002").chance, attacker)){
            val status = addStatus(attacker, defender, "8005002")
            if(status != null)
                addBattleDetail(battleId, "${showName(attacker)}\u7279\u6280:${tejiDetail("8005002").name}\u53d1\u52a8, ${status.name}发动", "8005002")
        }
    }

    private fun getSpeed(propsList:MutableList<BattleObject>, randomBasis:Int):Int{
        val speedList = propsList.map { props->
            var propsSpeed = props.speed
            if(hasTeji("8003004", props) && isTrigger(tejiDetail("8003004").chance, props)){
                propsSpeed = Math.round( props.speed.toFloat() * 2.0f)
                addBattleDetail(props.battleId, "${showName(props)}\u7279\u6280:${tejiDetail("8003004").name}\u53d1\u52a8, \u901f\u5ea6\u63d0\u5347\u4e3a$propsSpeed", "8003004")
            }
            propsSpeed
        }
        return speedList.maxBy { it }!! + Random().nextInt(randomBasis)
    }

    private fun getBattleValue(origin:Int, randomBasis:Int = 20):Int{
        val calculatorValue =  origin + (randomBasis - Random().nextInt(randomBasis * 2))
        return Math.max(0, calculatorValue)
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
            if(status.target == 0 && isStatusExpire(current, status.id)){
                current.statusRound[status.id] = getRound(current.battleId) + teji.statusRound
                return status
            }else if(status.target == 1 && isStatusExpire(opponent, status.id)){
                opponent.statusRound[status.id] = getRound(opponent.battleId) + teji.statusRound
                return status
            }
        }
        return null
    }

    private fun isStatusExpire (person: BattleObject, id:String):Boolean{
        return  person.statusRound[id] == null || (person.statusRound[id]!! < getRound(person.battleId))
    }

    private fun remainStatusRound (person: BattleObject, id:String):Int{
        if(person.statusRound[id] == null)
            return 0
        if(person.statusRound[id]!! >= getRound(person.battleId))
            return person.statusRound[id]!! + 1 - getRound(person.battleId)
        return 0
    }

    private fun isStatuTrigger (person: BattleObject, statusId:String):Boolean{
        val status = statusDetail(statusId)
        return  person.statusRound[statusId] != null && (person.statusRound[statusId]!! >=  getRound(person.battleId)) && isTrigger(status.chance)
    }

    private fun addBattleDetail(id:String, content:String, teji:String? = null){
        val battleInfo =  mBattles[id]!!
        battleInfo.seq++
        if(teji == null)
            battleInfo.details.add(BattleInfoSeq(battleInfo.round, battleInfo.seq, content))
        else
            battleInfo.details.add(BattleInfoSeq(battleInfo.round, battleInfo.seq, content, teji))
    }

    private fun replenishEnemyInfo(enemy: Enemy, battleEnemy:BattleObject){
        battleEnemy.name = enemy.name
        battleEnemy.follower = enemy.followerList.filter { !it.isDead }.map { follower->
            val props = follower.property
            val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.teji)
            result.name = "${enemy.name}-${follower.name}${follower.uniqueName}"
            result.followerReference = follower
            result.battleId = battleEnemy.battleId
            result
        }.toMutableList()
    }

    private fun replenishInfo(allPersons:ConcurrentHashMap<String, Person>?, person: Person, battlePerson:BattleObject){
        battlePerson.name = person.name
        battlePerson.attackBasis -= person.equipmentProperty[1]
        battlePerson.defenceBasis -= person.equipmentProperty[2]
        battlePerson.speedBasis -= person.equipmentProperty[3]

        if(person.followerList.isNotEmpty()){
            battlePerson.follower = person.followerList.map {
                val follower = mConfig.follower.find { f-> f.id == it.first }!!
                val props = follower.property
                val result = BattleObject(props[0], props[0], props[1], props[2], props[3], 2, follower.teji)
                result.name = "${person.name}-${follower.name}${it.second}"
                result.battleId = battlePerson.battleId
                result
            }.toMutableList()
        }
        if(allPersons == null)
            return
        val partner = allPersons[person.partner ?: "none"]
        if(partner != null){
            val partnerProps = CultivationHelper.getProperty(partner)
            val partnerBattleObject = BattleObject(partnerProps[0], partnerProps[0], partnerProps[1], partnerProps[2], partnerProps[3],
                    3, partner.teji)
            partnerBattleObject.name = "${person.name}-${partner.name}"
            partnerBattleObject.battleId = battlePerson.battleId
            battlePerson.follower.add(partnerBattleObject)
        }
    }

    private fun tejiDetail(id:String):TeJi{
        return CultivationHelper.mConfig.teji.find { it.id == id }!!
    }

    private fun statusDetail(id:String):Status{
        return CultivationHelper.mConfig.status.find { it.id == id }!!
    }

    private fun hasTeji(id: String, person:BattleObject):Boolean{
        return person.kills.find { it == id } != null
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

        constructor(h: Int, m:Int, a: Int, d: Int, s: Int, t:Int, k:MutableList<String>):this(h, m, a,d,s,t){
            kills = k
        }

        var attack: Int = a
        var defence:Int = d
        var speed:Int = s
        var hp:Int = h

        var goneCount:Int = 0//gone次数
        var statusRound:HashMap<String, Int> = HashMap()
        var extraDamage:Int = 0
        var minDamage:Int = 1
        var isDead:Boolean = false

        var follower = mutableListOf<BattleObject>()
        var attackBasis:Int = a
        var defenceBasis:Int = d
        var speedBasis:Int = s
        val hpBasis:Int = h
        val maxhp:Int = m
        val type:Int = t //normal = 0, enemy = 1, follower = 2, partner = 3
        var kills:MutableList<String> = mutableListOf()
        var battleId:String = ""
        var name: String = ""
        val attackInit:Int = a
        val defenceInit:Int = d
        val speedInit:Int = s
        var followerReference:Follower? = null
    }
}