package com.mx.gillustrated.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.databinding.FragmentDialogThemeBinding

class FragmentDialogTheme : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogTheme {
            return FragmentDialogTheme()
        }
    }

    private var mOldThemeIndex = 0
    private var _binding: FragmentDialogThemeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View{
        _binding = FragmentDialogThemeBinding.inflate(inflater, container, false)
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
        val context = activity as MainActivity
        var selectIndex = 0
        when (context.mSP.getString("theme", "Green")){
            "Green"-> selectIndex = 0
            "Blue"-> selectIndex = 1
            "Orange"-> selectIndex = 2
        }
        binding.spinnerTheme.setSelection(selectIndex)
        mOldThemeIndex = selectIndex
        binding.btnSave.setOnClickListener {
            val currentSelectIndex = binding.spinnerTheme.selectedItemPosition
            if(mOldThemeIndex == currentSelectIndex){
                Toast.makeText(context, "没有变化", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (currentSelectIndex){
                0-> context.mSP.edit().putString("theme", "Green").apply()
                1-> context.mSP.edit().putString("theme", "Blue").apply()
                2-> context.mSP.edit().putString("theme", "Orange").apply()
            }
            this.dismiss()
            context.recreate()
        }
    }
}