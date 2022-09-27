package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    val PostColors = arrayOf("#E2D223", "#B72962", "#2972B7", "#29B779")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")

    const val SP_JIE_TURN = 81
    val SP_EVENT_WEIGHT = listOf("1000-10","1200-40","7200-40","8400-40","9600-80")
    val SP_PUNISH_MILLION = listOf(100,500)

    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983")
    val SpecPersonFirstNameWeight:Pair<Int, Int> = Pair(50, 20)// tianfu.linggen

    //13 00 001 0
    val SpecPersonFirstName3:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(13000010, Pair("\u7389", "\u5e1d")), PresetInfo(13000020, Pair("\u83e9","\u63d0")),
            PresetInfo(13000030, Pair("\u6768","\u622c")), PresetInfo(13000040, Pair("\u54ea", "\u5412")),
            PresetInfo(13000050, Pair("\u592a\u4e0a","\u8001\u541b")), PresetInfo(13000061, Pair("\u5ae6","\u5a25")),
            PresetInfo(13000070, Pair("\u592a\u767d","\u91d1\u661f")),

            PresetInfo(13010011, Pair("\u9080","\u6708")),PresetInfo(13010021, Pair("\u601c","\u661f")),
            PresetInfo(13010031, Pair("\u82cf","\u6a31")), PresetInfo(13010041, Pair("\u674e","\u7ea2\u8896"),13010050),
            PresetInfo(13010050, Pair("\u695a","\u7559\u9999"),13010041),PresetInfo(13010061, Pair("\u98ce","\u56db\u5a18")),
            PresetInfo(13010070, Pair("\u674e","\u5bfb\u6b22")),

            PresetInfo(13020011, Pair("\u9ec4","\u84c9")),PresetInfo(13020021, Pair("\u8d75","\u654f")),
            PresetInfo(13020031, Pair("\u5468","\u82b7\u82e5")), PresetInfo(13020040, Pair("\u8427","\u5cf0")),
            PresetInfo(13020051, Pair("\u5c0f","\u662d")),PresetInfo(13020061, Pair("\u6728","\u5a49\u6e05")),
            PresetInfo(13020070, Pair("\u97e6","\u5c0f\u5b9d")),

            PresetInfo(13030010, Pair("\u4f0f", "\u7fb2"), 0, 100, 100), PresetInfo(13030021, Pair("\u5973","\u5a32"),0, 100, 100),
            PresetInfo(13030030, Pair("\u795e","\u519c"),0, 100, 100), PresetInfo(13030040, Pair("\u86a9", "\u5c24"),0, 100, 100),
            PresetInfo(13030050, Pair("\u989b","\u987c"),0, 100, 100), PresetInfo(13030060, Pair("\u5e1d","\u55be"),0, 100, 100),
            PresetInfo(13030070, Pair("\u9ec4","\u5e1d"),0, 100, 100)
    )

    val SpecPersonFirstName4:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Triple("\u66f9", "\u64cd", 2047),0,500, 1000),
            PresetInfo(14000020, Triple("\u53f8\u9a6c", "\u61ff", 2011), 0,200, 600),
            PresetInfo(14000030, Triple("\u90ed", "\u5609", 2019), 0,200, 600),
            PresetInfo(14000041, Triple("\u7504", "\u5b93", 4009), 0, 100, 400),
            PresetInfo(14000050, Triple("\u5f20", "\u8fbd", 2034), 0, 100, 200),
            PresetInfo(14000061, Triple("\u8521", "\u7430", 4010), 0,100, 400),
            PresetInfo(14000070, Triple("\u590f\u4faf","\u60c7", 2008)), PresetInfo(14000080, Triple("\u590f\u4faf","\u6e0a", 2027)),
            PresetInfo(14000090, Triple("\u8340", "\u5f67", 2010)), PresetInfo(14000100, Triple("\u5178","\u97e6", 2007)),
            PresetInfo(14000110, Triple("\u5f90","\u6643", 2028)), PresetInfo(14000120, Triple("\u5f20","\u90c3", 2029)),
            PresetInfo(14000130, Triple("\u4e8e","\u7981", 2030)), PresetInfo(14000140, Triple("\u4e50","\u8fdb", 2041)),
            PresetInfo(14000150, Triple("\u9093","\u827e", 2026)), PresetInfo(14000160, Triple("\u8d3e", "\u8be9", 2009), 0, 100, 400),

            PresetInfo(14010010, Triple("\u5218", "\u5907", 2045), 14020101,200, 600),
            PresetInfo(14010020, Triple("\u5173", "\u7fbd", 2020), 0,200, 600),
            PresetInfo(14010030, Triple("\u5f20", "\u98de", 2015),0,200, 600),
            PresetInfo(14010040, Triple("\u8d75", "\u4e91", 2044), 14010101,100, 400),
            PresetInfo(14010050, Triple("\u8bf8\u845b", "\u4eae", 2013), 14010061,500, 1000),
            PresetInfo(14010061, Triple("\u9ec4", "\u6708\u82f1", 4006), 14010050,100, 400),
            PresetInfo(14010070, Triple("\u5e9e","\u7edf", 2012)), PresetInfo(14010080, Triple("\u9a6c","\u8d85", 2035)),
            PresetInfo(14010090, Triple("\u9ec4", "\u5fe0", 2014)), PresetInfo(14010101, Triple("\u9a6c","\u4e91\u7984", 4008), 14010040, 100, 400),
            PresetInfo(14010110, Triple("\u9b4f", "\u5ef6", 2032)), PresetInfo(14010121, Triple("\u5173", "\u94f6\u5c4f", 4001), 0,200, 600),
            PresetInfo(14010130, Triple("\u738b", "\u5e73", 2022)), PresetInfo(14010140, Triple("\u5b5f", "\u83b7", 2031)),

            PresetInfo(14020010, Triple("\u5b59", "\u6743", 2046), 0,200, 600),
            PresetInfo(14020020, Triple("\u5b59", "\u7b56", 2003),14020041,200, 600),
            PresetInfo(14020030, Triple("\u9646", "\u900a", 2005), 0,100, 200),
            PresetInfo(14020041, Triple("\u5927", "\u4e54",4005), 14020020,100, 400),
            PresetInfo(14020051, Triple("\u5c0f", "\u4e54",4004), 14020060,100, 400),
            PresetInfo(14020060, Triple("\u5468", "\u745c", 2006), 14020051,500, 1000),
            PresetInfo(14020070, Triple("\u5468","\u6cf0",2040)), PresetInfo(14020080, Triple("\u7518","\u5b81", 2033)),
            PresetInfo(14020090, Triple("\u592a\u53f2", "\u6148", 2002)), PresetInfo(14020101, Triple("\u5b59","\u5c1a\u9999", 4003), 14010010, 100, 400),
            PresetInfo(14020110, Triple("\u9ec4", "\u76d6",2025)),PresetInfo(14020120, Triple("\u5415", "\u8499",2004)),
            PresetInfo(14020130, Triple("\u5b59", "\u575a",2018)),PresetInfo(14020140, Triple("\u9c81", "\u8083",2024)),

            PresetInfo(14030010, Triple("\u5415", "\u5e03", 2016), 14030041, 1000, 2000),
            PresetInfo(14030020, Triple("\u8463", "\u5353", 2001), 0, 200, 600),
            PresetInfo(14030030, Triple("\u9ad8", "\u987a",2023)),
            PresetInfo(14030041, Triple("\u8c82", "\u8749", 4007), 14030010, 100, 400),
            PresetInfo(14030050, Triple("\u674e", "\u5112", 2021), 0, 100, 200),
            PresetInfo(14030060, Triple("\u8881", "\u7ecd", 2017), 0, 100, 200),
            PresetInfo(14030070, Triple("\u989c","\u826f",2042)), PresetInfo(14030080, Triple("\u6f58","\u51e4",2039)),
            PresetInfo(14030090, Triple("\u534e", "\u96c4",2037)), PresetInfo(14030100, Triple("\u6587","\u4e11",2043)),
            PresetInfo(14030111, Triple("\u5415", "\u73b2\u7eee", 4002), 0, 200, 600),
            PresetInfo(14030120, Triple("\u5e9e", "\u5fb7",2038)), PresetInfo(14030130, Triple("\u5218", "\u8868",2036))
    )

    val SpecPersonFixedName:MutableList<Triple<Pair<String, String>, NameUtil.Gender, PersonFixedInfoMix>> = mutableListOf(
            Triple(Pair("\u7384", "\u5973"), NameUtil.Gender.Female, PersonFixedInfoMix("1000007", mutableListOf("4000106", "4000206", "4000304", "4000404", "4000504")))
            ,Triple(Pair("\u6bdb", "\u6b23"), NameUtil.Gender.Male, PersonFixedInfoMix("1000008", mutableListOf("4000109", "4000209", "4000305", "4000407", "4000506")))
    )

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