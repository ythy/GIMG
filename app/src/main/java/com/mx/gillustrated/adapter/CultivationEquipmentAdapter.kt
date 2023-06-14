package com.mx.gillustrated.adapter


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Toast
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.databinding.AdapterCultivationEquipmentBinding
import com.mx.gillustrated.databinding.AdapterCultivationEquipmentChildBinding
import com.mx.gillustrated.vo.cultivation.Equipment

class CultivationEquipmentAdapter constructor(private val mContext: Context, private val grouplist: List<Equipment>, private val callbacks: EquipmentAdapterCallback) : BaseExpandableListAdapter() {

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroup(groupPosition: Int): Equipment {
        return grouplist[groupPosition]
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertViews: View?, parent: ViewGroup?): View {
        var convertView = convertViews
        lateinit var component: AdapterCultivationEquipmentBinding

        if (convertView == null) {
            component = AdapterCultivationEquipmentBinding.inflate(layoutInflater, parent, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterCultivationEquipmentBinding

        val values = getGroup(groupPosition)
        val detail = values.detail
        val child = values.childrenAll
        if(detail.type <= 3 || detail.type == 9) {
            component.tvName.text = CultivationHelper.showing(detail.name)
        }else if(detail.type == 8){
            val tejiString = if (detail.teji.size > 0) "+" else  ""
            component.tvName.text = CultivationHelper.showing("\uD83D\uDD05 " + values.uniqueName + tejiString)
            component.tvName.setOnClickListener{
                if(tejiString == "+")
                    Toast.makeText(mContext, detail.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
            }
        }else if(detail.type == 5){
            component.tvName.text = "${CultivationHelper.showing(detail.name)}(${values.children.size})"
        }else if(detail.type == 6){
            component.tvName.text = CultivationHelper.showing(values.uniqueName)
            component.tvName.setOnClickListener{
                callbacks.onOpenDetailList(values)
            }
        }else if(detail.type == 7){
            val tejiString = if (detail.teji.size > 0) "+" else  ""
            component.tvName.text = CultivationHelper.showing(values.uniqueName + tejiString)
            if (child.isEmpty() && tejiString == "+"){
                component.tvName.setOnClickListener{
                    Toast.makeText(mContext, detail.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
                }
            }
        }
        component.tvName.setTextColor(Color.parseColor(CommonColors[detail.rarity]))
        val properties = mutableListOf(0,0,0,0)
        if(detail.type <= 3 ){
            val maxEquipment = child.maxByOrNull { it.detail.rarity }!!
            component.tvXiuwei.text = maxEquipment.detail.xiuwei.toString()
            component.tvSuccess.text = maxEquipment.detail.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += maxEquipment.detail.property[index]
            }
        }else if(detail.type == 8){
            component.tvXiuwei.text = detail.xiuwei.toString()
            component.tvSuccess.text = detail.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += detail.property[index]
            }
        }else if(detail.type == 9 || detail.type == 5){
            component.tvXiuwei.text = child.sumOf { it.detail.xiuwei }.toString()
            component.tvSuccess.text = child.sumOf { it.detail.success }.toString()
            child.forEach { equipment->
                (0 until 4).forEach { index ->
                    properties[index] += equipment.detail.property[index]
                }
            }
        }else if(detail.type == 6 || detail.type == 7){
            component.tvXiuwei.text = detail.xiuwei.toString()
            component.tvSuccess.text = "0"
            (0 until 4).forEach { index ->
                properties[index] += detail.property[index]
            }
        }
        component.tvProps.text = properties.joinToString()
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return getGroup(groupPosition).children.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Equipment {
        return getGroup(groupPosition).children[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertViews: View?, parent: ViewGroup?): View {
        var convertView = convertViews
        lateinit var component: AdapterCultivationEquipmentChildBinding

        if (convertView == null) {
            component = AdapterCultivationEquipmentChildBinding.inflate(layoutInflater, parent, false)
            convertView = component.root
            convertView.tag = component
        } else
            component = convertView.tag as AdapterCultivationEquipmentChildBinding

        val values = getChild(groupPosition, childPosition)
        val detail = values.detail
        val tejiString = if (detail.teji.size > 0) "+" else  ""
        val followerString = if (detail.follower.size > 0) "#" else  ""
        component.tvName.text = CultivationHelper.showing(values.uniqueName+tejiString+followerString)
        component.tvName.setTextColor(Color.parseColor(CommonColors[detail.rarity]))
        component.tvXiuwei.text = "${detail.xiuwei}"
        component.tvSuccess.text = "${detail.success}"
        component.tvProps.text = detail.property.take(4).joinToString()

        if(detail.type == 9 || detail.type == 5 ||  detail.type == 7 ){//bao and amulet can delete
            component.btnDel.visibility = View.VISIBLE
        }else{
            component.btnDel.visibility = View.GONE
        }

        component.btnDel.setOnClickListener{
            callbacks.onDeleteHandler(values, false)
        }
        component.tvName.setOnClickListener{
            var toastString = ""
            if(tejiString == "+"){
                toastString += detail.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }
            }
            if(followerString == "#"){
                toastString += detail.follower.joinToString {  CultivationHelper.mConfig.follower.find { f-> f.id == it }?.name ?: "" }
            }
            if(toastString != "")
                Toast.makeText(mContext, toastString, Toast.LENGTH_SHORT).show()
        }
        return convertView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return grouplist.size
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)





    interface EquipmentAdapterCallback {
        fun onDeleteHandler(equipment: Equipment, group:Boolean)
        fun onOpenDetailList(equipment: Equipment)
    }

}