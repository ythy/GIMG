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
        val detail = values.detail
        val child = values.childrenAll
        if(detail.type <= 3 || detail.type == 9) {
            component.name.text = CultivationHelper.showing(detail.name)
        }else if(detail.type == 8){
            val tejiString = if (detail.teji.size > 0) "+" else  ""
            component.name.text = CultivationHelper.showing("\uD83D\uDD05 " + values.uniqueName + tejiString)
            component.name.setOnClickListener{
                if(tejiString == "+")
                    Toast.makeText(mContext, detail.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
            }
        }else if(detail.type == 5){
            component.name.text = "${CultivationHelper.showing(detail.name)}(${values.children.size})"
        }else if(detail.type == 6){
            component.name.text = CultivationHelper.showing(values.uniqueName)
            component.name.setOnClickListener{
                callbacks.onOpenDetailList(values)
            }
        }else if(detail.type == 7){
            val tejiString = if (detail.teji.size > 0) "+" else  ""
            component.name.text = CultivationHelper.showing("\uD83D\uDCDC " + values.uniqueName + tejiString)
            if (child.isEmpty() && tejiString == "+"){
                component.name.setOnClickListener{
                    Toast.makeText(mContext, detail.teji.joinToString { CultivationBattleHelper.tejiDetail(it).name }, Toast.LENGTH_SHORT).show()
                }
            }
        }
        component.name.setTextColor(Color.parseColor(CommonColors[detail.rarity]))
        val properties = mutableListOf(0,0,0,0)
        if(detail.type <= 3 ){
            val maxEquipment = child.maxBy { it.detail.rarity }!!
            component.xiuwei.text = maxEquipment.detail.xiuwei.toString()
            component.success.text = maxEquipment.detail.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += maxEquipment.detail.property[index]
            }
        }else if(detail.type == 8){
            component.xiuwei.text = detail.xiuwei.toString()
            component.success.text = detail.success.toString()
            (0 until 4).forEach { index ->
                properties[index] += detail.property[index]
            }
        }else if(detail.type == 9 || detail.type == 5){
            component.xiuwei.text = child.sumBy { it.detail.xiuwei }.toString()
            component.success.text = child.sumBy { it.detail.success }.toString()
            child.forEach { equipment->
                (0 until 4).forEach { index ->
                    properties[index] += equipment.detail.property[index]
                }
            }
        }else if(detail.type == 6 || detail.type == 7){
            component.xiuwei.text = detail.xiuwei.toString()
            component.success.text = "0"
            (0 until 4).forEach { index ->
                properties[index] += detail.property[index]
            }
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
        val detail = values.detail
        val tejiString = if (detail.teji.size > 0) "+" else  ""
        val followerString = if (detail.follower.size > 0) "#" else  ""
        component.name.text = CultivationHelper.showing(values.uniqueName+tejiString+followerString)
        component.name.setTextColor(Color.parseColor(CommonColors[detail.rarity]))
        component.xiuwei.text = "${detail.xiuwei}"
        component.success.text = "${detail.success}"
        component.props.text = detail.property.take(4).joinToString()

        if(detail.type == 9 || detail.type == 5 ||  detail.type == 7 ){//bao and amulet can delete
            component.del.visibility = View.VISIBLE
        }else{
            component.del.visibility = View.GONE
        }

        component.del.setOnClickListener{
            callbacks.onDeleteHandler(values, false)
        }
        component.name.setOnClickListener{
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