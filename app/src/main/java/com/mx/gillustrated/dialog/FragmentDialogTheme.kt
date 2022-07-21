package com.mx.gillustrated.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.MainActivity

class FragmentDialogTheme : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogTheme {
            return FragmentDialogTheme()
        }
    }

    var mOldThemeIndex = 0

    @BindView(R.id.spinnerTheme)
    lateinit var mSpinner:Spinner

    @OnClick(R.id.btnSave)
    fun onSaveClick(){
        val context = activity as MainActivity
        val currentSelectIndex = mSpinner.selectedItemPosition
        if(mOldThemeIndex == currentSelectIndex){
            Toast.makeText(context, "没有变化", Toast.LENGTH_SHORT).show()
            return
        }
        when (currentSelectIndex){
            0-> context.mSP.edit().putString("theme", "Green").apply()
            1-> context.mSP.edit().putString("theme", "Blue").apply()
            2-> context.mSP.edit().putString("theme", "Orange").apply()
        }
        this.dismiss()
        context.recreate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val v = inflater.inflate(R.layout.fragment_dialog_theme, container, false)
        ButterKnife.bind(this, v)
        init()
        return v
    }

    fun init(){
        val context = activity as MainActivity
        var selectIndex = 0
        when (context.mSP.getString("theme", "Green")){
            "Green"-> selectIndex = 0
            "Blue"-> selectIndex = 1
            "Orange"-> selectIndex = 2
        }
        mSpinner.setSelection(selectIndex)
        mOldThemeIndex = selectIndex
    }
}