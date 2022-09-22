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

    val SpecPersonFirstName3:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(Pair("\u7389", "\u5e1d"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u83e9","\u63d0"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u6768","\u622c"), NameUtil.Gender.Male, 0),
            PresetInfo(Pair("\u54ea", "\u5412"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u592a\u4e0a","\u8001\u541b"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u5ae6","\u5a25"), NameUtil.Gender.Female, 0),
            PresetInfo(Pair("\u592a\u767d","\u91d1\u661f"), NameUtil.Gender.Male, 0),

            PresetInfo(Pair("\u9080","\u6708"), NameUtil.Gender.Female, 1),PresetInfo(Pair("\u601c","\u661f"), NameUtil.Gender.Female, 1),PresetInfo(Pair("\u82cf","\u6a31"), NameUtil.Gender.Female, 1),
            PresetInfo(Pair("\u674e","\u7ea2\u8896"), NameUtil.Gender.Female, 1),PresetInfo(Pair("\u695a","\u7559\u9999"), NameUtil.Gender.Male, 1),PresetInfo(Pair("\u98ce","\u56db\u5a18"), NameUtil.Gender.Female, 1),
            PresetInfo(Pair("\u674e","\u5bfb\u6b22"), NameUtil.Gender.Male, 1),

            PresetInfo(Pair("\u9ec4","\u84c9"), NameUtil.Gender.Female, 2),PresetInfo(Pair("\u8d75","\u654f"), NameUtil.Gender.Female, 2),PresetInfo(Pair("\u5468","\u82b7\u82e5"), NameUtil.Gender.Female, 2),
            PresetInfo(Pair("\u8427","\u5cf0"), NameUtil.Gender.Male, 2),PresetInfo(Pair("\u5c0f","\u662d"), NameUtil.Gender.Female, 2),PresetInfo(Pair("\u6728","\u5a49\u6e05"), NameUtil.Gender.Female, 2),
            PresetInfo(Pair("\u97e6","\u5c0f\u5b9d"), NameUtil.Gender.Male, 2),

            PresetInfo(Pair("\u4f0f", "\u7fb2"), NameUtil.Gender.Male, 3), PresetInfo(Pair("\u5973","\u5a32"), NameUtil.Gender.Female, 3), PresetInfo(Pair("\u795e","\u519c"), NameUtil.Gender.Male, 3),
            PresetInfo(Pair("\u86a9", "\u5c24"), NameUtil.Gender.Male, 3), PresetInfo(Pair("\u989b","\u987c"), NameUtil.Gender.Male, 3), PresetInfo(Pair("\u5e1d","\u55be"), NameUtil.Gender.Male, 3),
            PresetInfo(Pair("\u9ec4","\u5e1d"), NameUtil.Gender.Male, 3)
    )

    val SpecPersonFirstName4:MutableList<PresetInfo> = mutableListOf(
            PresetInfo(Pair("\u66f9", "\u64cd"), NameUtil.Gender.Male, 0, 500, 1000),
            PresetInfo(Pair("\u53f8\u9a6c", "\u61ff"), NameUtil.Gender.Male, 0, 200, 600),
            PresetInfo(Pair("\u90ed", "\u5609"), NameUtil.Gender.Male, 0, 200, 600),
            PresetInfo(Pair("\u7504", "\u5b93"), NameUtil.Gender.Female, 0, 100, 400),
            PresetInfo(Pair("\u738b", "\u5f02"), NameUtil.Gender.Female, 0, 100, 400),
            PresetInfo(Pair("\u590f\u4faf","\u60c7"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u590f\u4faf","\u6e0a"), NameUtil.Gender.Male, 0),
            PresetInfo(Pair("\u8340", "\u5f67"), NameUtil.Gender.Male, 0), PresetInfo(Pair("\u8bb8","\u891a"), NameUtil.Gender.Male, 0),


            PresetInfo(Pair("\u5218", "\u5907"), NameUtil.Gender.Male, 1, 200, 600),
            PresetInfo(Pair("\u5173", "\u7fbd"), NameUtil.Gender.Male, 1, 200, 600),
            PresetInfo(Pair("\u5f20", "\u98de"), NameUtil.Gender.Male, 1, 200, 600),
            PresetInfo(Pair("\u8d75", "\u4e91"), NameUtil.Gender.Male, 1, 100, 400),
            PresetInfo(Pair("\u8bf8\u845b", "\u4eae"), NameUtil.Gender.Male, 1, 500, 1000),
            PresetInfo(Pair("\u5e9e","\u7edf"), NameUtil.Gender.Male, 1), PresetInfo(Pair("\u9a6c","\u8d85"), NameUtil.Gender.Male, 1),
            PresetInfo(Pair("\u9ec4", "\u5fe0"), NameUtil.Gender.Male, 1), PresetInfo(Pair("\u9a6c","\u4e91\u7984"), NameUtil.Gender.Female, 1),

            PresetInfo(Pair("\u5b59", "\u6743"), NameUtil.Gender.Male, 2, 200, 600),
            PresetInfo(Pair("\u5b59", "\u7b56"), NameUtil.Gender.Male, 2, 200, 600),
            PresetInfo(Pair("\u9646", "\u900a"), NameUtil.Gender.Male, 2, 200, 600),
            PresetInfo(Pair("\u9ec4", "\u76d6"), NameUtil.Gender.Male, 2, 100, 200),
            PresetInfo(Pair("\u5468", "\u745c"), NameUtil.Gender.Male, 2, 500, 1000),
            PresetInfo(Pair("\u6f58","\u748b"), NameUtil.Gender.Male, 2), PresetInfo(Pair("\u7518","\u5b81"), NameUtil.Gender.Male, 2),
            PresetInfo(Pair("\u592a\u53f2", "\u6148"), NameUtil.Gender.Male, 2), PresetInfo(Pair("\u5b59","\u5c1a\u9999"), NameUtil.Gender.Female, 2),

            PresetInfo(Pair("\u5415", "\u5e03"), NameUtil.Gender.Male, 3, 1000, 2000),
            PresetInfo(Pair("\u8881", "\u7ecd"), NameUtil.Gender.Male, 3, 200, 600),
            PresetInfo(Pair("\u8d3e", "\u8be9"), NameUtil.Gender.Male, 3, 200, 600),
            PresetInfo(Pair("\u5f20", "\u8fbd"), NameUtil.Gender.Male, 3, 100, 300),
            PresetInfo(Pair("\u8463", "\u5353"), NameUtil.Gender.Male, 3, 100, 200),
            PresetInfo(Pair("\u989c","\u826f"), NameUtil.Gender.Male, 3), PresetInfo(Pair("\u674e","\u5095"), NameUtil.Gender.Male, 3),
            PresetInfo(Pair("\u534e", "\u96c4"), NameUtil.Gender.Male, 3), PresetInfo(Pair("\u6587","\u4e11"), NameUtil.Gender.Male, 3)


    )


    val SpecPersonFixedName:MutableList<Triple<Pair<String, String>, NameUtil.Gender, PersonFixedInfoMix>> = mutableListOf(
            Triple(Pair("\u7384", "\u5973"), NameUtil.Gender.Female, PersonFixedInfoMix("1000007", mutableListOf("4000106", "4000206", "4000304", "4000404", "4000504")))
            ,Triple(Pair("\u5b5f", "\u5a46"), NameUtil.Gender.Female, PersonFixedInfoMix("1000006", mutableListOf("4000104", "4000205", "4000305", "4000402", "4000506")))
            ,Triple(Pair("\u6bdb", "\u6b23"), NameUtil.Gender.Male, PersonFixedInfoMix("1000008", mutableListOf("4000109", "4000209", "4000305", "4000407", "4000506")))
    )

    data class SpecPersonInfo(var name:Pair<String, String?>, var gender: NameUtil.Gender?, var allianceIndex: Int, var TianFuWeight:Int, var LingGenWeight:Int)
    data class PersonFixedInfoMix(var lingGenId:String?, var tianFuIds:MutableList<String>?, var tianFuWeight: Int = 1, var lingGenWeight:Int = 1)
    // type 1 人物信息 assert(person != null), 2 流程信息 assert(battleId != "")
    // person : add lingGen color
    data class HistoryInfo(var type:Int = 0, var content:String, var person: Person?, var battleId:String?)
    data class PresetInfo(var name: Pair<String, String>, var gender: NameUtil.Gender, var index: Int, var tianfuWeight:Int = 50, var linggenWeight:Int = 50)
}