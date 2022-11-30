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
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.dialog.FragmentDialogEquipment
import com.mx.gillustrated.dialog.FragmentDialogRank
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.Person

@RequiresApi(Build.VERSION_CODES.N)
class FragmentEquipment: Fragment() {

    private val mConfigEquipments = CultivationHelper.mConfig.equipment

    @BindView(R.id.lv_equipment)
    lateinit var mListView: ExpandableListView

    @OnClick(R.id.btn_add_equipment_bao)
    fun onAddBaoClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        updateEquipment(equipment)
                    }
                }, mutableListOf(9))
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
        val exclusives =  CultivationHelper.mConfig.equipment.filter { it.type == 8 && it.spec.contains(mPerson.specIdentity)}.map {
            it.children.clear()
            it.childrenAll.clear()
            it
        }.sortedByDescending { it.rarity }
        val equipments = mPerson.equipmentListPair.map {
            var equipment = mConfigEquipments.find { e-> e.id == it.first}!!.copy()
            if(equipment.type == 5){
                equipment = CultivationSetting.getEquipmentCustom(it)
            }else{
                equipment.seq = it.second
                equipment.uniqueName = if(equipment.seq > 0) "${equipment.name}-${equipment.seq}" else equipment.name
            }
            equipment
        }.sortedWith(compareBy<Equipment> {
            it.type
        }.thenByDescending { it.rarity }.thenByDescending { it.seq })

        mEquipmentGroups.clear()
        val groups = equipments.groupBy{ if(it.type <= 3) it.type  else it.type * 100 +  it.id.toInt() % 10 }
        .map {
            it.value[0].children.clear()
            it.value[0].childrenAll.clear()
            if(it.value[0].type == 5){
                it.value[0].children.addAll(it.value)
            }else
                it.value[0].children.addAll(it.value.subList(0, Math.min(10, it.value.size)))
            it.value[0].childrenAll.addAll(it.value)
            it.value[0]
        }
        mEquipmentGroups.addAll(exclusives)
        mEquipmentGroups.addAll(groups)

        mListView.setAdapter(CultivationEquipmentAdapter(requireContext(), mEquipmentGroups, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {

            override fun onOpenDetailList(equipment: Equipment) {
                openRankDialog(equipment.id)
            }

            override fun onDeleteHandler(equipment: Equipment, group:Boolean) {
                mPerson.equipmentListPair.removeIf {
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
        if( mPerson.equipmentListPair.find { it.first == equipment.id } != null)
            return
        mPerson.equipmentListPair.add(Pair(equipment.id, 0))
        CultivationHelper.updatePersonEquipment(mPerson)
        updateList()
    }

    private fun openRankDialog(id:String){
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(7, mPerson.id, id)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }

}