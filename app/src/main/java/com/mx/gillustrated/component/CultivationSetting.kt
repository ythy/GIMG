package com.mx.gillustrated.component

import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person

object CultivationSetting {

    val CommonColors = arrayOf("#EAEFE8", "#417B29", "#367CC4", "#7435C1", "#D22E59", "#FB23B7", "#CDA812", "#F2E40A", "#04B4BA", "#C18135", "#A5529E")
    val EnemyNames = arrayOf("\u83dc\u83dc", "\u8fdc\u53e4", "\u68ee\u7f57", "\u4e07\u8c61", "\u9b51\u9b45", "\u9b4d\u9b49")

    const val SP_JIE_TURN = 81
    val SP_EVENT_WEIGHT = listOf("1000-10","1200-40","7200-40","8400-40","9600-80")
    val SP_PUNISH_MILLION = listOf(100,500)

    val SpecPersonFirstName2:MutableList<String> = mutableListOf("主", "\u4f8d", "廿一", "廿二", "廿三")
    val SpecPersonFirstName:MutableList<String> = mutableListOf("主", "\u4f8d", "儿", "\u5983")

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

            PresetInfo(13030010, Pair("\u4f0f", "\u7fb2")), PresetInfo(13030021, Pair("\u5973","\u5a32")),
            PresetInfo(13030030, Pair("\u795e","\u519c")), PresetInfo(13030040, Pair("\u86a9", "\u5c24")),
            PresetInfo(13030050, Pair("\u989b","\u987c")), PresetInfo(13030060, Pair("\u5e1d","\u55be")),
            PresetInfo(13030070, Pair("\u9ec4","\u5e1d"))
    )

    val SpecPersonFirstName4:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(14000010, Pair("\u66f9", "\u64cd"),0,500, 1000),
            PresetInfo(14000020, Pair("\u53f8\u9a6c", "\u61ff"), 14000061,200, 600),
            PresetInfo(14000030, Pair("\u90ed", "\u5609"), 0,200, 600),
            PresetInfo(14000041, Pair("\u7504", "\u5b93"), 0, 100, 400),
            PresetInfo(14000051, Pair("\u738b", "\u5f02"), 0,100, 400),
            PresetInfo(14000061, Pair("\u5f20", "\u6625\u534e"), 14000020,100, 400),
            PresetInfo(14000070, Pair("\u590f\u4faf","\u60c7")), PresetInfo(14000080, Pair("\u590f\u4faf","\u6e0a")),
            PresetInfo(14000090, Pair("\u8340", "\u5f67")), PresetInfo(14000100, Pair("\u8bb8","\u891a")),

            PresetInfo(14010010, Pair("\u5218", "\u5907"), 14020101,200, 600),
            PresetInfo(14010020, Pair("\u5173", "\u7fbd"), 0,200, 600),
            PresetInfo(14010030, Pair("\u5f20", "\u98de"),0,200, 600),
            PresetInfo(14010040, Pair("\u8d75", "\u4e91"), 14010101,100, 400),
            PresetInfo(14010050, Pair("\u8bf8\u845b", "\u4eae"), 14010061,500, 1000),
            PresetInfo(14010061, Pair("\u9ec4", "\u6708\u82f1"), 14010050,100, 400),
            PresetInfo(14010070, Pair("\u5e9e","\u7edf")), PresetInfo(14010080, Pair("\u9a6c","\u8d85")),
            PresetInfo(14010090, Pair("\u9ec4", "\u5fe0")), PresetInfo(14010101, Pair("\u9a6c","\u4e91\u7984"), 14010040),
            PresetInfo(14010110, Pair("\u9b4f", "\u5ef6")),

            PresetInfo(14020010, Pair("\u5b59", "\u6743"), 0,200, 600),
            PresetInfo(14020020, Pair("\u5b59", "\u7b56"),14020041,200, 600),
            PresetInfo(14020030, Pair("\u9646", "\u900a"), 0,100, 200),
            PresetInfo(14020041, Pair("\u5927", "\u4e54"), 14020020,100, 200),
            PresetInfo(14020051, Pair("\u5c0f", "\u4e54"), 14020060,100, 200),
            PresetInfo(14020060, Pair("\u5468", "\u745c"), 14020051,500, 1000),
            PresetInfo(14020070, Pair("\u6f58","\u748b")), PresetInfo(14020080, Pair("\u7518","\u5b81")),
            PresetInfo(14020090, Pair("\u592a\u53f2", "\u6148")), PresetInfo(14020101, Pair("\u5b59","\u5c1a\u9999"), 14010010),
            PresetInfo(14020110, Pair("\u9ec4", "\u76d6")),

            PresetInfo(14030010, Pair("\u5415", "\u5e03"), 14030011, 1000, 2000),
            PresetInfo(14030010, Pair("\u8463", "\u5353"), 0, 200, 600),
            PresetInfo(14030010, Pair("\u8d3e", "\u8be9"), 0, 100, 400),
            PresetInfo(14030011, Pair("\u8c82", "\u8749"), 14030010, 100, 400),
            PresetInfo(14030010, Pair("\u5f20", "\u8fbd"), 0, 100, 200),
            PresetInfo(14030010, Pair("\u8881", "\u7ecd"), 0, 100, 200),
            PresetInfo(14030010, Pair("\u989c","\u826f")), PresetInfo(14030010, Pair("\u674e","\u5095")),
            PresetInfo(14030010, Pair("\u534e", "\u96c4")), PresetInfo(14030010, Pair("\u6587","\u4e11"))
    )


    val SpecPersonFixedName:MutableList<Triple<Pair<String, String>, NameUtil.Gender, PersonFixedInfoMix>> = mutableListOf(
            Triple(Pair("\u7384", "\u5973"), NameUtil.Gender.Female, PersonFixedInfoMix("1000007", mutableListOf("4000106", "4000206", "4000304", "4000404", "4000504")))
            ,Triple(Pair("\u5b5f", "\u5a46"), NameUtil.Gender.Female, PersonFixedInfoMix("1000006", mutableListOf("4000104", "4000205", "4000305", "4000402", "4000506")))
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
    data class PresetInfo(var identity:Int, var name: Pair<String, String>, var partner:Int = 0, var tianfuWeight:Int = 50, var linggenWeight:Int = 50)
}