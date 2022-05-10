package com.mx.gillustrated.util

import com.mx.gillustrated.common.name.FirstNameCorpus1
import com.mx.gillustrated.common.name.FirstNameCorpus2
import com.mx.gillustrated.common.name.FirstNameCorpus3
import com.mx.gillustrated.common.name.FirstNameCorpus4
import com.mx.gillustrated.common.name.FirstNameCorpus5
import com.mx.gillustrated.common.name.FirstNameCorpus6
import com.mx.gillustrated.common.name.FirstNameCorpus7
import com.mx.gillustrated.common.name.FirstNameCorpus8
import com.mx.gillustrated.common.name.FirstNameCorpus9
import com.mx.gillustrated.common.name.FirstNameCorpus10
import com.mx.gillustrated.common.name.NameCorpus
import java.util.*


object NameUtil {

    enum class Gender constructor(val props: String) {
        Default("未知"),
        Male("男"),
        Female("女"),
    }

    fun getChineseName(lastName:String?, gender:Gender = Gender.Default):String{
        val random = Random().nextInt(20)
        return when {
            random < 9 -> randomTwoName(lastName, gender)
            random < 17 -> randomThreeName(lastName, gender)
            random < 19 -> randomThreeNames(lastName, gender)
            else -> randomFourName(lastName, gender)
        }
    }

    private fun getFirstName2(gender:Gender):String{
        var array:List<String> = listOf()
        when (Random().nextInt(10)) {
            0 -> array = genderFilter(FirstNameCorpus1.FirstName2, gender)
            1 -> array = genderFilter(FirstNameCorpus2.FirstName2, gender)
            2 -> array = genderFilter(FirstNameCorpus3.FirstName2, gender)
            3 -> array = genderFilter(FirstNameCorpus4.FirstName2, gender)
            4 -> array = genderFilter(FirstNameCorpus5.FirstName2, gender)
            5 -> array = genderFilter(FirstNameCorpus6.FirstName2, gender)
            6 -> array = genderFilter(FirstNameCorpus7.FirstName2, gender)
            7 -> array = genderFilter(FirstNameCorpus8.FirstName2, gender)
            8 -> array = genderFilter(FirstNameCorpus9.FirstName2, gender)
            9 -> array = genderFilter(FirstNameCorpus10.FirstName2, gender)
        }
        return array[ Random().nextInt(array.size)].split(",")[0]
    }

    private fun getFirstName1(gender:Gender):String{
        val array = genderFilter(NameCorpus.FirstName1, gender)
        return  array[ Random().nextInt(array.size)].split(",")[0]
    }

    private fun genderFilter(list:List<String>, gender:Gender):List<String>{
        return list.filter {
            it.split(",")[1] == gender.props || it.split(",")[1] == Gender.Default.props
        }
    }

    private fun randomTwoName(lastName:String?, gender:Gender = Gender.Default):String{
        return (lastName ?: NameCorpus.LAST_NAME1[ Random().nextInt(NameCorpus.LAST_NAME1.size)]) + getFirstName1(gender)

    }

    private fun randomThreeName(lastName:String?, gender:Gender = Gender.Default):String{
        return  (lastName ?: NameCorpus.LAST_NAME1[ Random().nextInt(NameCorpus.LAST_NAME1.size)]) + getFirstName2(gender)
    }

    private fun randomThreeNames(lastName:String?, gender:Gender = Gender.Default):String{
        return  (lastName ?: NameCorpus.LAST_NAME2[ Random().nextInt(NameCorpus.LAST_NAME2.size)]) + getFirstName1(gender)
    }

    private fun randomFourName(lastName:String?, gender:Gender = Gender.Default):String{
        return  (lastName ?: NameCorpus.LAST_NAME2[ Random().nextInt(NameCorpus.LAST_NAME2.size)]) + getFirstName2(gender)
    }

}