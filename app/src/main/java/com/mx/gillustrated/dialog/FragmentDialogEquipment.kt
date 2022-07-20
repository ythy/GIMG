package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.Equipment

@RequiresApi(Build.VERSION_CODES.N)
class FragmentDialogEquipment constructor(val callback:EquipmentSelectorCallback, val mType:Range<Int>): DialogFragment()  {

    companion object{
        fun newInstance(callback:EquipmentSelectorCallback, type:Range<Int>): FragmentDialogEquipment {
            return FragmentDialogEquipment(callback, type)
        }
    }

    @BindView(R.id.spinnerEquipment)
    lateinit var mSpinner: Spinner

    @OnClick(R.id.btnSave)
    fun onSaveClick(){
        callback.onItemSelected(mCurrentSelected)
        this.dismiss()
    }

   lateinit var mCurrentSelected: Equipment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_equipment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        init()
    }

    fun init(){
        val list = CultivationHelper.mConfig.equipment.filter { mType.contains(it.type) }.toMutableList()
        list.sortWith(compareBy<Equipment> {it.type}.thenBy { it.rarity })
        mCurrentSelected = list[0]
        val adapter = ArrayAdapter<Equipment>(context!!,
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = adapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val equipment = parent.selectedItem as Equipment
                mCurrentSelected = equipment
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    interface EquipmentSelectorCallback{
        fun onItemSelected(equipment: Equipment)
    }
}