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
import com.mx.gillustrated.dialog.FragmentDialogRank
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.EquipmentConfig
import com.mx.gillustrated.vo.cultivation.Person

@RequiresApi(Build.VERSION_CODES.N)
class FragmentEquipment: Fragment() {

    @BindView(R.id.lv_equipment)
    lateinit var mListView: ExpandableListView

    @OnClick(R.id.btn_add_equipment_bao)
    fun onAddBaoClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: EquipmentConfig) {
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
        val equipments = mPerson.equipmentList.sortedWith(compareByDescending<Equipment> {
            it.sortedWeight
        }.thenByDescending { it.seq }).toMutableList()
        val bossEquipment = mPerson.bossRound.mapIndexedNotNull{ index, count ->
            if (count == 0)
                null
            else
                Equipment(index.toString(), 0, Triple(index, count, ""))
        }
        val tipsEquipment = mPerson.tipsList.map{ tips ->
            Equipment(tips.id, 0, Triple(tips.level, 0, "TIPS"))
        }
        mEquipmentGroups.clear()
        // 0 1 2 3 by type / 5 by id ; 9 by id / 6 and 8 不在equiplist
        val groups = equipments.groupBy{ if(it.detail.type <= 3) it.detail.type  else it.id.toInt() }
        .map {
            it.value[0].children.clear()
            it.value[0].childrenAll.clear()
            it.value[0].children.addAll(it.value)
            it.value[0].childrenAll.addAll(it.value)
            it.value[0]
        }
        mEquipmentGroups.addAll(groups)
        mEquipmentGroups.addAll(bossEquipment)
        mEquipmentGroups.addAll(tipsEquipment)

        mListView.setAdapter(CultivationEquipmentAdapter(requireContext(), mEquipmentGroups, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {

            override fun onOpenDetailList(equipment: Equipment) {
                openRankDialog(equipment.id)
            }

            override fun onDeleteHandler(equipment: Equipment, group:Boolean) {
                mPerson.equipmentList.removeIf {
                    if(group){
                        it.id == equipment.id
                    }else{
                        it.id == equipment.id && it.seq == equipment.seq
                    }
                }
                CultivationHelper.updatePersonEquipment(mPerson)
                updateList()
            }

        }))
    }

    fun updateEquipment(equipment:EquipmentConfig){
        if( mPerson.equipmentList.find { it.id == equipment.id } != null)
            return
        mPerson.equipmentList.add(Equipment(equipment.id))
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