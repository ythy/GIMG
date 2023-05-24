package com.mx.gillustrated.dialog

import com.mx.gillustrated.vo.cultivation.TeJi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.FragmentDialogTejiBinding
import com.mx.gillustrated.vo.cultivation.TeJiConfig


class FragmentDialogTeJi constructor(private val callback:TeJiSelectorCallback, private val mType: Int = 0): DialogFragment()  {

    companion object{
        fun newInstance(callback:TeJiSelectorCallback, type:Int): FragmentDialogTeJi {
            return FragmentDialogTeJi(callback, type)
        }
    }

    lateinit var mCurrentSelected: TeJi
    private var _binding: FragmentDialogTejiBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogTejiBinding.inflate(inflater, container, false)
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
        val predicate = when (mType) {
            1 -> fun (teji:TeJiConfig):Boolean { return teji.type < 4 }
            else -> fun (teji:TeJiConfig):Boolean { return teji.type < 4 }
        }
        val list =  CultivationHelper.mConfig.teji.filter { predicate(it) }.sortedBy { it.rarity }
        mCurrentSelected = list[0].toTeji()
        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTeji.adapter = adapter
        binding.spinnerTeji.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val teji = parent.selectedItem as TeJiConfig
                mCurrentSelected = teji.toTeji()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.btnSave.setOnClickListener {
            callback.onItemSelected(mCurrentSelected)
            this.dismiss()
        }

    }

    interface TeJiSelectorCallback{
        fun onItemSelected(teji: TeJi)
    }
}