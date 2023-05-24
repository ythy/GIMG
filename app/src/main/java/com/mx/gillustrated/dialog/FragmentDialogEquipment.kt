package com.mx.gillustrated.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.FragmentDialogEquipmentBinding
import com.mx.gillustrated.vo.cultivation.EquipmentConfig


class FragmentDialogEquipment constructor(private val callback:EquipmentSelectorCallback, private val mType:MutableList<Int>): DialogFragment()  {

    companion object{
        fun newInstance(callback:EquipmentSelectorCallback, type:MutableList<Int>): FragmentDialogEquipment {
            return FragmentDialogEquipment(callback, type)
        }
    }


    private var _binding: FragmentDialogEquipmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
   lateinit var mCurrentSelected: EquipmentConfig

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        val list = CultivationHelper.mConfig.equipment.filter { mType.contains(it.type) }.toMutableList()
        list.sortWith(compareBy<EquipmentConfig> {it.type}.thenBy { it.teji.size }.thenBy { it.rarity })
        mCurrentSelected = list[0]
        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEquipment.adapter = adapter
        binding.spinnerEquipment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val equipment = parent.selectedItem as EquipmentConfig
                mCurrentSelected = equipment
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.btnSave.setOnClickListener {
            callback.onItemSelected(mCurrentSelected)
            this.dismiss()
        }
    }

    interface EquipmentSelectorCallback{
        fun onItemSelected(equipment: EquipmentConfig)
    }
}