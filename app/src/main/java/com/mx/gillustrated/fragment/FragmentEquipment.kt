package com.mx.gillustrated.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
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
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
class FragmentEquipment: Fragment() {

    private val mConfigEquipments = CultivationHelper.mConfig.equipment

    @BindView(R.id.lv_equipment)
    lateinit var mListView: ListView


    @OnClick(R.id.btn_add_equipment)
    fun onAddClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogEquipment.
                newInstance( object : FragmentDialogEquipment.EquipmentSelectorCallback{
                    override fun onItemSelected(equipment: Equipment) {
                        mPerson.equipment.add("${equipment.id},${equipment.name}")
                        CultivationHelper.updatePersonEquipment(mPerson)
                        updateList()
                    }
                })
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_equipment")
    }

    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    val mEquipments: MutableList<Equipment> = mutableListOf()

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
        mPerson = mContext.getOnlinePersonDetail(id) ?: mContext.getOfflinePersonDetail(id)!!
        mListView.adapter = CultivationEquipmentAdapter(this.context!!, mEquipments, object : CultivationEquipmentAdapter.EquipmentAdapterCallback {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDeleteHandler(uniqueName: String) {
                mPerson.equipment.removeIf { it.split(",")[1] == uniqueName }
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
            result.uniqueName = it.split(",")[1]
            result.type = equipment.type
            result.rarity = equipment.rarity
            result.xiuwei = equipment.xiuwei
            result.success = equipment.success
            result.property = equipment.property
            result
        }
        mEquipments.clear()
        mEquipments.addAll(equipments.sortedBy { it.type })
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
    }




}