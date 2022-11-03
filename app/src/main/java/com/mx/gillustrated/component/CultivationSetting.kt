package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    val PostColors = arrayOf("#E2D223", "#BE0012", "#0272E4", "#12A703", "#EF7362")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")

    object BattleSettings {
        const val AllianceMinSize = 16
        const val AllianceBonusCount = 4
        val AllianceBonus = arrayOf(5,8,5,3,1,0,0,0,0,0,0)
        const val ClanMinSize = 4
        const val ClanBonusCount = 3
        val ClanBonus = arrayOf(5,3,2,1,0,0,0,0,0,0,0)
        const val NationMinSize = 4
        const  val NationBonusCount = 3
        val NationBonus = arrayOf(5,3,2,1,0,0,0,0,0,0,0)
        const val SingleMinSize = 32
    }

    const val SP_JIE_TURN = 81
    val EVENT_WEIGHT = listOf("1200-50","7200-40","8400-40","9600-100")
    const val SP_PUNISH_BOSS_MILLION = 200

    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983", "\u4ec6", "\u8bcf", "\u536b", "\u8bed")
    val SpecPersonFirstNameWeight:Pair<Int, Int> = Pair(50, 20)// tianfu.linggen

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
            PresetInfo(12000121, Pair("\u57ce\u6237","\u7eb1\u7ec7"),12000110, 1000, 600000)
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
            PresetInfo(13020061, Pair("\u674e","\u79cb\u6c34"),0, 100, 100),
            PresetInfo(13020091, Pair("\u4efb","\u76c8\u76c8"), 0, 200, 600)
    )

    private val SpecPersonFirstName4:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Triple("\u66f9", "\u64cd", 2047),0,500, 1000),
            PresetInfo(14000020, Triple("\u53f8\u9a6c", "\u61ff", 2011), 0,200, 600),
            PresetInfo(14000030, Triple("\u90ed", "\u5609", 2019), 0,100, 200),
            PresetInfo(14000041, Triple("\u7504", "\u5b93", 4009), 0, 100, 200),
            PresetInfo(14000050, Triple("\u5f20", "\u8fbd", 2034)),
            PresetInfo(14000061, Triple("\u8521", "\u7430", 4010), 0,100, 100),
            PresetInfo(14000070, Triple("\u590f\u4faf","\u60c7", 2008)), PresetInfo(14000080, Triple("\u590f\u4faf","\u6e0a", 2027)),
            PresetInfo(14000090, Triple("\u8340", "\u5f67", 2010)), PresetInfo(14000100, Triple("\u5178","\u97e6", 2007)),
            PresetInfo(14000110, Triple("\u5f90","\u6643", 2028)), PresetInfo(14000120, Triple("\u5f20","\u90c3", 2029)),
            PresetInfo(14000130, Triple("\u4e8e","\u7981", 2030)), PresetInfo(14000140, Triple("\u4e50","\u8fdb", 2041)),
            PresetInfo(14000150, Triple("\u9093","\u827e", 2026)), PresetInfo(14000160, Triple("\u8d3e", "\u8be9", 2009)),

            PresetInfo(14010010, Triple("\u5218", "\u5907", 2045), 14020101,200, 600),
            PresetInfo(14010020, Triple("\u5173", "\u7fbd", 2020), 0,200, 600),
            PresetInfo(14010030, Triple("\u5f20", "\u98de", 2015),0,100, 200),
            PresetInfo(14010040, Triple("\u8d75", "\u4e91", 2044), 14010101,100, 200),
            PresetInfo(14010050, Triple("\u8bf8\u845b", "\u4eae", 2013), 14010061,500, 1000),
            PresetInfo(14010061, Triple("\u9ec4", "\u6708\u82f1", 4006), 14010050,100, 100),
            PresetInfo(14010070, Triple("\u5e9e","\u7edf", 2012)), PresetInfo(14010080, Triple("\u9a6c","\u8d85", 2035)),
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 2014)), PresetInfo(14010101, Triple("\u9a6c","\u4e91\u7984", 4008), 14010040),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 2032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 4001), 0,200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 2022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 2031), 14010151),
            PresetInfo(14010151, Triple("\u795d", "\u878d", 4011), 14010140),

            PresetInfo(14020010, Triple("\u5b59", "\u6743", 2046), 0,200, 600),
            PresetInfo(14020020, Triple("\u5b59", "\u7b56", 2003),14020041,100, 200),
            PresetInfo(14020030, Triple("\u9646", "\u900a", 2005)),
            PresetInfo(14020041, Triple("\u5927", "\u4e54",4005), 14020020,100, 200),
            PresetInfo(14020051, Triple("\u5c0f", "\u4e54",4004), 14020060,100, 200),
            PresetInfo(14020060, Triple("\u5468", "\u745c", 2006), 14020051,500, 1000),
            PresetInfo(14020070, Triple("\u5468","\u6cf0",2040)), PresetInfo(14020080, Triple("\u7518","\u5b81", 2033)),
            PresetInfo(14020090, Triple("\u592a\u53f2", "\u6148", 2002)), PresetInfo(14020101, Triple("\u5b59","\u5c1a\u9999", 4003), 14010010, 100, 200),
            PresetInfo(14020110, Triple("\u9ec4", "\u76d6",2025)),PresetInfo(14020120, Triple("\u5415", "\u8499",2004)),
            PresetInfo(14020130, Triple("\u5b59", "\u575a",2018)),PresetInfo(14020140, Triple("\u9c81", "\u8083",2024)),

            PresetInfo(14030010, Triple("\u5415", "\u5e03", 2016), 14030041, 1000, 2000),
            PresetInfo(14030020, Triple("\u8463", "\u5353", 2001), 0, 200, 600),
            PresetInfo(14030030, Triple("\u9ad8", "\u987a",2023)),
            PresetInfo(14030041, Triple("\u8c82", "\u8749", 4007), 14030010, 100, 200),
            PresetInfo(14030050, Triple("\u674e", "\u5112", 2021)),
            PresetInfo(14030060, Triple("\u8881", "\u7ecd", 2017), 0, 100, 200),
            PresetInfo(14030070, Triple("\u989c","\u826f",2042)), PresetInfo(14030080, Triple("\u6f58","\u51e4",2039)),
            PresetInfo(14030090, Triple("\u534e", "\u96c4",2037)), PresetInfo(14030100, Triple("\u6587","\u4e11",2043)),
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 4002), 0, 200, 600, Pair(14030010, 14030041)),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7",2038)), PresetInfo(14030130, Triple("\u5218", "\u8868",2036)),
            PresetInfo(14030140, Triple("\u534e", "\u4f57",2048)),

            PresetInfo(14040010, Pair("\u5218", "\u7109"), 0, 100, 100),
            PresetInfo(14040020, Pair("\u7559", "\u8d5e"), 0, 100, 100),
            PresetInfo(14040030, Pair("\u5173", "\u7d22"), 14040081, 200, 600),
            PresetInfo(14040041, Pair("\u8d75", "\u8944"), 0, 200, 600, Pair(14010040, 14010101)),
            PresetInfo(14040050, Pair("\u8bb8", "\u52ad"), 0, 100, 200),
            PresetInfo(14040061, Pair("\u738b", "\u8363"), 0, 100, 200),
            PresetInfo(14040071, Pair("\u82b1", "\u9b18"), 14040030, 100, 200, Pair(14010140, 14010151)),
            PresetInfo(14040081, Pair("\u9c8d", "\u4e09\u5a18"), 14040030, 100, 200),
            PresetInfo(14040090, Pair("\u738b", "\u53cc")), PresetInfo(14040100, Pair("\u9648", "\u7433")),
            PresetInfo(14040101, Pair("\u5468", "\u5983"), 0, 100, 100, Pair(14020060, 14020051))

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
            PresetInfo(16000020, Pair("\u6768","\u5fd7")),
            PresetInfo(16000030, Pair("\u5218","\u5510")),
            PresetInfo(16000040, Pair("\u516c\u5b59","\u80dc"),0, 100, 100),
            PresetInfo(16000051, Pair("\u6248","\u4e09\u5a18"),0, 200, 600),
            PresetInfo(16000060, Pair("\u6797","\u51b2"),0, 100, 100),
            PresetInfo(16000070, Pair("\u9c81","\u667a\u6df1")),
            PresetInfo(16000080, Pair("\u6b66","\u677e"),0, 100, 100),
            PresetInfo(16000090, Pair("\u962e","\u5c0f\u4e03")),
            PresetInfo(16000100, Pair("\u82b1","\u8363"),0, 100, 200),
            PresetInfo(16000110, Pair("\u79e6","\u660e")),
            PresetInfo(16000120, Pair("\u67f4","\u8fdb")),

            PresetInfo(16010010, Pair("\u8521","\u4eac"),0, 100, 100),
            PresetInfo(16010020, Pair("\u9ad8","\u4fc5"),0, 100, 100),
            PresetInfo(16010030, Pair("\u9ad8","\u8859\u5185")),
            PresetInfo(16010040, Pair("\u7ae5","\u8d2f")),
            PresetInfo(16010050, Pair("\u897f\u95e8","\u5e86"),16010061),
            PresetInfo(16010061, Pair("\u6f58","\u91d1\u83b2"),16010050, 100, 100),

            PresetInfo(16020010, Pair("\u6641","\u76d6"),0, 100, 200),
            PresetInfo(16020020, Pair("\u738b","\u4f26"))
    )

    private val SpecPersonFirstName7:MutableList<PresetInfo> = mutableListOf(

            PresetInfo(17000010, Triple("\u5929\u9a6c\u5ea7","\u661f\u77e2",4001),0, 100, 200),
            PresetInfo(17000020, Triple("\u51e4\u51f0\u5ea7","\u4e00\u8f89",4005),17030051, 100, 200),
            PresetInfo(17000030, Triple("\u5929\u9f99\u5ea7","\u7d2b\u9f99",4002)),
            PresetInfo(17000040, Triple("\u767d\u9e1f\u5ea7","\u51b0\u6cb3",4003)),
            PresetInfo(17000050, Triple("\u4ed9\u5973\u5ea7","\u77ac",4005)),
            PresetInfo(17000060, Triple("\u72ec\u89d2\u517d\u5ea7","\u90aa\u6b66",4006)),
            PresetInfo(17000070, Triple("\u5e7c\u72ee\u5ea7","\u86ee",4007)),
            PresetInfo(17000080, Triple("\u8c7a\u72fc\u5ea7","\u90a3\u667a",4008)),
            PresetInfo(17000090, Triple("\u5927\u718a\u5ea7","\u6a84",4009)),
            PresetInfo(17000100, Triple("\u6c34\u86c7\u5ea7","\u5e02",4010)),
            PresetInfo(17000111, Triple("\u53d8\u8272\u9f99\u5ea7","\u73cd\u59ae",2214)),
            PresetInfo(17000120, Triple("\u5361\u897f","\u6b27\u58eb",4073)),
            PresetInfo(17000130, Triple("\u8fb0\u5df1","\u5f97\u4e38",4074)),

            PresetInfo(17010010, Triple("\u53cc\u5b50\u5ea7","\u6492\u52a0",4012),0, 200, 600),
            PresetInfo(17010020, Triple("\u5904\u5973\u5ea7","\u6c99\u52a0",4015),0, 200, 600),
            PresetInfo(17010030, Triple("\u767d\u7f8a\u5ea7","\u7a46",4021)),
            PresetInfo(17010040, Triple("\u6c34\u74f6\u5ea7","\u5361\u5999",4018)),
            PresetInfo(17010050, Triple("\u5929\u874e\u5ea7","\u7c73\u7f57",4016)),
            PresetInfo(17010060, Triple("\u6469\u7faf\u5ea7","\u4fee\u7f57",4017)),
            PresetInfo(17010070, Triple("\u53cc\u9c7c\u5ea7","\u963f\u5e03\u7f57\u72c4",4019)),
            PresetInfo(17010080, Triple("\u72ee\u5b50\u5ea7","\u827e\u5965\u91cc\u4e9a",4014)),
            PresetInfo(17010090, Triple("\u5929\u67b0\u5ea7","\u7ae5\u864e",4022),0,100,100),
            PresetInfo(17010100, Triple("\u767d\u7f8a\u5ea7","\u53f2\u6602",4023),0,100,200),
            PresetInfo(17010110, Triple("\u91d1\u725b\u5ea7","\u963f\u9c81\u8fea\u5df4",4011)),
            PresetInfo(17010120, Triple("\u5de8\u87f9\u5ea7","\u8fea\u65af\u9a6c\u65af\u514b",4013)),
            PresetInfo(17010130, Triple("\u5c04\u624b\u5ea7","\u827e\u4fc4\u7f57\u65af",4020)),
            PresetInfo(17010140, Triple("\u53cc\u5b50\u5ea7","\u52a0\u9686",4024),0, 100, 100),

            PresetInfo(17020010, Triple("\u6d77\u7687","\u6ce2\u585e\u51ac",4038),0, 1000, 2000),
            PresetInfo(17020020, Triple("\u6d77\u9f99","\u52a0\u9686",4037),0, 100, 100),
            PresetInfo(17020030, Triple("\u6d77\u9b54\u5973","\u82cf\u5170\u7279",4032),0, 100, 100),
            PresetInfo(17020040, Triple("\u6d77\u9a6c","\u5df4\u5c14\u5b89",4031)),
            PresetInfo(17020050, Triple("\u516d\u5723\u517d","\u4f0a\u5965",4034)),
            PresetInfo(17020060, Triple("\u6d77\u7687\u5b50","\u514b\u4fee\u62c9",4033),0,100,100),
            PresetInfo(17020070, Triple("\u9b54\u9b3c\u9c7c","\u827e\u5c14\u624e\u514b",4036)),
            PresetInfo(17020080, Triple("\u6d77\u5e7b\u517d","\u5361\u8428",4035)),


            PresetInfo(17030010, Triple("\u51a5\u738b","\u54c8\u8fea\u65af",4039),0, 1000, 2000),
            PresetInfo(17030020, Triple("\u7761\u795e","\u4fee\u666e\u8bfa\u65af",4040),0, 500, 1000),
            PresetInfo(17030030, Triple("\u6b7b\u795e","\u5854\u7eb3\u6258\u65af",4041),0, 500, 1000),
            PresetInfo(17030040, Triple("\u5929\u731b\u661f","\u62c9\u8fbe\u66fc\u8fea\u65af",4042),0, 100, 100),
            PresetInfo(17030051, Pair("\u6f58","\u591a\u62c9"),17000020, 100, 200),
            PresetInfo(17030060, Triple("\u5929\u8d35\u661f","\u7c73\u8bfa\u65af",4043)),
            PresetInfo(17030070, Triple("\u5929\u96c4\u661f","\u827e\u4e9a\u54e5\u65af",4044)),
            PresetInfo(17030080, Triple("\u5929\u82f1\u661f","\u8def\u5c3c",4045)),
            PresetInfo(17030090, Triple("\u5929\u517d\u661f","\u6cd5\u62c9\u5965",4046)),
            PresetInfo(17030100, Triple("\u5de8\u87f9\u5ea7","\u8fea\u65af\u9a6c\u65af\u514b(\u51a5)",4026)),
            PresetInfo(17030110, Triple("\u53cc\u9c7c\u5ea7","\u963f\u5e03\u7f57\u72c4(\u51a5)",4027)),
            PresetInfo(17030120, Triple("\u6c34\u74f6\u5ea7","\u5361\u5999(\u51a5)",4030)),
            PresetInfo(17030130, Triple("\u6469\u7faf\u5ea7","\u4fee\u7f57(\u51a5)",4029)),
            PresetInfo(17030140, Triple("\u53cc\u5b50\u5ea7","\u6492\u52a0(\u51a5)",4028),0, 100, 200),
            PresetInfo(17030150, Triple("\u767d\u7f8a\u5ea7","\u53f2\u6602(\u51a5)",4025),0,100,100),
            PresetInfo(17030160, Triple("\u5929\u54ed\u661f","\u5df4\u8fde\u8fbe\u56e0",4047)),
            PresetInfo(17030170, Triple("\u5730\u5996\u661f","\u7f2a\u0020",4048)),

            PresetInfo(17040011, Triple("\u5929\u9e70\u5ea7","\u9b54\u94c3",2212)),
            PresetInfo(17040021, Triple("\u86c7\u592b\u5ea7","\u838e\u5c14\u5a1c",2213),0, 100, 200),
            PresetInfo(17040030, Triple("\u8725\u8734\u5ea7","\u7f8e\u65af\u72c4",4078)),
            PresetInfo(17040040, Triple("\u767d\u9cb8\u5ea7","\u6469\u897f\u65af",4081)),
            PresetInfo(17040050, Triple("\u730e\u72ac\u5ea7","\u4e9a\u72c4\u91cc\u5b89",4080)),
            PresetInfo(17040060, Triple("\u82f1\u4ed9\u5ea7","\u4e9a\u9c81\u54e5\u8def",4079)),
            PresetInfo(17040070, Triple("\u5929\u7434\u5ea7","\u5965\u8def\u83f2",4082),0,200,600),

            PresetInfo(17050010, Triple("\u5929\u9a6c\u5ea7","\u5929\u9a6c",4055)),
            PresetInfo(17050021, Triple("\u96c5\u5178\u5a1c","\u8428\u6c99",2208),0,200,600),
            PresetInfo(17050030, Triple("\u5929\u67b0\u5ea7","\u7ae5\u864e(LC)",4051)),
            PresetInfo(17050040, Triple("\u5de8\u87f9\u5ea7","\u9a6c\u5c3c\u6208\u7279",4052)),
            PresetInfo(17050050, Triple("\u767d\u7f8a\u5ea7","\u53f2\u6602(LC)",4057)),
            PresetInfo(17050060, Triple("\u5c04\u624b\u5ea7","\u5e0c\u7eea\u5f17\u65af",4058)),
            PresetInfo(17050070, Triple("\u6469\u7faf\u5ea7","\u827e\u5c14\u5e0c\u5fb7",4059)),
            PresetInfo(17050080, Triple("\u53cc\u9c7c\u5ea7","\u96c5\u67cf\u83f2\u5361",4060)),
            PresetInfo(17050090, Triple("\u796d\u575b\u5ea7","\u767d\u793c",4061)),
            PresetInfo(17050100, Triple("\u91d1\u725b\u5ea7","\u963f\u9c81\u8fea\u5df4(LC)",4062)),
            PresetInfo(17050110, Triple("\u5904\u5973\u5ea7","\u963f\u91ca\u5bc6\u8fbe",4063)),
            PresetInfo(17050120, Triple("\u5929\u874e\u5ea7","\u5361\u8def\u8fea\u4e9a",4065)),
            PresetInfo(17050130, Triple("\u6c34\u74f6\u5ea7","\u7b1b\u6377\u5c14",4066)),
            PresetInfo(17050140, Triple("\u53cc\u5b50\u5ea7","\u963f\u65af\u666e\u6d1b\u65af",4068),0,100,100),
            PresetInfo(17050150, Triple("\u72ee\u5b50\u5ea7","\u96f7\u53e4\u9c81\u65af",4070),0,100,200),

            PresetInfo(17060010, Triple("\u54c8\u8fea\u65af","\u4e9a\u4f26",4049)),
            PresetInfo(17060021, Triple("\u6f58","\u591a\u62c9(LC)",2207),17060080,100,200),
            PresetInfo(17060030, Triple("\u51a5\u738b","\u54c8\u8fea\u65af(LC)",4050),0, 200, 600),
            PresetInfo(17060040, Triple("\u7761\u795e","\u4fee\u666e\u8bfa\u65af(LC)",4054),0,100,200),
            PresetInfo(17060050, Triple("\u6b7b\u795e","\u5854\u7eb3\u6258\u65af(LC)",4053),0,100,200),
            PresetInfo(17060060, Triple("\u5929\u66b4\u661f","\u8f89\u706b",4064)),
            PresetInfo(17060070, Triple("\u68a6\u795e","\u5965\u6d85\u4f0a\u6d1b\u65af",4067),200, 600),
            PresetInfo(17060080, Triple("\u5929\u731b\u661f","\u62c9\u8fbe\u66fc\u8fea\u65af(\u795e\u9f99)",4069),17060021, 100, 200),
            PresetInfo(17060090, Triple("\u51a5\u738b","\u54c8\u8fea\u65af(LC/\u771f)",4071),0, 500, 1000),
            PresetInfo(17060101, Triple("\u6d77\u7687","\u6ce2\u585e\u51ac(LC)",2204),0, 500, 1000)


    )

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