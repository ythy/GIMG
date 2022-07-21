package com.mx.gillustrated.fragment

import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEquipmentAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.dialog.FragmentDialogEquipment
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.Person

class FragmentEquipment: Fragment() {

    private val mConfigEquipments = CultivationHelper.mConfig.equipment

    lateinit var mListView: ListView


    @OnClick(R.id.btn_add_equipment)
    fun onAddClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        if( mPerson.equipment.find { it.split(",")[0] == equipment.id } != null)
                            return
                        val exist = mPerson.equipment.find { e-> mConfigEquipments.find { c-> c.id == e.split(",")[0] }?.type == equipment.type }
                        if(equipment.type > 0 && exist != null){
                            mPerson.equipment.remove(exist)
                        }
                        mPerson.equipment.add("${equipment.id},${equipment.name}")
                        CultivationHelper.updatePersonEquipment(mPerson)
                        updateList()
                    }
                }, Range(1, 10))
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
                        if( mPerson.equipment.find { it.split(",")[0] == equipment.id } != null)
                            return
                        mPerson.equipment.add("${equipment.id},${equipment.name}")
                        CultivationHelper.updatePersonEquipment(mPerson)
                        updateList()
                    }
                }, Range(0, 0))
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_equipment")
    }



    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    private val mEquipments: MutableList<Equipment> = mutableListOf()

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
        val id = this.requireArguments().getString("id", "")
        mPerson = mContext.getOnlinePersonDetail(id) ?: mContext.getOfflinePersonDetail(id)!!
        mListView.adapter = CultivationEquipmentAdapter(this.requireContext(), mEquipments, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {
            override fun onDeleteHandler(equipment: Equipment) {
                mPerson.equipment.removeIf {
                    val equipmentArray = it.split(",")
                    if(equipment.type > 10)
                        equipmentArray[1] == equipment.uniqueName
                    else
                        equipmentArray[0] == equipment.id
                }
                CultivationHelper.updatePersonEquipment(mPerson)
                updateList()
            }
        })
        updateList()
    }

    fun updateList(){
        val equipments = mPerson.equipment.map {
            val equipment = mConfigEquipments.find { e-> e.id == it.split(",")[0] }!!
            val result = Equipment()
            result.id = equipment.id
            result.name = equipment.name
            result.uniqueName = if(equipment.type > 10) it.split(",")[1] else equipment.name
            result.type = equipment.type
            result.rarity = equipment.rarity
            result.xiuwei = equipment.xiuwei
            result.success = equipment.success
            result.property = equipment.property
            result
        }.toMutableList()
        mEquipments.clear()
        equipments.sortWith(compareBy<Equipment> {it.type}.thenBy { it.rarity })
        mEquipments.addAll(equipments)
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
    }




}