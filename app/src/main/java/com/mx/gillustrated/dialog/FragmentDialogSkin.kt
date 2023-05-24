package com.mx.gillustrated.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.databinding.FragmentDialogSkinBinding

class FragmentDialogSkin : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogSkin {
            return FragmentDialogSkin()
        }
    }

    private var mOldSkinIndex = 0
    private var _binding: FragmentDialogSkinBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding =  FragmentDialogSkinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        val context = activity as CultivationActivity
        var selectIndex = 0
        when (context.mSP.getString("cultivation_skin", "spring")){
            "spring"-> selectIndex = 0
            "rain"-> selectIndex = 1
            "equinox"-> selectIndex = 2
            "grain_rain" -> selectIndex = 3
            "grain_rain2" -> selectIndex = 4
            "grain_rain3" -> selectIndex = 5
            "summer_begin" -> selectIndex = 6
        }
        binding.spinnerSkin.setSelection(selectIndex)
        mOldSkinIndex = selectIndex
        binding.btnSave.setOnClickListener {
            val currentSelectIndex = binding.spinnerSkin.selectedItemPosition
            if(mOldSkinIndex == currentSelectIndex){
                Toast.makeText(context, "没有变化", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (currentSelectIndex){
                0-> context.mSP.edit().putString("cultivation_skin", "spring").apply()
                1-> context.mSP.edit().putString("cultivation_skin", "rain").apply()
                2-> context.mSP.edit().putString("cultivation_skin", "equinox").apply()
                3-> context.mSP.edit().putString("cultivation_skin", "grain_rain").apply()
                4-> context.mSP.edit().putString("cultivation_skin", "grain_rain2").apply()
                5-> context.mSP.edit().putString("cultivation_skin", "grain_rain3").apply()
                6-> context.mSP.edit().putString("cultivation_skin", "summer_begin").apply()
            }
            context.loadSkin()
            this.dismiss()
        }

    }
}