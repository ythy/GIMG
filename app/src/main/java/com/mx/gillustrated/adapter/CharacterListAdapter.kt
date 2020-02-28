package com.mx.gillustrated.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import com.mx.gillustrated.R
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.CharacterInfo
import java.util.Arrays
import butterknife.BindView
import butterknife.ButterKnife

class CharacterListAdapter(context: Context, items: List<CharacterInfo>, associationList: List<CardInfo>?) : BaseAdapter() {

    private val mcontext: Context = context
    private val layoutInflator: LayoutInflater
    private val list: List<CharacterInfo> = items
    private val associationList: List<CardInfo>? = associationList
    private val charPropDatas: List<String>
    private val charDomainDatas: List<String>
    private val charNationalDatas: List<String>
    private val charAgeDatas: List<String>
    private val charLineDatas: List<String>
    private val charCharDatas: List<String>
    private var mListener: CharacterListAdapter.CharacterTouchListener? = null

    companion object{
       const val FONT_SIZE = 12
    }


    init {
        layoutInflator = LayoutInflater.from(mcontext)
        charPropDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_prop))
        charDomainDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_domain))
        charNationalDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_nationality))
        charAgeDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_age))
        charLineDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_line))
        charCharDatas = Arrays.asList(*mcontext.resources.getStringArray(R.array.char_character))
    }


    fun setCharacterTouchListener(listener: CharacterListAdapter.CharacterTouchListener) {
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
        var convertView = convertViews
        var component: Component? = null

        if (convertView == null) {
            convertView = layoutInflator.inflate(
                    R.layout.adapter_character, null)
            component = Component(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as Component

        val currentComponent = component
        try {

            component.etName!!.setText(list[arg0].name)
            component.spinnerNational!!.adapter = SpinnerSimpleAdapter(mcontext, charNationalDatas, FONT_SIZE)
            component.spinnerNational!!.setSelection(list[arg0].nationality)
            component.spinnerAge!!.adapter = SpinnerSimpleAdapter(mcontext, charAgeDatas, FONT_SIZE)
            component.spinnerAge!!.setSelection(list[arg0].age)
            component.spinnerLine!!.adapter = SpinnerSimpleAdapter(mcontext, charLineDatas, FONT_SIZE)
            component.spinnerLine!!.setSelection(list[arg0].skilled)
            component.spinnerChar!!.adapter = SpinnerSimpleAdapter(mcontext, charCharDatas, FONT_SIZE)
            component.spinnerChar!!.setSelection(list[arg0].character)

            component.spinnerProp!!.adapter = SpinnerSimpleAdapter(mcontext, charPropDatas, FONT_SIZE)
            component.spinnerProp!!.setSelection(list[arg0].prop)
            component.spinnerDomain!!.adapter = SpinnerSimpleAdapter(mcontext, charDomainDatas, FONT_SIZE)
            component.spinnerDomain!!.setSelection(list[arg0].domain)

            var newAssociationList:MutableList<CardInfo>? = null
            if (associationList != null) {
                newAssociationList = mutableListOf(CardInfo(0,""))
                associationList.forEach {
                    val national = charNationalDatas[list[arg0].nationality]
                    val sexuality = charCharDatas[list[arg0].character]
                    val line = charLineDatas[list[arg0].skilled]
                    val age = charAgeDatas[list[arg0].age]
                    val domain = charDomainDatas[list[arg0].domain]

                    if(national != "无" && national != it.attr)
                        return@forEach
                    if( (sexuality.startsWith("女") || sexuality.startsWith("男")) ){
                        if(sexuality.substring(0, 1) != it.maxHP)
                            return@forEach
                    }
                    if( line != "无" && line != it.maxAttack.substring(0, 1))
                        return@forEach
                    if( age != "无" && age != it.maxDefense.substring(0, 1))
                        return@forEach

                    var newName = it.name
                    if( domain != "无" && it.extraValue1.substring(0,1) != domain.substring(20, 21))
                        newName = "<font color=\"red\">$newName</font>"
                    newAssociationList.add(CardInfo(it.id, newName))
                }

                component.spinnerMatching!!.adapter = SpinnerCommonAdapter(mcontext, newAssociationList, false, FONT_SIZE)
                component.spinnerMatching!!.setSelection(getAssociationSelection(newAssociationList, list[arg0].association))
            }


            val finalComponent = component
            component.btnModify!!.setOnClickListener {
                val name = currentComponent.etName!!.text.toString()
                val id = list[arg0].id
                val gid = list[arg0].gameId
                val characterInfo = CharacterInfo()
                characterInfo.gameId = gid
                characterInfo.id = id
                characterInfo.name = name
                characterInfo.nationality = finalComponent.spinnerNational!!.selectedItemPosition
                characterInfo.domain = finalComponent.spinnerDomain!!.selectedItemPosition
                characterInfo.age = finalComponent.spinnerAge!!.selectedItemPosition
                characterInfo.skilled = finalComponent.spinnerLine!!.selectedItemPosition
                characterInfo.character = finalComponent.spinnerChar!!.selectedItemPosition
                characterInfo.prop = finalComponent.spinnerProp!!.selectedItemPosition
                if(newAssociationList != null){
                    val associationIndex = finalComponent.spinnerMatching!!.selectedItemPosition
                    characterInfo.association = newAssociationList[associationIndex].name
                }
                mListener!!.onSaveBtnClickListener(characterInfo, arg0)
            }

            component.btnDel!!.setOnClickListener {
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

    inner class Component(view: View) {

        @JvmField
        @BindView(R.id.etCharName)
        var etName: EditText? = null

        @JvmField
        @BindView(R.id.spinnerChar)
        var spinnerChar: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerNational)
        var spinnerNational: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerDomain)
        var spinnerDomain: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerAge)
        var spinnerAge: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerSkilled)
        var spinnerLine: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerProp)
        var spinnerProp: Spinner? = null

        @JvmField
        @BindView(R.id.spinnerMatching)
        var spinnerMatching: Spinner? = null

        @JvmField
        @BindView(R.id.btnCharModify)
        var btnModify: ImageButton? = null

        @JvmField
        @BindView(R.id.btnCharDel)
        var btnDel: ImageButton? = null

        init {
            ButterKnife.bind(this, view)
        }

    }
}
