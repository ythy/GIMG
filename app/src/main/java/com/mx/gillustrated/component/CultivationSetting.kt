package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*
import kotlin.collections.HashMap

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    val PostColors = arrayOf("#E2D223", "#BE0012", "#0272E4", "#12A703", "#EF7362")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")


    object BattleSettings {
        const val AllianceMinSize = 16
        const val AllianceBonusCount = 4
        val AllianceBonus = arrayOf(5,8,5,3,1,0,0,0,0,0,0)// [0]: equipment maxCount, [1..10]: bonus count by BonusCount
        const val ClanMinSize = 4
        const val ClanBonusCount = 3
        val ClanBonus = arrayOf(5,3,2,1,0,0,0,0,0,0,0)
        const val NationMinSize = 4
        const val NationBonusCount = 3
        val NationBonus = arrayOf(5,3,2,1,0,0,0,0,0,0,0)
        const val SingleMinSize = 32
        const val SingleBonusCount = 8
        val SingleBonus = arrayOf(5,30,20,15,10,6,4,2,1,0,0)
    }

    const val SP_JIE_TURN = 81
    const val SP_NAN_9 = 5
    const val SP_NAN_81 = 10
    const val LIFE_TIME_YEAR = 100
    val EVENT_WEIGHT = listOf("1200-50","7200-40","8400-40","9600-100")
    const val SP_PUNISH_BOSS_MILLION = 200

    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983", "\u4ec6", "\u8bcf", "\u536b", "\u8bed", "\u9b41", "\u5f71")
    val SpecPersonFirstNameWeight:Pair<Int, Int> = Pair(50, 20)// tianfu.linggen

    // 1xxx spec; 2xxx real; 30xx sanguoyingxiong;
    val SpecPersonFirstName2:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(12000010, Pair("\u6bdb","\u6b23"),12000021, 1000, 2000),
            PresetInfo(12000021, Pair("\u674e","\u4e00\u6850"),12000010, 1000, 2000),
            PresetInfo(12000031, Pair("\u674e","\u4e00\u6850\u2161"),12000010, 500, 1000),
            PresetInfo(12000041, Pair("\u6bdb","\u6c47\u5f64"),0, 1000, 2000, Pair(12000010, 12000021)),
            PresetInfo(12000050, Pair("\u5f20","\u5c0f\u51e1"),12000061, 200, 600),
            PresetInfo(12000061, Pair("\u78a7","\u7476"),12000050, 200, 600),
            PresetInfo(12000071, Pair("\u9646","\u96ea\u742a"),12000050, 200, 600),
            PresetInfo(12000080, Pair("\u7f57","\u5cf0"),12000091, 200, 600),
            PresetInfo(12000091, Pair("\u5f90","\u6b23"),12000080, 200, 600),
            PresetInfo(12000101, Pair("\u73cd\u59ae","\u7279"),12000080, 100, 100),
            PresetInfo(12000110, Pair("\u6731\u5229\u5b89","\u68ad\u7f57"),12000121, 1000, 600000),
            PresetInfo(12000121, Pair("\u57ce\u6237","\u7eb1\u7ec7"),12000110, 1000, 600000),
            PresetInfo(12000130, Pair("\u6613","\u5929"),12000141, 100, 200),
            PresetInfo(12000141, Pair("\u5bd2","\u6708"),12000130, 100, 200)
     )

    //13 00 001 0
    private val SpecPersonFirstName3:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(13000010, Pair("\u6b65","\u60ca\u4e91"),0, 100, 200),
            PresetInfo(13000020, Pair("\u8042","\u98ce"),13000031, 200, 600),
            PresetInfo(13000031, Pair("\u7b2c\u4e8c","\u68a6"),13000020),
            PresetInfo(13000040, Pair("\u96c4","\u9738"),0, 200, 600),
            PresetInfo(13000050, Pair("\u79e6","\u971c")),
            PresetInfo(13000060, Pair("\u65ad","\u6d6a"),0,100,100),

            PresetInfo(13010031, Pair("\u82cf","\u6a31"),0, 100, 100),
            PresetInfo(13010041, Pair("\u674e","\u7ea2\u8896"),0, 100, 100),
            PresetInfo(13010070, Pair("\u674e","\u5bfb\u6b22"),0, 200, 600),

            PresetInfo(13020011, Pair("\u9ec4","\u84c9"),0, 200, 600),
            PresetInfo(13020021, Pair("\u8d75","\u654f"),0, 200, 600),
            PresetInfo(13020031, Pair("\u5468","\u82b7\u82e5"),0, 200, 600),
            PresetInfo(13020041, Pair("\u4e1c\u65b9","\u4e0d\u8d25"),0, 500, 1000),
            PresetInfo(13020051, Pair("\u5c0f","\u662d"),0, 100, 200),
            PresetInfo(13020061, Pair("\u674e","\u79cb\u6c34"),13020070, 100, 100),
            PresetInfo(13020070, Pair("\u65e0","\u5d16\u5b50"),13020061, 100, 200),
            PresetInfo(13020081, Pair("\u6728","\u5a49\u6e05"),0,100,100),
            PresetInfo(13020091, Pair("\u4efb","\u76c8\u76c8"), 0, 200, 600)
    )

    //achieved immutable
    private val SpecPersonFirstName4:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Triple("\u66f9", "\u64cd", 3047),16030141,500, 1000),
            PresetInfo(14000020, Triple("\u53f8\u9a6c", "\u61ff", 3011), 0,200, 600),
            PresetInfo(14000030, Triple("\u90ed", "\u5609", 3019), 0,100, 200),
            PresetInfo(14000041, Triple("\u7504", "\u5b93", 3009), 0, 100, 200),
            PresetInfo(14000050, Triple("\u5f20", "\u8fbd", 3034)),
            PresetInfo(14000061, Triple("\u8521", "\u7430", 3010), 0,100, 100),
            PresetInfo(14000070, Triple("\u590f\u4faf","\u60c7", 3008)), PresetInfo(14000080, Triple("\u590f\u4faf","\u6e0a", 3027)),
            PresetInfo(14000090, Triple("\u8340", "\u5f67", 3010)), PresetInfo(14000100, Triple("\u5178","\u97e6", 3007)),
            PresetInfo(14000110, Triple("\u5f90","\u6643", 3028)), PresetInfo(14000120, Triple("\u5f20","\u90c3", 3029)),
            PresetInfo(14000130, Triple("\u4e8e","\u7981", 3030)), PresetInfo(14000140, Triple("\u4e50","\u8fdb", 3041)),
            PresetInfo(14000150, Triple("\u9093","\u827e", 3026)), PresetInfo(14000160, Triple("\u8d3e", "\u8be9", 3009)),

            PresetInfo(14010010, Triple("\u5218", "\u5907", 3045), 14020101,200, 600),
            PresetInfo(14010020, Triple("\u5173", "\u7fbd", 3020), 0,200, 600),
            PresetInfo(14010030, Triple("\u5f20", "\u98de", 3015),0,100, 200),
            PresetInfo(14010040, Triple("\u8d75", "\u4e91", 3044), 14010101,100, 200),
            PresetInfo(14010050, Triple("\u8bf8\u845b", "\u4eae", 3013), 14010061,500, 1000),
            PresetInfo(14010061, Triple("\u9ec4", "\u6708\u82f1", 3006), 14010050,100, 100),
            PresetInfo(14010070, Triple("\u5e9e","\u7edf", 3012)), PresetInfo(14010080, Triple("\u9a6c","\u8d85", 3035), 16030111),
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 3014)), PresetInfo(14010101, Triple("\u9a6c","\u4e91\u7984", 3008), 14010040),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 3032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 3001), 0,200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 3022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 3031), 14010151),
            PresetInfo(14010151, Triple("\u795d", "\u878d", 3011), 14010140),

            PresetInfo(14020010, Triple("\u5b59", "\u6743", 3046), 16030121,200, 600),
            PresetInfo(14020020, Triple("\u5b59", "\u7b56", 3003),14020041,100, 200),
            PresetInfo(14020030, Triple("\u9646", "\u900a", 3005), 16030161),
            PresetInfo(14020041, Triple("\u5927", "\u4e54",3005), 14020020,100, 200),
            PresetInfo(14020051, Triple("\u5c0f", "\u4e54",3004), 14020060,100, 200),
            PresetInfo(14020060, Triple("\u5468", "\u745c", 3006), 14020051,500, 1000),
            PresetInfo(14020070, Triple("\u5468","\u6cf0",3040)), PresetInfo(14020080, Triple("\u7518","\u5b81", 3033)),
            PresetInfo(14020090, Triple("\u592a\u53f2", "\u6148", 3002)), PresetInfo(14020101, Triple("\u5b59","\u5c1a\u9999", 3003), 14010010, 100, 200),
            PresetInfo(14020110, Triple("\u9ec4", "\u76d6",3025)),PresetInfo(14020120, Triple("\u5415", "\u8499",3004)),
            PresetInfo(14020130, Triple("\u5b59", "\u575a",3018)),PresetInfo(14020140, Triple("\u9c81", "\u8083",3024)),

            PresetInfo(14030010, Triple("\u5415", "\u5e03", 3016), 14030041, 1000, 2000),
            PresetInfo(14030020, Triple("\u8463", "\u5353", 3001), 0, 200, 600),
            PresetInfo(14030030, Triple("\u9ad8", "\u987a",3023)),
            PresetInfo(14030041, Triple("\u8c82", "\u8749", 3007), 14030010, 100, 200),
            PresetInfo(14030050, Triple("\u674e", "\u5112", 3021)),
            PresetInfo(14030060, Triple("\u8881", "\u7ecd", 3017), 0, 100, 200),
            PresetInfo(14030070, Triple("\u989c","\u826f",3042)), PresetInfo(14030080, Triple("\u6f58","\u51e4",3039)),
            PresetInfo(14030090, Triple("\u534e", "\u96c4",3037)), PresetInfo(14030100, Triple("\u6587","\u4e11",3043)),
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 3002), 0, 200, 600, Pair(14030010, 14030041)),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7",3038)), PresetInfo(14030130, Triple("\u5218", "\u8868",3036)),
            PresetInfo(14030140, Triple("\u534e", "\u4f57",3048))

    )

    private val SpecPersonFirstName5:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(15000010, Pair("\u674e","\u767d"),0, 200, 600),
            PresetInfo(15000020, Pair("\u675c","\u752b"),0, 100, 100),
            PresetInfo(15000031, Pair("\u674e","\u6e05\u7167"),0, 100, 200),
            PresetInfo(15000040, Pair("\u8f9b","\u5f03\u75be"),0, 100, 100),
            PresetInfo(15000050, Pair("\u767d","\u5c45\u6613"),0, 100, 100),
            PresetInfo(15000060, Pair("\u82cf","\u8f7c"),0, 100, 100),
            PresetInfo(15000070, Pair("\u738b","\u7fb2\u4e4b"),0, 100, 200),
            PresetInfo(15000081, Pair("\u865e","\u59ec"),15010020, 100, 200),
            PresetInfo(15000091, Pair("\u6768","\u7389\u73af"),15020040,100,200),
            PresetInfo(15000101, Pair("\u897f","\u65bd"),0,100,200),
            PresetInfo(15000111, Pair("\u738b","\u662d\u541b"),0,100,100),

            PresetInfo(15010010, Pair("\u674e","\u5143\u9738"),0, 500, 1000),
            PresetInfo(15010020, Pair("\u9879","\u7fbd"),15000081, 500, 1000),
            PresetInfo(15010030, Pair("\u8346","\u8f72"),0, 100, 100),
            PresetInfo(15010040, Pair("\u674e","\u5e7f"),0, 100, 100),

            PresetInfo(15020010, Pair("\u5b34","\u653f"),0, 500, 1000),
            PresetInfo(15020020, Pair("\u674e","\u4e16\u6c11"),0, 100, 200),
            PresetInfo(15020030, Pair("\u8d75","\u4f76"),0, 100, 100),
            PresetInfo(15020040, Pair("\u674e","\u9686\u57fa"),15000091),
            PresetInfo(15020050, Pair("\u5218","\u90a6"),0, 100, 100)
    )

    private val SpecPersonFirstName6:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(16000010, Pair("\u5b8b","\u6c5f"),0, 100, 200),
            PresetInfo(16000020, Pair("\u6768","\u5fd7")), PresetInfo(16000030, Pair("\u5218","\u5510")),
            PresetInfo(16000040, Pair("\u516c\u5b59","\u80dc"),0, 100, 100),
            PresetInfo(16000051, Pair("\u6248","\u4e09\u5a18"),0, 200, 600),
            PresetInfo(16000060, Pair("\u6797","\u51b2"),0, 100, 100),
            PresetInfo(16000070, Pair("\u9c81","\u667a\u6df1")),
            PresetInfo(16000080, Pair("\u6b66","\u677e"),0, 100, 100),
            PresetInfo(16000090, Pair("\u962e","\u5c0f\u4e03")),
            PresetInfo(16000100, Pair("\u82b1","\u8363"),0, 100, 200),
            PresetInfo(16000110, Pair("\u79e6","\u660e")), PresetInfo(16000120, Pair("\u67f4","\u8fdb")),
            PresetInfo(16000130, Pair("\u674e","\u9035"),0, 100, 200),
            PresetInfo(16000140, Pair("\u6234","\u5b97")),
            PresetInfo(16000150, Pair("\u5f20","\u987a"),0,100,100),

            PresetInfo(16010010, Pair("\u8521","\u4eac"),0, 100, 100),
            PresetInfo(16010020, Pair("\u9ad8","\u4fc5"),0, 100, 100),
            PresetInfo(16010030, Pair("\u9ad8","\u8859\u5185")),
            PresetInfo(16010040, Pair("\u7ae5","\u8d2f")),
            PresetInfo(16010050, Pair("\u897f\u95e8","\u5e86"),16010061),
            PresetInfo(16010061, Pair("\u6f58","\u91d1\u83b2"),16010050, 100, 100),

            PresetInfo(16020010, Pair("\u6641","\u76d6"),0, 100, 200),
            PresetInfo(16020020, Pair("\u738b","\u4f26")),

            PresetInfo(16030010, Pair("\u5218", "\u7109"), 0, 100, 100),
            PresetInfo(16030020, Pair("\u7559", "\u8d5e"), 0, 100, 100),
            PresetInfo(16030030, Pair("\u5173", "\u7d22"), 16030081, 200, 600),
            PresetInfo(16030041, Pair("\u8d75", "\u8944"), 0, 200, 600, Pair(14010040, 14010101)),
            PresetInfo(16030050, Pair("\u8bb8", "\u52ad"), 0, 100, 200),
            PresetInfo(16030061, Pair("\u738b", "\u8363"), 0, 100, 200),
            PresetInfo(16030071, Pair("\u82b1", "\u9b18"), 16030030, 100, 200, Pair(14010140, 14010151)),
            PresetInfo(16030081, Pair("\u9c8d", "\u4e09\u5a18"), 16030030, 100, 200),
            PresetInfo(16030090, Pair("\u738b", "\u53cc")), PresetInfo(16030100, Pair("\u9648", "\u7433")),
            PresetInfo(16030101, Pair("\u5468", "\u5983"), 0, 100, 100, Pair(14020060, 14020051)),
            PresetInfo(16030111, Pair("\u6768", "\u5a49"), 14010080, 100, 200),
            PresetInfo(16030121, Pair("\u6f58", "\u6dd1"), 14020010, 100, 200),
            PresetInfo(16030131, Pair("\u6a0a", "\u7389\u51e4"), 14010040, 100, 200),
            PresetInfo(16030141, Pair("\u675c", "\u592b\u4eba"), 14000010, 100, 200),
            PresetInfo(16030151, Pair("\u66f9", "\u91d1\u7389"), 0, 200, 600, Pair(14000010, 16030141)),
            PresetInfo(16030161, Pair("\u5b59", "\u8339"), 14020030, 100, 200, Pair(14020020, 14020041)),
            PresetInfo(16030171, Pair("\u5468", "\u5937"), 14010040, 100, 200)
    )

    // key = type
    fun getSpecPersonsByType():HashMap<Int, MutableList<PresetInfo>>{
        val persons = hashMapOf<Int, MutableList<PresetInfo>>()
        persons[3] = SpecPersonFirstName3
        persons[4] = SpecPersonFirstName4
        persons[5] = SpecPersonFirstName5
        persons[6] = SpecPersonFirstName6
        return persons
    }

    fun getAllSpecPersons():MutableList<PresetInfo>{
        val persons = mutableListOf<PresetInfo>()
        persons.addAll(SpecPersonFirstName2)
        persons.addAll(SpecPersonFirstName3)
        persons.addAll(SpecPersonFirstName4)
        persons.addAll(SpecPersonFirstName5)
        persons.addAll(SpecPersonFirstName6)
        return persons
    }

    //1300 0010
    fun getIdentityIndex(identity:Int):Int{
        return (identity / 10000) % 10
    }
    //13020061
    fun getIdentityGender(identity:Int):NameUtil.Gender{
        return if (identity % 10 == 0 ) NameUtil.Gender.Male else if (identity % 10 == 1) NameUtil.Gender.Female else NameUtil.Gender.Default
    }

    //例 1300 001 0  1 ~ 999
    fun getIdentitySeq(identity:Int):Int{
        return identity.toString().substring(4, 7).toInt()
    }

    fun createLifeTurnName(lifeturn:Int):String{
        return if(lifeturn == 0)
            ""
        else
            "${lifeturn + 1}世"
    }

    //return 1 ~ 999
    fun createIdentitySeq(index:Int):String{
        return  when(index){
            in 0..8 -> "00${index + 1}"
            in 9..98 -> "0${index + 1}"
            else -> "${Math.min(999, index + 1)}"
        }
    }

    val amuletList = mutableListOf("\u6d3b\u529b", "\u6b8b\u66b4", "\u7a33\u56fa", "\u95ea\u7535")
    val amuletWeight = mutableListOf(1, 10, 50, 200, 1000, 5000, 20000)
    val amuletBonus = mutableListOf(5, 10, 15, 20, 30, 40, 50)
    val amuletName = mutableListOf("\u51f9\u51f8", "\u7cbe\u826f", "\u5de5\u5320", "\u73e0\u5b9d\u5320", "\u5927\u5e08", "\u5b97\u5e08", "\u602a\u5f02")
    fun createEquipmentCustom():Pair<String, Int>{
        val sizeRandom = Random().nextInt(50)
        val size = when(sizeRandom) {
            0 -> 2
            in(1..5) -> 1
            else -> 0
        }
        val type = Random().nextInt(amuletList.size)
        var index = amuletWeight.size
        while (index-- > 0){
            if(CultivationHelper.isTrigger(amuletWeight[index])){
                break
            }
        }
        val config = CultivationHelper.mConfig.equipment.filter { it.type == 5}.sortedBy { it.rarity }
        return Pair(config[size].id, "${type + 1}0$index".toInt())
    }

    fun getEquipmentCustom(spec:Pair<String, Int>):Equipment{
        val type = spec.second / 100 - 1
        val rarity = spec.second % 100
        val config = CultivationHelper.mConfig.equipment.find { it.id == spec.first}!!.copy()
        val equipment = Equipment()
        equipment.id = config.id
        equipment.name = config.name
        equipment.seq = spec.second
        equipment.rarity = rarity + config.rarity
        equipment.uniqueName = "${amuletName[rarity]}之${amuletList[type]}${equipment.name}"
        equipment.type = config.type
        val bonus = when(config.rarity) {
            1 -> 1
            2 -> 2
            3 -> 4
            else -> 1
        }
        equipment.property[type] = if(type == 0) amuletBonus[rarity] * bonus * 5 else amuletBonus[rarity] * bonus
        return equipment
    }


    data class PersonFixedInfoMix(var lingGenId:String?, var tianFuIds:MutableList<String>?, var tianFuWeight: Int = 1, var lingGenWeight:Int = 1)
    // type 1 人物信息 assert(person != null), 2 流程信息 assert(battleId != "")
    // person : add lingGen color
    data class HistoryInfo(var type:Int = 0, var content:String, var person: Person?, var battleId:String?)

    class PresetInfo constructor(var identity:Int, var name: Pair<String, String>, var partner:Int = 0, var tianfuWeight:Int = 50, var linggenWeight:Int = 50){
        var profile:Int = 0
        var parent: Pair<Int, Int>? = null

        constructor(identity:Int, name: Triple<String, String, Int>, partner:Int = 0, tianfuWeight:Int = 50, linggenWeight:Int = 50)
                :this(identity, Pair(name.first, name.second), partner, tianfuWeight, linggenWeight){
            profile = name.third
        }

        constructor(identity:Int, name: Pair<String, String>, partner:Int = 0, tianfuWeight:Int = 50, linggenWeight:Int = 50, fixedParent:Pair<Int, Int>)
                :this(identity, name, partner, tianfuWeight, linggenWeight){
            parent = fixedParent
        }

        constructor(identity:Int, name: Triple<String, String, Int>, partner:Int = 0, tianfuWeight:Int = 50, linggenWeight:Int = 50, fixedParent:Pair<Int, Int>)
                :this(identity, name, partner, tianfuWeight, linggenWeight){
            parent = fixedParent
        }
    }

}