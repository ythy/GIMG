package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person
import kotlin.collections.HashMap
import kotlin.math.min

object CultivationSetting {

    val CommonColors = arrayOf("#E1DAD3", "#539B35", "#3B86D4", "#8850CD", "#FF8C00", "#FC2CBB", "#FE4B4B", "#FFD700", "#04B4BA","#BDA072", "#CBD289", "#B8DAD2", "#E8D3C0", "#E7C2D8", "#DE7487", "#FFFFFF")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")


    object BattleSettings {
        const val AllianceMinSize = 16
        const val AllianceBonusCount = 4
        const val AllianceMaxXiuwei = 50
        val AllianceBonus = arrayOf(5, 8, 5, 3, 1, 0, 0, 0, 0, 0, 0)// [0]: equipment maxCount, [1..10]: bonus count by BonusCount
        const val ClanMinSize = 4
        const val ClanBonusCount = 3
        const val ClanMaxXiuwei = 20
        val ClanBonus = arrayOf(5, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0)
//        const val NationMinSize = 4
//        const val NationBonusCount = 3
//        val NationBonus = arrayOf(5, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0)
        const val SingleMinSize = 32
        const val SingleBonusCount = 8
        val SingleBonus = arrayOf(5, 30, 20, 15, 10, 6, 4, 2, 1, 0, 0)
    }

    const val SP_JIE_TURN = 9
    const val SP_TALENT_PROTECT = 40 //天赋
    const val SP_DEAD_SYMBOL = "🔪"
    const val SP_SKIN_BATTLE_MIN = 9
    var TEMP_SP_JIE_TURN = SP_JIE_TURN
    var TEMP_TALENT_PROTECT = SP_TALENT_PROTECT
    var TEMP_DEAD_SYMBOL = SP_DEAD_SYMBOL
    var TEMP_SKIN_BATTLE_MIN = SP_SKIN_BATTLE_MIN
    const val LIFE_TIME_YEAR = 100
    val EVENT_WEIGHT = listOf("1000-10", "2000-10", "2000-10", "5000-10")
    const val SP_PUNISH_BOSS_MILLION = 200

    val SpecPersonFirstName2: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(12000010, Triple("\u6bdb", "\u6b23", 1002), 12000021, 1000, 2000),
            PresetInfo(12000021, Triple("\u827E", "\u60C5", 2005), 12000010, 1000, 2000),
            PresetInfo(12000031, Triple("\u8C2D", "\u768E", 2003), 12000010, 500, 1000),
            PresetInfo(12000041, Triple("\u6bdb", "\u6c47\u5f64", 2002), 0, 1000, 2000, Pair(12000010, 12000021)),
            PresetInfo(12000051, Pair("\u7403\u7403", "(\u6EDA\u5706)"), 0, 500, 1000, Pair(12000010, 12000031)),

            PresetInfo(12000100, Triple("\u97E9", "\u7EDD", 1003), 12000111, 1000, 6000000),
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
            PresetInfo(13020061, Triple("\u963F", "\u6731", 1306), 0, 100, 100),
            PresetInfo(13020070, Pair("\u6D2A", "\u4E03\u516C"), 0, 100, 200),
            PresetInfo(13020081, Triple("\u6728", "\u5a49\u6e05", 1307), 0, 100, 100),
            PresetInfo(13020091, Triple("\u4efb", "\u76c8\u76c8", 1308), 0, 200, 600),
            PresetInfo(13020101, Triple("\u9EC4\u886B\u5973\u5B50", "", 1310), 0, 500, 1000),
            PresetInfo(13020111, Triple("\u7A46", "\u5FF5\u6148", 1313), 0, 100, 200),
            PresetInfo(13021011, Triple("\u82cf", "\u6a31", 1309), 0, 100, 100),
            PresetInfo(13021021, Triple("\u674e", "\u7ea2\u8896", 1310), 0, 100, 100),
            PresetInfo(13021030, Pair("\u674e", "\u5bfb\u6b22"), 0, 200, 600),
            PresetInfo(13021041, Triple("\u6731", "\u6CEA\u513F", 1314), 0, 100, 200)
    )

    //achieved immutable
    private val SpecPersonFirstName4: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Triple("\u66f9", "\u64cd", 3047), 0, 500, 1000),
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
            PresetInfo(14010020, Triple("\u5173", "\u7fbd", 3020), 0, 200, 600),
            PresetInfo(14010030, Triple("\u5f20", "\u98de", 3015), 0, 100, 200),
            PresetInfo(14010040, Triple("\u8d75", "\u4e91", 3044), 14010101, 100, 200),
            PresetInfo(14010050, Triple("\u8bf8\u845b", "\u4eae", 3013), 14010061, 500, 1000),
            PresetInfo(14010061, Triple("\u9ec4", "\u6708\u82f1", 3006), 14010050, 100, 100),
            PresetInfo(14010070, Triple("\u5e9e", "\u7edf", 3012)), PresetInfo(14010080, Triple("\u9a6c", "\u8d85", 3035)),
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 3014)), PresetInfo(14010101, Triple("\u9a6c", "\u4e91\u7984", 3008), 14010040),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 3032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 3001), 0, 200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 3022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 3031), 14010151),
            PresetInfo(14010151, Triple("\u795d", "\u878d", 3011), 14010140),

            PresetInfo(14020010, Triple("\u5b59", "\u6743", 3046), 0, 200, 600),
            PresetInfo(14020020, Triple("\u5b59", "\u7b56", 3003), 14020041, 100, 200),
            PresetInfo(14020030, Triple("\u9646", "\u900a", 3005)),
            PresetInfo(14020041, Triple("\u5927", "\u4e54", 3005), 14020020, 100, 200),
            PresetInfo(14020051, Triple("\u5c0f", "\u4e54", 3004), 14020060, 100, 200),
            PresetInfo(14020060, Triple("\u5468", "\u745c", 3006), 14020051, 500, 1000),
            PresetInfo(14020070, Triple("\u5468", "\u6cf0", 3040)), PresetInfo(14020080, Triple("\u7518", "\u5b81", 3033)),
            PresetInfo(14020090, Triple("\u592a\u53f2", "\u6148", 3002)), PresetInfo(14020101, Triple("\u5b59", "\u5c1a\u9999", 3003), 14010010, 100, 200),
            PresetInfo(14020110, Triple("\u9ec4", "\u76d6", 3025)), PresetInfo(14020120, Triple("\u5415", "\u8499", 3004)),
            PresetInfo(14020130, Triple("\u5b59", "\u575a", 3018)), PresetInfo(14020140, Triple("\u9c81", "\u8083", 3024)),

            PresetInfo(14030010, Triple("\u5415", "\u5e03", 3016), 14030041, 1000, 2000),
            PresetInfo(14030020, Triple("\u8463", "\u5353", 3001), 0, 200, 600),
            PresetInfo(14030030, Triple("\u9ad8", "\u987a", 3023)),
            PresetInfo(14030041, Triple("\u8c82", "\u8749", 3007), 14030010, 100, 200),
            PresetInfo(14030050, Triple("\u674e", "\u5112", 3021)),
            PresetInfo(14030060, Triple("\u8881", "\u7ecd", 3017), 0, 100, 200),
            PresetInfo(14030070, Triple("\u989c", "\u826f", 3042)), PresetInfo(14030080, Triple("\u6f58", "\u51e4", 3039)),
            PresetInfo(14030090, Triple("\u534e", "\u96c4", 3037)), PresetInfo(14030100, Triple("\u6587", "\u4e11", 3043)),
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 3002), 0, 200, 600, Pair(14030010, 14030041)),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7", 3038)), PresetInfo(14030130, Triple("\u5218", "\u8868", 3036)),
            PresetInfo(14030140, Triple("\u534e", "\u4f57", 3048))

    )
    //6
    private val SpecPersonFirstName5: MutableList<PresetInfo> = mutableListOf(
            PresetInfo(15000011, Triple("\u6674\u96EF", "", 1201), 0, 200, 600),
            PresetInfo(15000021, Triple("\u6797", "\u9EDB\u7389", 1202), 0, 200, 600),
            PresetInfo(15000031, Triple("\u859B", "\u5B9D\u9497", 1203), 0, 200, 600),
            PresetInfo(15000041, Triple("\u674E", "\u7EA8", 1204), 0, 100, 200),
            PresetInfo(15000051, Triple("\u79E6", "\u53EF\u537F", 1205), 0, 200, 400),
            PresetInfo(15000061, Triple("\u53F2", "\u6E58\u4E91", 1206), 0, 100, 200),
            PresetInfo(15000071, Triple("\u8D3E", "\u5143\u6625", 1207), 0, 100, 200),
            PresetInfo(15000081, Triple("\u90A2", "\u5CAB\u70DF", 1208), 0, 100, 200),
            PresetInfo(15000091, Triple("\u5E73\u513F", "", 1209), 0, 100, 200),
            PresetInfo(15000101, Triple("\u9999\u83F1", "", 1210), 0, 100, 200),
            PresetInfo(15000111, Triple("\u5999\u7389", "", 1211), 0, 100, 200),
            PresetInfo(15000121, Triple("\u88AD\u4EBA", "", 1212), 0, 100, 200)
    )

    private val SpecPersonFirstName6:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(16040010, Pair("\u5B59", "\u609F\u7A7A"), 0, 1000, 2000),
            PresetInfo(16040020, Pair("\u5510", "\u7384\u5958"), 0, 500, 1000),
            PresetInfo(16040030, Pair("\u732A", "\u516B\u6212"), 0, 100, 200),
            PresetInfo(16040040, Pair("\u6C99", "\u609F\u51C0")),
            PresetInfo(16040051, Pair("\u7D2B\u971E", "\u4ED9\u5B50"), 0, 200, 600),
            PresetInfo(16040061, Pair("\u767D", "\u6676\u6676"), 0, 100, 100),
            PresetInfo(16040071, Pair("\u9752\u971E", "\u4ED9\u5B50"), 0, 100, 100),
            PresetInfo(16040080, Pair("\u725B", "\u9B54\u738B"), 0, 100, 200),
            PresetInfo(16040091, Pair("\u94C1\u6247", "\u516C\u4E3B"), 0, 100, 200),
            PresetInfo(16040100, Pair("\u5954\u6CE2\u513F\u705E", "")),
            PresetInfo(16040110, Pair("\u516D\u8033\u7315\u7334", ""), 0, 1000, 2000),
            PresetInfo(16040120, Pair("\u7389\u7687\u5927\u5E1D", ""), 0, 2000, 5000),
            PresetInfo(16040131, Pair("\u89C2\u97F3", ""), 0, 2000, 5000),
            PresetInfo(16040140, Pair("\u5982\u6765", ""), 0, 2000, 5000),
            PresetInfo(16040150, Pair("\u5730\u85CF\u738B", ""), 0, 100, 200),
            PresetInfo(16040161, Pair("\u5B5F\u5A46", ""), 0, 1000, 2000),
            PresetInfo(16040170, Pair("\u949F", "\u9997"), 0, 200, 600),
            PresetInfo(16040180, Pair("\u8881", "\u5929\u7F61"), 0, 100, 200)
    )

    //achieved immutable
    private val SpecPersonFirstName7:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(17000010, Triple("\u5b8b","\u6c5f", 4001),0, 100, 200),
            PresetInfo(17000020, Triple("\u5362","\u4FCA\u4E49", 4002),0, 200, 600),
            PresetInfo(17000030, Triple("\u5434","\u7528", 4003),0, 100, 200),
            PresetInfo(17000040, Triple("\u516c\u5b59","\u80dc", 4004),0, 100, 100),
            PresetInfo(17000050, Triple("\u5173","\u80DC", 4005),0, 100, 100),
            PresetInfo(17000060, Triple("\u6797","\u51b2", 4006),0, 100, 200),
            PresetInfo(17000070, Triple("\u79E6","\u660E", 4007)),
            PresetInfo(17000080, Triple("\u547C\u5EF6","\u707C", 4008),0, 100, 100),
            PresetInfo(17000090, Triple("\u82b1","\u8363", 4009),0, 100, 200),
            PresetInfo(17000100, Triple("\u67f4","\u8fdb", 4010)),
            PresetInfo(17000110, Triple("\u674E","\u5E94", 4011)),
            PresetInfo(17000120, Triple("\u6731","\u4EDD", 4012)),
            PresetInfo(17000130, Triple("\u9c81","\u667a\u6df1", 4013),0, 100, 100),
            PresetInfo(17000140, Triple("\u6b66","\u677e", 4014),0, 100, 100),
            PresetInfo(17000150, Triple("\u8463","\u5E73", 4015)),
            PresetInfo(17000160, Triple("\u5F20","\u6E05", 4016),0,100,200),
            PresetInfo(17000170, Triple("\u6768","\u5fd7",4017)), PresetInfo(17000180, Triple("\u5F90","\u5B81", 4018)),
            PresetInfo(17000190, Triple("\u7D22","\u8D85", 4019)), PresetInfo(17000200, Triple("\u6234","\u5b97", 4020)),
            PresetInfo(17000210, Triple("\u5218","\u5510", 4021)),
            PresetInfo(17000220, Triple("\u674e","\u9035", 4022),0, 100, 200),
            PresetInfo(17000230, Triple("\u53F2","\u8FDB", 4023)), PresetInfo(17000240, Triple("\u7A46","\u5F18", 4024)),
            PresetInfo(17000250, Triple("\u96F7","\u6A2A", 4025)), PresetInfo(17000260, Triple("\u674E","\u4FCA", 4026)),
            PresetInfo(17000270, Triple("\u962E","\u5C0F\u4E8C", 4027)), PresetInfo(17000280, Triple("\u5F20","\u6A2A", 4028)),
            PresetInfo(17000290, Triple("\u962E","\u5C0F\u4E94", 4029)),
            PresetInfo(17000300, Triple("\u5f20","\u987a", 4030),0,100,100),
            PresetInfo(17000310, Triple("\u962E","\u5C0F\u4E03", 4031)), PresetInfo(17000320, Triple("\u6768","\u96C4", 4032)),
            PresetInfo(17000330, Triple("\u77F3","\u79C0", 4033)), PresetInfo(17000340, Triple("\u89E3","\u73CD", 4034)),
            PresetInfo(17000350, Triple("\u89E3","\u5B9D", 4035)), PresetInfo(17000360, Triple("\u71D5","\u9752", 4036)),


            PresetInfo(17020370, Triple("\u6731","\u6B66", 4037)), PresetInfo(17020380, Triple("\u9EC4","\u4FE1", 4038)),
            PresetInfo(17020390, Triple("\u5B59","\u7ACB", 4039)), PresetInfo(17020400, Triple("\u5BA3","\u8D5E", 4040)),
            PresetInfo(17020410, Triple("\u90DD","\u601D\u6587", 4041)), PresetInfo(17020420, Triple("\u97E9","\u6ED4", 4042)),
            PresetInfo(17020430, Triple("\u5F6D","\u7398", 4043)), PresetInfo(17020440, Triple("\u5355","\u5EF7\u572D", 4044)),
            PresetInfo(17020450, Triple("\u9B4F","\u5B9A\u56FD", 4045)), PresetInfo(17020460, Triple("\u8427","\u8BA9", 4046)),
            PresetInfo(17020470, Triple("\u88F4","\u5BA3", 4047)), PresetInfo(17020480, Triple("\u6B27","\u9E4F", 4048)),
            PresetInfo(17020490, Triple("\u9093","\u98DE", 4049)), PresetInfo(17020500, Triple("\u71D5","\u987A", 4050)),
            PresetInfo(17020510, Triple("\u6768","\u6797", 4051)), PresetInfo(17020520, Triple("\u51CC","\u632F", 4052)),
            PresetInfo(17020530, Triple("\u848B","\u656C", 4053)), PresetInfo(17020540, Triple("\u5415","\u65B9", 4054)),
            PresetInfo(17020550, Triple("\u90ED","\u76DB", 4055)),
            PresetInfo(17020560, Triple("\u5B89","\u9053\u5168", 4056), 0, 100, 100),
            PresetInfo(17020570, Triple("\u7687\u752B","\u7AEF", 4057)),
            PresetInfo(17020580, Triple("\u738B","\u82F1", 4058), 17020591),
            PresetInfo(17020591, Triple("\u6248","\u4e09\u5a18", 1501),17020580, 200, 600),
            PresetInfo(17020600, Triple("\u90ED","\u76DB", 4060)),
            PresetInfo(17020610, Triple("\u6A0A","\u745E", 4061)), PresetInfo(17020620, Triple("\u5B54","\u660E", 4062)),
            PresetInfo(17020630, Triple("\u5B54","\u4EAE", 4063)), PresetInfo(17020640, Triple("\u9879","\u5145", 4064)),
            PresetInfo(17020650, Triple("\u674E","\u886E", 4065)), PresetInfo(17020660, Triple("\u91D1","\u5927\u575A", 4066)),
            PresetInfo(17020670, Triple("\u9A6C","\u9E9F", 4067)), PresetInfo(17020680, Triple("\u7AE5","\u5A01", 4068)),
            PresetInfo(17020690, Triple("\u7AE5","\u731B", 4069)), PresetInfo(17020700, Triple("\u5B5F","\u5EB7", 4070)),
            PresetInfo(17020710, Triple("\u4FAF","\u5EFA", 4071)), PresetInfo(17020720, Triple("\u9648","\u8FBE", 4072)),
            PresetInfo(17020730, Triple("\u6768","\u6625", 4073)), PresetInfo(17020740, Triple("\u90D1","\u5929\u5BFF", 4074)),
            PresetInfo(17020750, Triple("\u9676","\u5B97\u65FA", 4075)), PresetInfo(17020760, Triple("\u5B8B","\u6E05", 4076)),
            PresetInfo(17020770, Triple("\u4E50","\u548C", 4077)), PresetInfo(17020780, Triple("\u9F9A","\u65FA", 4078)),
            PresetInfo(17020790, Triple("\u4E01","\u5F97\u5B59", 4079)), PresetInfo(17020800, Triple("\u7A46","\u6625", 4080)),
            PresetInfo(17020810, Triple("\u66F9","\u6B63", 4081)), PresetInfo(17020820, Triple("\u5B8B","\u4E07", 4082)),
            PresetInfo(17020830, Triple("\u675C","\u8FC1", 4083)), PresetInfo(17020840, Triple("\u859B","\u6C38", 4084)),
            PresetInfo(17020850, Triple("\u674E","\u5FE0", 4085)), PresetInfo(17020860, Triple("\u5468","\u901A", 4086)),
            PresetInfo(17020870, Triple("\u6C64","\u9686", 4087)), PresetInfo(17020880, Triple("\u675C","\u5174", 4088)),
            PresetInfo(17020890, Triple("\u90B9","\u6E0A", 4089)), PresetInfo(17020900, Triple("\u90B9","\u6DA6", 4090)),
            PresetInfo(17020910, Triple("\u6731","\u8D35", 4091)), PresetInfo(17020920, Triple("\u6731","\u5BCC", 4092)),
            PresetInfo(17020930, Triple("\u65BD","\u6069", 4093)), PresetInfo(17020940, Triple("\u8521","\u798F", 4094)),
            PresetInfo(17020950, Triple("\u8521","\u5E86", 4095)), PresetInfo(17020960, Triple("\u674E","\u7ACB", 4096)), PresetInfo(17020970, Triple("\u674E","\u4E91", 4097)),
            PresetInfo(17020980, Triple("\u7126","\u633A", 4098)), PresetInfo(17020990, Triple("\u77F3","\u52C7", 4099)),
            PresetInfo(17021000, Triple("\u5B59","\u65B0", 4100), 17021011),
            PresetInfo(17021011, Triple("\u987E","\u5927\u5AC2", 1502), 17021000),
            PresetInfo(17021020, Triple("\u5F20","\u9752", 4102), 17021031, 100, 100),
            PresetInfo(17021031, Triple("\u5B59","\u4E8C\u5A18", 1503), 17021020, 200, 600),
            PresetInfo(17021040, Triple("\u738B","\u5B9A\u516D", 4104)), PresetInfo(17021050, Triple("\u90C1","\u4FDD\u56DB", 4105)),
            PresetInfo(17021060, Triple("\u767D","\u80DC", 4106)), PresetInfo(17021070, Triple("\u65F6","\u8FC1", 4107)),
            PresetInfo(17021080, Triple("\u6BB5","\u666F\u67F1", 4108))
    )


    // key = type
    fun getSpecPersonsByType():HashMap<Int, MutableList<PresetInfo>>{
        val persons = hashMapOf<Int, MutableList<PresetInfo>>()
        persons[3] = SpecPersonFirstName3
        persons[4] = SpecPersonFirstName4
        persons[5] = SpecPersonFirstName5
        persons[6] = SpecPersonFirstName6
        persons[7] = SpecPersonFirstName7
        return persons
    }

    fun getAllSpecPersons():MutableList<PresetInfo>{
        val persons = mutableListOf<PresetInfo>()
        persons.addAll(SpecPersonFirstName2)
        persons.addAll(SpecPersonFirstName3)
        persons.addAll(SpecPersonFirstName4)
        persons.addAll(SpecPersonFirstName5)
        persons.addAll(SpecPersonFirstName6)
        persons.addAll(SpecPersonFirstName7)
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


    data class PersonFixedInfoMix(var lingGenId:String?, var tianFuIds:MutableList<String>?, var tianFuWeight: Int = 1, var lingGenWeight:Int = 1)
    // type 1 人物信息 assert(person != null), 2 流程信息 assert(battleId != "")
    // person : add lingGen color
    data class HistoryInfo(var xun:Long, var type:Int = 0, var content:String, var person: Person?, var battleId:String?)

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

