package com.mx.gillustrated.dialog

import com.mx.gillustrated.vo.cultivation.TeJi
import android.os.Build
import android.os.Bundle
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

@RequiresApi(Build.VERSION_CODES.N)
class FragmentDialogTeJi constructor(val callback:TeJiSelectorCallback, val mType:Int): DialogFragment()  {

    companion object{
        fun newInstance(callback:TeJiSelectorCallback, type:Int): FragmentDialogTeJi {
            return FragmentDialogTeJi(callback, type)
        }
    }

    @BindView(R.id.spinnerTeji)
    lateinit var mSpinner: Spinner

    @OnClick(R.id.btnSave)
    fun onSaveClick(){
        callback.onItemSelected(mCurrentSelected)
        this.dismiss()
    }

    lateinit var mCurrentSelected: TeJi

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_teji, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        init()
    }

    fun init(){
        val list = CultivationHelper.mConfig.teji.filter { it.type == mType }.sortedBy { it.rarity }
        mCurrentSelected = list[0]
        val adapter = ArrayAdapter<TeJi>(context!!,
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = adapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val teji = parent.selectedItem as TeJi
                mCurrentSelected = teji
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    interface TeJiSelectorCallback{
        fun onItemSelected(teji: TeJi)
    }
}