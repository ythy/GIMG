package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    val PostColors = arrayOf("#E2D223", "#BE0012", "#0272E4", "#12A703", "#EF7362")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")

    const val SP_JIE_TURN = 81
    val EVENT_WEIGHT = listOf("1200-50","7200-40","8400-40","9600-100")
    const val SP_PUNISH_BOSS_MILLION = 200

    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983", "\u4ec6", "\u8bcf")
    val SpecPersonFirstNameWeight:Pair<Int, Int> = Pair(50, 20)// tianfu.linggen

    //13 00 001 0
    private val SpecPersonFirstName3:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(13000010, Pair("\u6b65","\u60ca\u4e91"),0, 100, 200),
            PresetInfo(13000020, Pair("\u8042","\u98ce"),13000031, 200, 600),
            PresetInfo(13000031, Pair("\u7b2c\u4e8c","\u68a6"),13000020),
            PresetInfo(13000040, Pair("\u96c4","\u9738"),0, 200, 600),
            PresetInfo(13000050, Pair("\u79e6","\u971c")),

            PresetInfo(13010031, Pair("\u82cf","\u6a31"),0, 100, 100),
            PresetInfo(13010041, Pair("\u674e","\u7ea2\u8896"),0, 100, 100),
            PresetInfo(13010070, Pair("\u674e","\u5bfb\u6b22"),0, 200, 600),

            PresetInfo(13020011, Pair("\u9ec4","\u84c9"),0, 200, 600),
            PresetInfo(13020021, Pair("\u8d75","\u654f"),0, 200, 600),
            PresetInfo(13020031, Pair("\u5468","\u82b7\u82e5"),0, 200, 600),
            PresetInfo(13020051, Pair("\u5c0f","\u662d"),0, 100, 200),
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
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 2014)), PresetInfo(14010101, Triple("\u9a6c","\u4e91\u7984", 4008)),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 2032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 4001), 0,200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 2022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 2031)),
            PresetInfo(14010151, Triple("\u795d", "\u878d", 4011)),

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
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 4002), 0, 200, 600),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7",2038)), PresetInfo(14030130, Triple("\u5218", "\u8868",2036)),
            PresetInfo(14030140, Triple("\u534e", "\u4f57",2048))
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

            PresetInfo(15010010, Pair("\u674e","\u5143\u9738"),0, 500, 1000),
            PresetInfo(15010020, Pair("\u9879","\u7fbd"),15000081, 500, 1000),
            PresetInfo(15010030, Pair("\u8346","\u8f72"),0, 100, 100),

            PresetInfo(15020010, Pair("\u5b34","\u653f"),0, 500, 1000),
            PresetInfo(15020020, Pair("\u674e","\u4e16\u6c11"),0, 100, 200),
            PresetInfo(15020030, Pair("\u8d75","\u4f76"),0, 100, 100),
            PresetInfo(15020040, Pair("\u674e","\u9686\u57fa"),15000091)
    )

    private val SpecPersonFirstName6:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(16000010, Pair("\u5b8b","\u6c5f"),0, 100, 200),
            PresetInfo(16000020, Pair("\u6768","\u5fd7")),
            PresetInfo(16000030, Pair("\u5218","\u5510")),
            PresetInfo(16000040, Pair("\u516c\u5b59","\u80dc"),0, 100, 100),
            PresetInfo(16000051, Pair("\u6248","\u4e09\u5a18"),0, 200, 600),
            PresetInfo(16000060, Pair("\u6797","\u51b2"),0, 100, 100),
            PresetInfo(16000070, Pair("\u9c81","\u667a\u6df1")),

            PresetInfo(16010010, Pair("\u8521","\u4eac"),0, 100, 100),
            PresetInfo(16010020, Pair("\u9ad8","\u7403"),0, 100, 100),

            PresetInfo(16020010, Pair("\u6641","\u76d6"),0, 100, 200),
            PresetInfo(16020020, Pair("\u738b","\u4f26"))
    )

    private val SpecPersonFirstName7:MutableList<PresetInfo> = mutableListOf(

            PresetInfo(17000010, Pair("\u5929\u9a6c\u5ea7","\u661f\u77e2"),0, 100, 200),
            PresetInfo(17000020, Pair("\u51e4\u51f0\u5ea7","\u4e00\u8f89"),17030051, 100, 200),
            PresetInfo(17000030, Pair("\u5929\u9f99\u5ea7","\u7d2b\u9f99")),
            PresetInfo(17000040, Pair("\u767d\u9e1f\u5ea7","\u51b0\u6cb3")),
            PresetInfo(17000050, Pair("\u4ed9\u5973\u5ea7","\u77ac")),
            PresetInfo(17000060, Pair("\u72ec\u89d2\u517d\u5ea7","\u90aa\u6b66")),

            PresetInfo(17010010, Pair("\u53cc\u5b50\u5ea7","\u6492\u52a0"),0, 200, 600),
            PresetInfo(17010020, Pair("\u5904\u5973\u5ea7","\u6c99\u52a0"),0, 200, 600),
            PresetInfo(17010030, Pair("\u767d\u7f8a\u5ea7","\u7a46")),
            PresetInfo(17010040, Pair("\u6c34\u74f6\u5ea7","\u5361\u5999")),
            PresetInfo(17010050, Pair("\u5929\u874e\u5ea7","\u7c73\u7f57")),
            PresetInfo(17010060, Pair("\u6469\u7faf\u5ea7","\u4fee\u7f57")),
            PresetInfo(17010070, Pair("\u53cc\u9c7c\u5ea7","\u963f\u5e03\u7f57\u72c4")),
            PresetInfo(17010080, Pair("\u72ee\u5b50\u5ea7","\u827e\u5965\u91cc\u4e9a")),
            PresetInfo(17010090, Pair("\u5929\u67b0\u5ea7","\u7ae5\u864e"),0,100,100),
            PresetInfo(17010100, Pair("\u767d\u7f8a\u5ea7","\u53f2\u6602"),0,100,200),
            PresetInfo(17010110, Pair("\u91d1\u725b\u5ea7","\u963f\u9c81\u8fea\u5df4")),
            PresetInfo(17010120, Pair("\u5de8\u87f9\u5ea7","\u8fea\u65af\u9a6c\u65af\u514b")),
            PresetInfo(17010130, Pair("\u5c04\u624b\u5ea7","\u827e\u4fc4\u7f57\u65af")),
            PresetInfo(17010140, Pair("\u53cc\u5b50\u5ea7","\u52a0\u9686"),0, 100, 100),

            PresetInfo(17020010, Pair("\u6d77\u7687","\u6ce2\u585e\u51ac"),0, 1000, 2000),
            PresetInfo(17020020, Pair("\u6d77\u9f99","\u52a0\u9686"),0, 100, 100),
            PresetInfo(17020030, Pair("\u6d77\u9b54\u5973","\u82cf\u5170\u7279"),0, 100, 100),
            PresetInfo(17020040, Pair("\u6d77\u9a6c","\u5df4\u5c14\u5b89")),
            PresetInfo(17020050, Pair("\u516d\u5723\u517d","\u4f0a\u5965")),
            PresetInfo(17020060, Pair("\u6d77\u7687\u5b50","\u514b\u4fee\u62c9"),0,100,100),
            PresetInfo(17020070, Pair("\u9b54\u9b3c\u9c7c","\u827e\u5c14\u624e\u514b")),
            PresetInfo(17020080, Pair("\u6d77\u5e7b\u517d","\u5361\u8428")),


            PresetInfo(17030010, Pair("\u51a5\u738b","\u54c8\u8fea\u65af"),0, 1000, 2000),
            PresetInfo(17030020, Pair("\u7761\u795e","\u4fee\u666e\u8bfa\u65af"),0, 500, 1000),
            PresetInfo(17030030, Pair("\u6b7b\u795e","\u5854\u7eb3\u6258\u65af"),0, 500, 1000),
            PresetInfo(17030040, Pair("\u5929\u731b\u661f","\u62c9\u8fbe\u66fc\u8fea\u65af"),0, 100, 100),
            PresetInfo(17030051, Pair("\u6f58","\u591a\u62c9"),17000020, 100, 200),
            PresetInfo(17030060, Pair("\u5929\u8d35\u661f","\u7c73\u8bfa\u65af")),
            PresetInfo(17030070, Pair("\u5929\u96c4\u661f","\u827e\u4e9a\u54e5\u65af")),
            PresetInfo(17030080, Pair("\u5929\u82f1\u661f","\u8def\u5c3c")),
            PresetInfo(17030090, Pair("\u5929\u517d\u661f","\u6cd5\u62c9\u5965")),
            PresetInfo(17030100, Pair("\u51a5\u5de8\u87f9\u5ea7","\u8fea\u65af\u9a6c\u65af\u514b")),
            PresetInfo(17030110, Pair("\u51a5\u53cc\u9c7c\u5ea7","\u963f\u5e03\u7f57\u72c4")),
            PresetInfo(17030120, Pair("\u51a5\u6c34\u74f6\u5ea7","\u5361\u5999")),
            PresetInfo(17030130, Pair("\u51a5\u6469\u7faf\u5ea7","\u4fee\u7f57")),
            PresetInfo(17030140, Pair("\u51a5\u53cc\u5b50\u5ea7","\u6492\u52a0"),0, 100, 200),
            PresetInfo(17030150, Pair("\u51a5\u767d\u7f8a\u5ea7","\u53f2\u6602"),0,100,100)
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

        constructor(identity:Int, name: Triple<String, String, Int>, partner:Int = 0, tianfuWeight:Int = 50, linggenWeight:Int = 50)
                :this(identity, Pair(name.first, name.second), partner, tianfuWeight, linggenWeight){
            profile = name.third
        }
    }

}