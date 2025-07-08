package com.mx.gillustrated.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mx.gillustrated.activity.GameBaseActivity
import com.mx.gillustrated.adapter.CultivationEquipmentAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.FragmentVpEquipmentBinding
import com.mx.gillustrated.dialog.FragmentDialogEquipment
import com.mx.gillustrated.dialog.FragmentDialogRank
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.EquipmentConfig
import com.mx.gillustrated.vo.cultivation.Person


class FragmentEquipment: Fragment() {

    private var _binding: FragmentVpEquipmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var mContext: GameBaseActivity
    lateinit var mPerson: Person
    private val mEquipmentGroups: MutableList<Equipment> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVpEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as GameBaseActivity
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        val id = requireArguments().getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        binding.tvGenre.text = mPerson.genres.mapNotNull { CultivationHelper.mConfig.genre.find { f-> f.id == it } }.joinToString{ it.name }
        binding.btnAddEquipmentBao.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogEquipment.
            newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                override fun onItemSelected(equipment: EquipmentConfig) {
                    updateEquipment(equipment)
                }
            }, mutableListOf(9))
            newFragment.isCancelable = true
            newFragment.show(ft, "dialog_equipment")
        }
        updateList()
    }

    fun updateList(){
        val equipments = mPerson.equipmentList.sortedWith(compareByDescending<Equipment> {
            it.sortedWeight
        }.thenByDescending { it.amuletSerialNo }).toMutableList()
        val tipsEquipment = mPerson.tipsList.map{ tips ->
            val tipsConfig =  CultivationHelper.mConfig.tips.find { it.id == tips.id }!!
            val setting = CultivationHelper.getEquipmentOfTips(tips.level, tipsConfig)
            val equipment = Equipment("", 0, setting.first)
            val prefix = if (tipsConfig.type == 1) "\u2694" else "\uD83D\uDCDC"
            equipment.uniqueName = "$prefix ${tips.tipsName}${setting.second}"
            if (tips.detail.type > 2){
                equipment.children.clear()
                equipment.children.add(equipment)
            }
            equipment
        }.sortedBy { it.sortedWeight }
        mEquipmentGroups.clear()
        // 0 1 2 3 by type / 5 by id ; 9 by id / 6 7 8 不在equiplist
        val groups = equipments.groupBy{ if(it.detail.type <= 3) it.detail.type  else it.id.toInt() }
        .map {
            it.value[0].children.clear()
            it.value[0].childrenAll.clear()
            it.value[0].children.addAll(it.value)
            it.value[0].childrenAll.addAll(it.value)
            it.value[0]
        }
        mEquipmentGroups.addAll(groups)
        mEquipmentGroups.addAll(tipsEquipment)

        binding.lvEquipment.setAdapter(CultivationEquipmentAdapter(requireContext(), mEquipmentGroups, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {

            override fun onOpenDetailList(equipment: Equipment) {
                openRankDialog(equipment.id)
            }

            override fun onDeleteHandler(equipment: Equipment, group:Boolean) {
                if (equipment.detail.type == 7){
                    mPerson.tipsList.removeIf {
                        it.detail.id == equipment.detail.id
                    }
                }else{
                    mPerson.equipmentList.removeIf {
                        if(group){
                            it.id == equipment.id
                        }else{
                            it.id == equipment.id && it.amuletSerialNo == equipment.amuletSerialNo
                        }
                    }
                    CultivationHelper.updatePersonEquipment(mPerson)
                }
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