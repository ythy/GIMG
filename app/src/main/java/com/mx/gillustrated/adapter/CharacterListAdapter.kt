package com.mx.gillustrated.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mx.gillustrated.R
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.CharacterInfo
import java.util.Arrays
import com.mx.gillustrated.databinding.AdapterCharacterBinding

class CharacterListAdapter(private val context: Context, items: List<CharacterInfo>, private val associationList: List<CardInfo>?) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val list: List<CharacterInfo> = items
    private val charPropData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_prop))
    private val charDomainData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_domain))
    private val charNationalData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_nationality))
    private val charAgeData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_age))
    private val charLineData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_line))
    private val charCharData: List<String> = Arrays.asList(*context.resources.getStringArray(R.array.char_character))
    private var mListener: CharacterTouchListener? = null

    companion object{
       const val FONT_SIZE = 12
    }

    fun setCharacterTouchListener(listener: CharacterTouchListener) {
        mListener = listener
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(arg0: Int): Any {
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews //目的可修改
        lateinit var component: AdapterCharacterBinding

        if (convertView == null) {
            component = AdapterCharacterBinding.inflate(layoutInflater, arg2, false )
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterCharacterBinding

        try {

            component.etCharName.setText(list[arg0].name)
            component.spinnerNational.adapter = SpinnerSimpleAdapter(context, charNationalData, FONT_SIZE)
            component.spinnerNational.setSelection(list[arg0].nationality)
            component.spinnerAge.adapter = SpinnerSimpleAdapter(context, charAgeData, FONT_SIZE)
            component.spinnerAge.setSelection(list[arg0].age)
            component.spinnerSkilled.adapter = SpinnerSimpleAdapter(context, charLineData, FONT_SIZE)
            component.spinnerSkilled.setSelection(list[arg0].skilled)
            component.spinnerChar.adapter = SpinnerSimpleAdapter(context, charCharData, FONT_SIZE)
            component.spinnerChar.setSelection(list[arg0].character)

            component.spinnerProp.adapter = SpinnerSimpleAdapter(context, charPropData, FONT_SIZE)
            component.spinnerProp.setSelection(list[arg0].prop)
            component.spinnerDomain.adapter = SpinnerSimpleAdapter(context, charDomainData, FONT_SIZE)
            component.spinnerDomain.setSelection(list[arg0].domain)

            var newAssociationList:MutableList<CardInfo>? = null
            if (associationList != null) {
                newAssociationList = mutableListOf(CardInfo(0,""))
                associationList.forEach {
                    val national = charNationalData[list[arg0].nationality]
                    val sexuality = charCharData[list[arg0].character]
                    val line = charLineData[list[arg0].skilled]
                    val age = charAgeData[list[arg0].age]
                    val domain = charDomainData[list[arg0].domain]

                    if(national != "无" && national != it.attr)
                        return@forEach
                    if( (sexuality.startsWith("女") || sexuality.startsWith("男")) ){
                        if(sexuality.substring(0, 1) != it.maxHP)
                            return@forEach
                    }
                    if( line != "无" && line != it.maxAttack?.substring(0, 1))
                        return@forEach
                    if( age != "无" && age != it.maxDefense?.substring(0, 1))
                        return@forEach

                    var newName = it.name
                    if( domain != "无" && it.extraValue1?.substring(0,1) != domain.substring(20, 21))
                        newName = "<font color=\"red\">$newName</font>"
                    newAssociationList.add(CardInfo(it.id, newName))
                }

                component.spinnerMatching.adapter = SpinnerCommonAdapter(context, newAssociationList, false, FONT_SIZE)
                component.spinnerMatching.setSelection(getAssociationSelection(newAssociationList, list[arg0].association))
            }


            val finalComponent = component
            component.btnCharModify.setOnClickListener {
                val name = component.etCharName.text.toString()
                val id = list[arg0].id
                val gid = list[arg0].gameId
                val characterInfo = CharacterInfo()
                characterInfo.gameId = gid
                characterInfo.id = id
                characterInfo.name = name
                characterInfo.nationality = finalComponent.spinnerNational.selectedItemPosition
                characterInfo.domain = finalComponent.spinnerDomain.selectedItemPosition
                characterInfo.age = finalComponent.spinnerAge.selectedItemPosition
                characterInfo.skilled = finalComponent.spinnerSkilled.selectedItemPosition
                characterInfo.character = finalComponent.spinnerChar.selectedItemPosition
                characterInfo.prop = finalComponent.spinnerProp.selectedItemPosition
                if(newAssociationList != null){
                    val associationIndex = finalComponent.spinnerMatching.selectedItemPosition
                    characterInfo.association = newAssociationList[associationIndex].name ?: ""
                }
                mListener!!.onSaveBtnClickListener(characterInfo, arg0)
            }

            component.btnCharDel.setOnClickListener {
                val id = list[arg0].id
                val gid = list[arg0].gameId
                val cardTypeInfo = CharacterInfo()
                cardTypeInfo.id = id
                cardTypeInfo.gameId = gid
                mListener!!.onDelBtnClickListener(cardTypeInfo, arg0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return convertView
    }

    private fun getAssociationSelection(list:List<CardInfo>, input: String?): Int {
        var result = 0
        list.forEachIndexed { index, cardInfo ->
            if (cardInfo.name == input)
                result = index
        }
        return result
    }

    interface CharacterTouchListener {
        fun onSaveBtnClickListener(info: CharacterInfo, index: Int)
        fun onDelBtnClickListener(info: CharacterInfo, index: Int)
    }
}
