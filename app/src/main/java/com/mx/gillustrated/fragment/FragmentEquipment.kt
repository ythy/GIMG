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
                        updateEquipment(equipment, false)
                    }
                }, Range(1, 1))
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
                        updateEquipment(equipment, false)
                    }
                }, Range(0, 0))
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
                        updateEquipment(equipment, false)
                    }
                }, Range(2, 2))
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
        }.toMutableList()
        mEquipmentGroups.clear()
        equipments.sortWith(compareBy <Equipment> {
            val extra = if( it.type <= 10 ) -10 else if( it.type < 20 && it.type != 12)  20 - 2 * it.type else 0
            it.type + extra
        }.thenByDescending {
            if(it.seq == 0)
                it.rarity
            else
                -it.seq
        })
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

    fun updateEquipment(equipment:Equipment, autoCheck:Boolean = true){
        if( mPerson.equipmentList.find { it.first == equipment.id } != null)
            return
        if(autoCheck){
            val exist = mPerson.equipmentList.find { e-> mConfigEquipments.find {
                c-> c.id == e.first }?.type == equipment.type
            }
            if(equipment.type > 0 && exist != null){
                mPerson.equipmentList.remove(exist)
            }
        }
        mPerson.equipmentList.add(Triple(equipment.id, 0, ""))
        CultivationHelper.updatePersonEquipment(mPerson)
        updateList()
    }


}