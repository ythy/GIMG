package com.mx.gillustrated.adapter


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationEnemyHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting.CommonColors
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

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        lateinit var component: ViewHolderGroup

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_cultivation_equipment, parent, false)
            component = ViewHolderGroup(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolderGroup

        val values = getGroup(groupPosition)
        val child = values.childrenAll
        if(values.type <= 3 || values.type == 9)
            component.name.text = CultivationHelper.showing(values.name)
        else if(values.type == 8){
            val tejiString = if (values.teji.size > 0) "+" else  ""
            component.name.text = CultivationHelper.showing(values.name + tejiString)
            component.name.setOnClickListener{
                if(tejiString == "+")
                    Toast.makeText(mContext, values.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
            }
        }else if(values.type == 5){
            component.name.text = "${CultivationHelper.showing(values.name)}(${values.children.size})"
        }else if(values.type == 6){
            component.name.text = "${CultivationHelper.showing(values.name)}(${values.seq}/${CultivationHelper.getValidBonus(values.seq)})"
            component.name.setOnClickListener{
                callbacks.onOpenDetailList(values)
            }
        }
        component.name.setTextColor(Color.parseColor(CommonColors[values.rarity]))
        val properties = mutableListOf(0,0,0,0)
        if(values.type <= 3 ){
            val maxEquipment = child.maxBy { it.rarity }!!
            component.xiuwei.text = maxEquipment.xiuwei.toString()
            component.success.text = maxEquipment.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += maxEquipment.property[index]
            }
        }else if(values.type == 8){
            component.xiuwei.text = values.xiuwei.toString()
            component.success.text = values.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += values.property[index]
            }
        }else if(values.type == 9 || values.type == 5){
            component.xiuwei.text = child.sumBy { it.xiuwei }.toString()
            component.success.text = child.sumBy { it.success }.toString()
            child.forEach { equipment->
                (0 until 4).forEach { index ->
                    properties[index] += equipment.property[index]
                }
            }
        }else if(values.type == 6){
            component.xiuwei.text = (CultivationEnemyHelper.bossSettings[values.id.toInt()].bonus * CultivationHelper.getValidBonus(values.seq)).toString()
        }
        component.props.text = properties.joinToString()
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

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        lateinit var component: ViewHolderChild

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_cultivation_equipment_child, parent, false)
            component = ViewHolderChild(convertView)
            convertView!!.tag = component
        } else
            component = convertView.tag as ViewHolderChild

        val values = getChild(groupPosition, childPosition)
        val tejiString = if (values.teji.size > 0) "+" else  ""
        component.name.text = CultivationHelper.showing(values.uniqueName+tejiString)
        component.name.setTextColor(Color.parseColor(CommonColors[values.rarity]))
        component.xiuwei.text = "${values.xiuwei}"
        component.success.text = "${values.success}"
        component.props.text = values.property.take(4).joinToString()

        if(values.type == 9 || values.type == 5){//bao and amulet can delete
            component.del.visibility = View.VISIBLE
        }else{
            component.del.visibility = View.GONE
        }

        component.del.setOnClickListener{
            callbacks.onDeleteHandler(values, false)
        }
        component.name.setOnClickListener{
            if(tejiString == "+")
                Toast.makeText(mContext, values.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
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


    internal class ViewHolderGroup(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei: TextView

        @BindView(R.id.tv_success)
        lateinit var success: TextView

        @BindView(R.id.tv_props)
        lateinit var props: TextView


        init {
            ButterKnife.bind(this, view)
        }
    }

    internal class ViewHolderChild(view: View) {

        @BindView(R.id.tv_name)
        lateinit var name: TextView

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei: TextView

        @BindView(R.id.tv_success)
        lateinit var success: TextView

        @BindView(R.id.tv_props)
        lateinit var props: TextView


        @BindView(R.id.btnDel)
        lateinit var del: ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }

    interface EquipmentAdapterCallback {
        fun onDeleteHandler(equipment: Equipment, group:Boolean)
        fun onOpenDetailList(equipment: Equipment)
    }

}