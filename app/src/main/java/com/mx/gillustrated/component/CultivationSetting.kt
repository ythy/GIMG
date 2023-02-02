package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.EquipmentConfig
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*
import kotlin.collections.HashMap

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#539B35", "#3B86D4", "#AF85E3", "#FFA500", "#FC2CBB", "#EA5078", "#FFFF00", "#04B4BA","#CFB53B", "#8DFAB1", "#F7FFCC", "#FF0000", "#75ABA7", "#AB9BB7")
    val PostColors = arrayOf("#E2D223", "#BE0012", "#0272E4", "#12A703", "#EF7362")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")
    object Epithet {
        val SingleDefault = "\u8FDB\u58EB"
        val SingleBattle = arrayOf("\u72B6\u5143", "\u699C\u773C", "\u63A2\u82B1", SingleDefault, SingleDefault, SingleDefault, SingleDefault, SingleDefault, SingleDefault, SingleDefault)
    }

    object BattleSettings {
        const val AllianceMinSize = 16
        const val AllianceBonusCount = 4
        const val AllianceMaxXiuwei = 50
        val AllianceBonus = arrayOf(5, 8, 5, 3, 1, 0, 0, 0, 0, 0, 0)// [0]: equipment maxCount, [1..10]: bonus count by BonusCount
        const val ClanMinSize = 4
        const val ClanBonusCount = 3
        const val ClanMaxXiuwei = 20
        val ClanBonus = arrayOf(5, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0)
        const val NationMinSize = 4
        const val NationBonusCount = 3
        val NationBonus = arrayOf(5, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0)
        const val SingleMinSize = 32
        const val SingleBonusCount = 8
        val SingleBonus = arrayOf(5, 30, 20, 15, 10, 6, 4, 2, 1, 0, 0)
    }

    const val SP_JIE_TURN = 999
    const val SP_REDUCE_TURN = 10 //life turn --
    const val SP_TALENT_PROTECT = 30 //天赋
    const val SP_TALENT_EXP = 99 //次数
    const val SP_NAN_9 = 5
    const val SP_NAN_81 = 10
    var TEMP_SP_JIE_TURN = SP_JIE_TURN
    var TEMP_TALENT_PROTECT = SP_TALENT_PROTECT
    var TEMP_TALENT_EXP = SP_TALENT_EXP
    var TEMP_REDUCE_TURN = SP_REDUCE_TURN
    const val LIFE_TIME_YEAR = 100
    val EVENT_WEIGHT = listOf("1200-50", "7200-40", "8400-40", "9600-100")
    const val SP_PUNISH_BOSS_MILLION = 200

    val SpecPersonFirstName: MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983", "\u4ec6", "\u8bcf", "\u536b", "\u8bed", "\u9b41", "\u5f71", "\u8bed", "\u96e8", "\u82B1")
    val SpecPersonFirstNameWeight: Pair<Int, Int> = Pair(50, 20)// tianfu.linggen


    val SpecPersonFirstName2: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(12000010, Triple("\u6bdb", "\u6b23", 1007), 12000021, 1000, 2000),
            PresetInfo(12000021, Triple("\u674e", "\u4e00\u6850", 2005), 12000010, 1000, 2000),
            PresetInfo(12000031, Triple("\u674e", "\u4e00\u6850\u2161", 2003), 12000010, 500, 1000),
            PresetInfo(12000041, Triple("\u6bdb", "\u6c47\u5f64", 2002), 0, 1000, 2000, Pair(12000010, 12000021)),
            PresetInfo(12000051, Pair("\u7403\u7403", "(\u6EDA\u5706)"), 0, 500, 1000, Pair(12000010, 12000031)),

            PresetInfo(12000100, Triple("\u97E9", "\u7EDD", 1004), 12000111, 1000, 6000000),
            PresetInfo(12000111, Triple("\u5211", "\u7EA2\u7487", 1311), 12000100, 200, 600),

            PresetInfo(12000200, Triple("\u674e", "\u900d\u9065", 1001), 12000211, 500, 1000),
            PresetInfo(12000211, Triple("\u963f", "\u5974", 1001), 12000200, 200, 600)

    )

    //13 00 001 0
    private val SpecPersonFirstName3: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(13020011, Triple("\u9ec4", "\u84c9", 1301), 0, 200, 600),
            PresetInfo(13020021, Triple("\u8d75", "\u654f", 1302), 0, 200, 600),
            PresetInfo(13020031, Triple("\u5468", "\u82b7\u82e5", 1303), 0, 200, 600),
            PresetInfo(13020041, Triple("\u4e1c\u65b9", "\u4e0d\u8d25", 1304), 0, 500, 1000),
            PresetInfo(13020051, Triple("\u5c0f", "\u662d", 1305), 0, 100, 200),
            PresetInfo(13020061, Triple("\u674e", "\u79cb\u6c34", 1306), 13020070, 100, 100),
            PresetInfo(13020070, Pair("\u65e0", "\u5d16\u5b50"), 13020061, 100, 200),
            PresetInfo(13020081, Triple("\u6728", "\u5a49\u6e05", 1307), 0, 100, 100),
            PresetInfo(13020091, Triple("\u4efb", "\u76c8\u76c8", 1308), 0, 200, 600),
            PresetInfo(13020101, Triple("\u9EC4\u886B\u5973\u5B50", "", 1310), 0, 500, 1000),
            PresetInfo(13021011, Triple("\u82cf", "\u6a31", 1309), 0, 100, 100),
            PresetInfo(13021021, Triple("\u674e", "\u7ea2\u8896", 1310), 0, 100, 100),
            PresetInfo(13021030, Pair("\u674e", "\u5bfb\u6b22"), 0, 200, 600)
    )

    //achieved immutable
    private val SpecPersonFirstName4: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Triple("\u66f9", "\u64cd", 3047), 16030141, 500, 1000),
            PresetInfo(14000020, Triple("\u53f8\u9a6c", "\u61ff", 3011), 0, 200, 600),
            PresetInfo(14000030, Triple("\u90ed", "\u5609", 3019), 0, 100, 200),
            PresetInfo(14000041, Triple("\u7504", "\u5b93", 3009), 0, 100, 200),
            PresetInfo(14000050, Triple("\u5f20", "\u8fbd", 3034)),
            PresetInfo(14000061, Triple("\u8521", "\u7430", 3010), 0, 100, 100),
            PresetInfo(14000070, Triple("\u590f\u4faf", "\u60c7", 3008)), PresetInfo(14000080, Triple("\u590f\u4faf", "\u6e0a", 3027)),
            PresetInfo(14000090, Triple("\u8340", "\u5f67", 3010)), PresetInfo(14000100, Triple("\u5178", "\u97e6", 3007)),
            PresetInfo(14000110, Triple("\u5f90", "\u6643", 3028)), PresetInfo(14000120, Triple("\u5f20", "\u90c3", 3029)),
            PresetInfo(14000130, Triple("\u4e8e", "\u7981", 3030)), PresetInfo(14000140, Triple("\u4e50", "\u8fdb", 3041)),
            PresetInfo(14000150, Triple("\u9093", "\u827e", 3026)), PresetInfo(14000160, Triple("\u8d3e", "\u8be9", 3009)),

            PresetInfo(14010010, Triple("\u5218", "\u5907", 3045), 14020101, 200, 600),
            PresetInfo(14010020, Triple("\u5173", "\u7fbd", 3020), 16030231, 200, 600),
            PresetInfo(14010030, Triple("\u5f20", "\u98de", 3015), 0, 100, 200),
            PresetInfo(14010040, Triple("\u8d75", "\u4e91", 3044), 14010101, 100, 200),
            PresetInfo(14010050, Triple("\u8bf8\u845b", "\u4eae", 3013), 14010061, 500, 1000),
            PresetInfo(14010061, Triple("\u9ec4", "\u6708\u82f1", 3006), 14010050, 100, 100),
            PresetInfo(14010070, Triple("\u5e9e", "\u7edf", 3012)), PresetInfo(14010080, Triple("\u9a6c", "\u8d85", 3035), 16030111),
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 3014)), PresetInfo(14010101, Triple("\u9a6c", "\u4e91\u7984", 3008), 14010040),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 3032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 3001), 0, 200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 3022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 3031), 14010151),
            PresetInfo(14010151, Triple("\u795d", "\u878d", 3011), 14010140),

            PresetInfo(14020010, Triple("\u5b59", "\u6743", 3046), 16030121, 200, 600),
            PresetInfo(14020020, Triple("\u5b59", "\u7b56", 3003), 14020041, 100, 200),
            PresetInfo(14020030, Triple("\u9646", "\u900a", 3005), 16030161),
            PresetInfo(14020041, Triple("\u5927", "\u4e54", 3005), 14020020, 100, 200),
            PresetInfo(14020051, Triple("\u5c0f", "\u4e54", 3004), 14020060, 100, 200),
            PresetInfo(14020060, Triple("\u5468", "\u745c", 3006), 14020051, 500, 1000),
            PresetInfo(14020070, Triple("\u5468", "\u6cf0", 3040)), PresetInfo(14020080, Triple("\u7518", "\u5b81", 3033)),
            PresetInfo(14020090, Triple("\u592a\u53f2", "\u6148", 3002)), PresetInfo(14020101, Triple("\u5b59", "\u5c1a\u9999", 3003), 14010010, 100, 200),
            PresetInfo(14020110, Triple("\u9ec4", "\u76d6", 3025)), PresetInfo(14020120, Triple("\u5415", "\u8499", 3004)),
            PresetInfo(14020130, Triple("\u5b59", "\u575a", 3018)), PresetInfo(14020140, Triple("\u9c81", "\u8083", 3024)),

            PresetInfo(14030010, Triple("\u5415", "\u5e03", 3016), 16030191, 1000, 2000),
            PresetInfo(14030020, Triple("\u8463", "\u5353", 3001), 0, 200, 600),
            PresetInfo(14030030, Triple("\u9ad8", "\u987a", 3023)),
            PresetInfo(14030041, Triple("\u8c82", "\u8749", 3007), 14030010, 100, 200),
            PresetInfo(14030050, Triple("\u674e", "\u5112", 3021)),
            PresetInfo(14030060, Triple("\u8881", "\u7ecd", 3017), 0, 100, 200),
            PresetInfo(14030070, Triple("\u989c", "\u826f", 3042)), PresetInfo(14030080, Triple("\u6f58", "\u51e4", 3039)),
            PresetInfo(14030090, Triple("\u534e", "\u96c4", 3037)), PresetInfo(14030100, Triple("\u6587", "\u4e11", 3043)),
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 3002), 0, 200, 600, Pair(14030010, 16030191)),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7", 3038), 16030181), PresetInfo(14030130, Triple("\u5218", "\u8868", 3036)),
            PresetInfo(14030140, Triple("\u534e", "\u4f57", 3048))

    )

    private val SpecPersonFirstName6:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(16000010, Triple("\u5b8b","\u6c5f", 4001),0, 100, 200),
            PresetInfo(16000020, Triple("\u6768","\u5fd7",4017)),
            PresetInfo(16000030, Triple("\u5218","\u5510", 4021)),
            PresetInfo(16000040, Triple("\u516c\u5b59","\u80dc", 4004),0, 100, 100),
            PresetInfo(16000051, Triple("\u6248","\u4e09\u5a18", 1501),16060080, 200, 600),
            PresetInfo(16000060, Triple("\u6797","\u51b2", 4006),0, 100, 100),
            PresetInfo(16000070, Triple("\u9c81","\u667a\u6df1", 4013)),
            PresetInfo(16000080, Triple("\u6b66","\u677e", 4014),0, 100, 100),
            PresetInfo(16000090, Triple("\u5434","\u7528", 4003),0, 100, 200),
            PresetInfo(16000100, Triple("\u82b1","\u8363", 4009),0, 100, 200),
            PresetInfo(16000110, Triple("\u5173","\u80DC", 4005)), PresetInfo(16000120, Triple("\u67f4","\u8fdb", 4010)),
            PresetInfo(16000130, Triple("\u674e","\u9035", 4022),0, 100, 200),
            PresetInfo(16000140, Triple("\u6234","\u5b97", 4020)),
            PresetInfo(16000150, Triple("\u5F20","\u6E05", 4016),0,100,100),
            PresetInfo(16000160, Triple("\u674E","\u5E94", 4011),0,100,100),
            PresetInfo(16000170, Triple("\u53F2","\u8FDB", 4023)),
            PresetInfo(16000180, Triple("\u5362","\u4FCA\u4E49", 4002),0, 200, 600),
            PresetInfo(16000190, Triple("\u77F3","\u79C0", 4033)), PresetInfo(16000200, Triple("\u71D5","\u9752", 4036)),
            PresetInfo(16000210, Triple("\u79E6","\u660E", 4007)), PresetInfo(16000220, Triple("\u547C\u5EF6","\u707C", 4008)),
            PresetInfo(16000230, Triple("\u8463","\u5E73", 4015)),
            PresetInfo(16000240, Triple("\u5F90","\u5B81", 4018),0,100,100),
            PresetInfo(16000250, Triple("\u7D22","\u8D85", 4019)),

            PresetInfo(16010010, Pair("\u8521","\u4eac"),0, 100, 100),
            PresetInfo(16010020, Pair("\u9ad8","\u4fc5"),0, 100, 100),
            PresetInfo(16010030, Pair("\u9ad8","\u8859\u5185")),
            PresetInfo(16010040, Pair("\u7ae5","\u8d2f")),
            PresetInfo(16010050, Pair("\u897f\u95e8","\u5e86"),16010061),
            PresetInfo(16010061, Pair("\u6f58","\u91d1\u83b2"),16010050, 100, 100),
            PresetInfo(16010071, Pair("\u674E","\u5E08\u5E08"),0, 100, 200),
            PresetInfo(16010080, Pair("\u6881","\u4E16\u6770")), PresetInfo(16010090, Pair("\u6768","\u622C")),
            PresetInfo(16010100, Pair("\u8D75","\u4F76"),0, 200, 600),

            PresetInfo(16030010, Pair("\u5218", "\u7109"), 0, 100, 100),
            PresetInfo(16030020, Pair("\u7559", "\u8d5e"), 0, 100, 100),
            PresetInfo(16030030, Pair("\u5173", "\u7d22"), 16030081, 200, 600, Pair(14010020, 16030231)),
            PresetInfo(16030041, Pair("\u8d75", "\u8944"), 0, 200, 600, Pair(14010040, 14010101)),
            PresetInfo(16030050, Pair("\u8bb8", "\u52ad"), 0, 100, 200),
            PresetInfo(16030061, Triple("\u738b", "\u8363", 1403), 0, 100, 200),
            PresetInfo(16030071, Pair("\u82b1", "\u9b18"), 16030030, 100, 200, Pair(14010140, 14010151)),
            PresetInfo(16030081, Pair("\u9c8d", "\u4e09\u5a18"), 16030030, 100, 200),
            PresetInfo(16030090, Pair("\u738b", "\u53cc")), PresetInfo(16030100, Pair("\u9648", "\u7433")),
            PresetInfo(16030101, Pair("\u5468", "\u5983"), 0, 100, 100, Pair(14020060, 14020051)),
            PresetInfo(16030111, Triple("\u6768", "\u5a49", 1402), 14010080, 100, 200),
            PresetInfo(16030121, Triple("\u6f58", "\u6dd1", 1405), 14020010, 100, 200),
            PresetInfo(16030131, Pair("\u6a0a", "\u7389\u51e4"), 14010040, 100, 200),
            PresetInfo(16030141, Pair("\u675c", "\u592b\u4eba"), 14000010, 100, 200),
            PresetInfo(16030151, Triple("\u66f9", "\u91d1\u7389", 1407), 0, 200, 600, Pair(14000010, 16030141)),
            PresetInfo(16030161, Pair("\u5b59", "\u8339"), 14020030, 100, 200, Pair(14020020, 14020041)),
            PresetInfo(16030171, Pair("\u5468", "\u5937"), 14010040, 100, 200),
            PresetInfo(16030181, Pair("\u674E", "\u91C7\u8587"), 14030120, 100, 200),
            PresetInfo(16030191, Triple("\u4E25", "\u592B\u4EBA", 1401), 14030010, 100, 200),
            PresetInfo(16030201, Triple("\u8D75", "\u5AE3", 1406), 14020010, 100, 200),
            PresetInfo(16030211, Pair("\u4E01", "\u5C1A\u6DB4"), 14000010),
            PresetInfo(16030221, Pair("\u5C39", "\u592B\u4EBA"), 14000010, 100, 200),
            PresetInfo(16030231, Triple("\u80E1", "\u91D1\u5B9A", 1408), 14010020, 200, 600),

            PresetInfo(16040010, Pair("\u5B59", "\u5927\u5723"), 0, 1000, 2000),
            PresetInfo(16040020, Pair("\u5510", "\u7384\u5958"), 0, 500, 1000),
            PresetInfo(16040030, Pair("\u732A", "\u516B\u6212"), 0, 100, 200),
            PresetInfo(16040040, Pair("\u6C99", "\u609F\u51C0")),
            PresetInfo(16040051, Pair("\u7D2B\u971E", "\u4ED9\u5B50"), 0, 200, 600),
            PresetInfo(16040061, Pair("\u767D", "\u6676\u6676"), 0, 100, 100),
            PresetInfo(16040071, Pair("\u6625", "\u4E09\u5341\u5A18"), 0, 100, 100),
            PresetInfo(16040080, Pair("\u725B", "\u9B54\u738B"), 0, 100, 200),
            PresetInfo(16040091, Pair("\u94C1\u6247", "\u516C\u4E3B"), 0, 100, 200),
            PresetInfo(16040100, Pair("\u5954\u6CE2\u513F\u705E", "")),
            PresetInfo(16040110, Pair("\u516D\u8033\u7315\u7334", ""), 0, 1000, 2000),

            PresetInfo(16050010, Triple("\u7A46","\u5F18", 4024)), PresetInfo(16050020, Triple("\u96F7","\u6A2A", 4025)),
            PresetInfo(16050030, Triple("\u674E","\u4FCA", 4026)), PresetInfo(16050040, Triple("\u962E","\u5C0F\u4E8C", 4027)),
            PresetInfo(16050050, Triple("\u5F20","\u6A2A", 4028)), PresetInfo(16050060, Triple("\u962E","\u5C0F\u4E94", 4029)),
            PresetInfo(16050070, Triple("\u5f20","\u987a", 4030),0,100,100),
            PresetInfo(16050080, Triple("\u962E","\u5C0F\u4E03", 4031)), PresetInfo(16050090, Triple("\u6768","\u96C4", 4032)),
            PresetInfo(16050100, Triple("\u89E3","\u73CD", 4034)), PresetInfo(16050110, Triple("\u89E3","\u5B9D", 4035)),
            PresetInfo(16050120, Triple("\u6731","\u4EDD", 4012)),PresetInfo(16050130, Triple("\u738B","\u5B9A\u516D", 4104)),
            PresetInfo(16050141, Triple("\u987E","\u5927\u5AC2", 1502), 16060140),
            PresetInfo(16050151, Triple("\u5B59","\u4E8C\u5A18", 1503), 16060150, 200, 600),
            PresetInfo(16050160, Triple("\u6731","\u6B66", 4037)),
            PresetInfo(16050170, Triple("\u5B89","\u9053\u5168", 4056), 0, 100, 100),
            PresetInfo(16050180, Triple("\u65F6","\u8FC1", 4107)),PresetInfo(16050190, Triple("\u9879","\u5145", 4064)),
            PresetInfo(16050200, Triple("\u674E","\u886E", 4065)),PresetInfo(16050210, Triple("\u8427","\u8BA9", 4046)),
            PresetInfo(16050220, Triple("\u91D1","\u5927\u575A", 4066)),PresetInfo(16050230, Triple("\u90DD","\u601D\u6587", 4041)),
            PresetInfo(16050240, Triple("\u5355","\u5EF7\u572D", 4044)),PresetInfo(16050250, Triple("\u9B4F","\u5B9A\u56FD", 4045)),

            PresetInfo(16060010, Triple("\u9EC4","\u4FE1", 4038)), PresetInfo(16060020, Triple("\u5B59","\u7ACB", 4039)),
            PresetInfo(16060030, Triple("\u5BA3","\u8D5E", 4040)), PresetInfo(16060040, Triple("\u97E9","\u6ED4", 4042)),
            PresetInfo(16060050, Triple("\u5F6D","\u7398", 4043)), PresetInfo(16060060, Triple("\u5415","\u65B9", 4054)),
            PresetInfo(16060070, Triple("\u90ED","\u76DB", 4055)), PresetInfo(16060080, Triple("\u738B","\u82F1", 4058), 16000051),
            PresetInfo(16060090, Triple("\u6A0A","\u745E", 4061)), PresetInfo(16060100, Triple("\u675C","\u5174", 4088)),
            PresetInfo(16060110, Triple("\u6731","\u8D35", 4091)), PresetInfo(16060120, Triple("\u6731","\u5BCC", 4092)),
            PresetInfo(16060130, Triple("\u8521","\u5E86", 4095)), PresetInfo(16060140, Triple("\u5B59","\u65B0", 4100), 16050141),
            PresetInfo(16060150, Triple("\u5F20","\u9752", 4102), 16050151, 100, 100)
    )

    // key = type
    fun getSpecPersonsByType():HashMap<Int, MutableList<PresetInfo>>{
        val persons = hashMapOf<Int, MutableList<PresetInfo>>()
        persons[3] = SpecPersonFirstName3
        persons[4] = SpecPersonFirstName4
        persons[6] = SpecPersonFirstName6
        return persons
    }

    fun getAllSpecPersons():MutableList<PresetInfo>{
        val persons = mutableListOf<PresetInfo>()
        persons.addAll(SpecPersonFirstName2)
        persons.addAll(SpecPersonFirstName3)
        persons.addAll(SpecPersonFirstName4)
        persons.addAll(SpecPersonFirstName6)
        return persons
    }

    //1 '3' 000010
    fun getIdentityType(identity:Int):Int{
        return (identity / 1000000) % 10
    }

    //13 '00' 0010
    fun getIdentityIndex(identity:Int):Int{
        return (identity / 10000) % 100
    }
    //1302006 '1'
    fun getIdentityGender(identity:Int):NameUtil.Gender{
        return if (identity % 10 == 0 ) NameUtil.Gender.Male else if (identity % 10 == 1) NameUtil.Gender.Female else NameUtil.Gender.Default
    }

    //例 1300 '001' 0  1 ~ 999
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

    fun createDecadeSeq(index:Int):String{
        return  when(index){
            in 0..9 -> "0$index"
            else -> "${Math.min(99, index)}"
        }
    }

    /*
        ## custom type 8
        rarity:
        12 - 400 teji2 xiuwei300
        11 - 300 teji 1/2 xiuwei200
        10 - 200 teji xiuwei 0/50/100/200
        9 - 150 xiuwei100
        8 - 100 xiuwei50 hp300
        7 - 50 xiuwei50 hp200

        ## label
        2 20
        3 30
        4 40

        6 100, 50, -:-, 50
        7 500, 100, 50:50, 100
        8 2000, 150, 100:100, 150

        9 1 0000 -, 150:150, 200, 150+ ling
        10 5 0000 -, 200:200, 300, 200+100:100 jian/zhan
        11 25 0000 -, 300:300, 400, 300+200:200 yao
        12 100 0000 sheng
        13 200 0000 xian/mie

     */
    // weight: max 10000
    data class AmuletType(val id:Int, val name:String, val weight:Int, val rarityBonus:Int, val addProperty:MutableList<Boolean>,
                          val addXiuwei:Boolean, val props:MutableList<AmuletProps>, val config:MutableList<AmuletConfig>)
    data class AmuletProps(val id:Int, val weight:Int, val rarityBase:Int, val bonus:Int, val xiuwei:Int, val prefix:String, val teji:MutableList<String> = mutableListOf())
    data class AmuletConfig(val equipmentId:String, val weight:Int, val rarityBonus:Int, val propsMulti:Int)

    private object Amulet {
        val configSmall = mutableListOf(AmuletConfig("7005101",1, 0, 1))
        val configLarge = mutableListOf(AmuletConfig("7005102",50, 1, 2))
        val configGrand = mutableListOf(AmuletConfig("7005103",500, 2, 4))
        val configNecklace = mutableListOf(AmuletConfig("7005104",1, 0, 1))
        val configRing = mutableListOf(AmuletConfig("7005105",1, 0, 1))
        val configNormal = mutableListOf(configSmall[0], configLarge[0], configGrand[0])

        val propsNormal = mutableListOf(
                AmuletProps(0,1, 1,5, 10, "\u51f9\u51f8\u4e4b"),
                AmuletProps(1,10, 2, 10, 20, "\u7cbe\u826f\u4e4b"),
                AmuletProps(2,50, 3, 15, 30, "\u5de5\u5320\u4e4b"),
                AmuletProps(3,200, 4, 20, 40, "\u73e0\u5b9d\u5320\u4e4b"),
                AmuletProps(4,1000,5, 30, 50, "\u5927\u5e08\u4e4b"),
                AmuletProps(5,5000, 6, 40, 80, "\u5b97\u5e08\u4e4b"),
                AmuletProps(6,20000, 7,50, 100, "\u795e\u5320\u4e4b")
        )

        val propsTal = mutableListOf(AmuletProps(0,1,9,50, 100, "", mutableListOf("8001005")))
        val propsTorch = mutableListOf(AmuletProps(0,1,9,100, 100, ""))
        val propsGheed = mutableListOf(AmuletProps(0,1,6,40, 100, ""))
        val propsJordan = mutableListOf(AmuletProps(0,1,9,100, 200, ""))
        val propsBul = mutableListOf(AmuletProps(0,1,10,100, 200, "", mutableListOf("8003006")))
        val propsNagel = mutableListOf(AmuletProps(0,1,4,0, 50, ""))
        val propsRaven = mutableListOf(AmuletProps(0,1,5,50, 0, ""))
        val propsMara = mutableListOf(AmuletProps(0,1,6,50, 100, ""))

        val types = mutableListOf(
                AmuletType(111,  "\u602a\u5f02", 50, 1, mutableListOf(false,true,true,false), false, propsNormal, configNormal),
                AmuletType(112,  "\u6B8B\u66B4", 50, 1, mutableListOf(false,true,false,true), false, propsNormal, configNormal),
                AmuletType(113,  "\u6bc1\u706d", 100,2, mutableListOf(true,true,true,true), false, propsNormal, configNormal),
                AmuletType(114,  "\u4e0d\u673d", 100,2, mutableListOf(true,true,false,false), true, propsNormal, configNormal),

                AmuletType(201,  "\u5854-\u62c9\u590f\u7684\u5224\u51b3", 5000,0, mutableListOf(false,true,false,true), true, propsTal, configNecklace),
                AmuletType(202,  "\u5730\u72f1\u706b\u70ac", 5000,0, mutableListOf(true,false,true,false), true, propsTorch, configLarge),
                AmuletType(203,  "\u57fa\u5fb7\u7684\u8fd0\u6c14", 2000,0, mutableListOf(false,false,false,true), true, propsGheed, configGrand),
                AmuletType(204,  "\u4e54\u4e39\u4e4b\u77f3", 5000,0, mutableListOf(true,false,false,false), true, propsJordan, configRing),
                AmuletType(205,  "\u5e03\u5c14\u51ef\u7d22\u4e4b\u6212", 5000,0, mutableListOf(false,true,false,false), false, propsBul, configRing),
                AmuletType(206,  "\u62ff\u5404\u7684\u6212\u6307", 200,0, mutableListOf(false,false,false,false), true, propsNagel, configRing),
                AmuletType(207,  "\u4e4c\u9e26\u4e4b\u971c", 200,0, mutableListOf(true,false,false,true), false, propsRaven, configRing),
                AmuletType(208,  "\u739B\u62C9\u7684\u4E07\u82B1\u7B52", 1000,0, mutableListOf(false,false,true,false), true, propsMara, configNecklace)
        )

    }

    // SEQ like 20101  5位
    fun createEquipmentCustom(fixType:Int = 0):Pair<String, Int>?{
        //↓ 选取type
        var amuletType:AmuletType? = null
        if(fixType == 0){
            Amulet.types.toMutableList().sortedByDescending { it.weight }.forEach {
                if(amuletType == null && CultivationHelper.isTrigger(it.weight) ){
                    amuletType = it
                }
            }
        }else{
            amuletType = Amulet.types.find { it.id == fixType}
        }
        if (amuletType == null)
            return null
        //↓ 选取props
        var props:AmuletProps? = null
        if(amuletType!!.props.size == 1){
            props = amuletType!!.props[0]
        }else{
            amuletType!!.props.sortedByDescending { it.weight }.forEach {
                if(props == null && CultivationHelper.isTrigger(it.weight) ){
                    props = it
                }
            }
        }
        //↓ 选取equipment
        var config:AmuletConfig? = null
        if(amuletType!!.config.size == 1){
            config = amuletType!!.config[0]
        }else {
            amuletType!!.config.sortedByDescending { it.weight }.forEach {
                if (config == null && CultivationHelper.isTrigger(it.weight)) {
                    config = it
                }
            }
        }
        return Pair(config!!.equipmentId, "${amuletType!!.id}${createDecadeSeq(props!!.id)}".toInt())
    }

    fun getEquipmentCustom(id:String, seq:Int):Pair<EquipmentConfig, String>{
        val amuletType = Amulet.types.find { it.id == seq / 100 }!!
        val props = amuletType.props.find { it.id == seq % 100 } ?: amuletType.props[0]
        val config = amuletType.config.find { it.equipmentId == id } ?: amuletType.config[0]
        val equipmentConfig = CultivationHelper.mConfig.equipment.find { it.id == config.equipmentId}!!
        val configPropsMulti = if(amuletType.config.size == 1) 1 else config.propsMulti
        val configRarityBonus = if(amuletType.config.size == 1) 0 else config.rarityBonus

        val property:MutableList<Int> = mutableListOf(0,0,0,0,0,0,0,0)
        property.take(4).forEachIndexed { index, _ ->
            if (amuletType.addProperty[index]){
                property[index] =  props.bonus * configPropsMulti * (if (index == 0) 5 else 1)
            }else{
                property[index] = 0
            }
        }
        return Pair(EquipmentConfig(
                equipmentConfig.id,
                equipmentConfig.name,
                equipmentConfig.type,
                props.rarityBase + amuletType.rarityBonus + configRarityBonus,
                if(amuletType.addXiuwei) props.xiuwei * configPropsMulti else 0,
                0,
                property,
                mutableListOf(),
                mutableListOf(),
                props.teji,
                mutableListOf()
        ),  if (amuletType.props.size == 1) amuletType.name else "${props.prefix}${amuletType.name}${equipmentConfig.name}")
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

