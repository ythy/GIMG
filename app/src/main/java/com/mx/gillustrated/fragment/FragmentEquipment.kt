package com.mx.gillustrated.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEquipmentAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.dialog.FragmentDialogEquipment
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.Person

@RequiresApi(Build.VERSION_CODES.N)
class FragmentEquipment: Fragment() {

    private val mConfigEquipments = CultivationHelper.mConfig.equipment

    @BindView(R.id.lv_equipment)
    lateinit var mListView: ExpandableListView


    @OnClick(R.id.btn_add_equipment)
    fun onAddClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        updateEquipment(equipment)
                    }
                }, mutableListOf(1))
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_equipment")
    }

    @OnClick(R.id.btn_add_equipment_bao)
    fun onAddBaoClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        updateEquipment(equipment)
                    }
                }, mutableListOf(0, 9))
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_equipment")
    }

    @OnClick(R.id.btn_add_equipment_armor)
    fun onAddArmorClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        updateEquipment(equipment)
                    }
                }, mutableListOf(2,3))
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_equipment")
    }



    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    private val mEquipmentGroups: MutableList<Equipment> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vp_equipment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
        init()
    }

    fun init(){
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        updateList()
    }

    fun updateList(){
        val equipments = mPerson.equipmentList.map {
            val equipment = mConfigEquipments.find { e-> e.id == it.first}!!.copy()
            equipment.seq = it.second
            equipment.uniqueName = if(equipment.seq > 0) "${equipment.name}-${equipment.seq}" else equipment.name
            equipment
        }.sortedWith(compareByDescending<Equipment> {
            if(it.type <= 10 ) it.type + 100 + 10 * ( 10 - it.type ) // +100 : type<=10整体排序靠前; 10 * x  type越小加得越多
            else it.type
        }.thenByDescending { it.rarity }.thenBy { it.seq })

        mEquipmentGroups.clear()
        val groups = equipments.groupBy{ it.type * 10000 + (if( it.type <= 10) 0 else it.rarity) }
        .map {
            it.value[0].children.clear()
            it.value[0].children.addAll(it.value)
            it.value[0]
        }
        mEquipmentGroups.addAll(groups)
        mListView.setAdapter(CultivationEquipmentAdapter(requireContext(), mEquipmentGroups, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {
            override fun onDeleteHandler(equipment: Equipment, group:Boolean) {
                mPerson.equipmentList.removeIf {
                    if(group){
                        it.first == equipment.id
                    }else{
                        it.first == equipment.id && it.second == equipment.seq
                    }
                }
                CultivationHelper.updatePersonEquipment(mPerson)
                updateList()
            }
        }))
    }

    fun updateEquipment(equipment:Equipment){
        if( mPerson.equipmentList.find { it.first == equipment.id } != null)
            return
        mPerson.equipmentList.add(Triple(equipment.id, 0, ""))
        CultivationHelper.updatePersonEquipment(mPerson)
        updateList()
    }


}