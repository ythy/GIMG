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
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.activity.MainActivity

class FragmentDialogSkin : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogSkin {
            return FragmentDialogSkin()
        }
    }

    var mOldSkinIndex = 0

    @BindView(R.id.spinnerSkin)
    lateinit var mSpinner:Spinner

    @OnClick(R.id.btnSave)
    fun onSaveClick(){
        val context = activity as CultivationActivity
        val currentSelectIndex = mSpinner.selectedItemPosition
        if(mOldSkinIndex == currentSelectIndex){
            Toast.makeText(context, "没有变化", Toast.LENGTH_SHORT).show()
            return
        }
        when (currentSelectIndex){
            0-> context.mSP.edit().putString("cultivation_skin", "spring").apply()
            1-> context.mSP.edit().putString("cultivation_skin", "equinox").apply()
        }
        this.dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val v = inflater.inflate(R.layout.fragment_dialog_skin, container, false)
        ButterKnife.bind(this, v)
        init()
        return v
    }

    fun init(){
        val context = activity as CultivationActivity
        var selectIndex = 0
        when (context.mSP.getString("cultivation_skin", "spring")){
            "spring"-> selectIndex = 0
            "equinox"-> selectIndex = 1
        }
        mSpinner.setSelection(selectIndex)
        mOldSkinIndex = selectIndex
    }
}