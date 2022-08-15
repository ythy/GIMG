package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person


@RequiresApi(Build.VERSION_CODES.N)
class FragmentDialogAddPerson: DialogFragment()  {

    companion object{
        fun newInstance(): FragmentDialogAddPerson {
            return FragmentDialogAddPerson()
        }
    }

    @BindView(R.id.et_name_first)
    lateinit var etNameFirst:EditText

    @BindView(R.id.et_name_last)
    lateinit var etNameLast:EditText

    @BindView(R.id.rb_male)
    lateinit var rbMale:RadioButton

    @BindView(R.id.et_linggen)
    lateinit var etLingGen:EditText

    @BindView(R.id.et_tianfu)
    lateinit var etTianFu:EditText

    @BindView(R.id.et_mum)
    lateinit var etMum:EditText


    @OnClick(R.id.btn_save)
    fun onSaveClick(){
        val mum = mContext.mPersons.map { it.value }.find { it.name == etMum.text.toString() || PinyinUtil.convert(it.name) == etMum.text.toString()  }
        var parent:Pair<Person, Person>? = null
        if(mum?.partner != null){
            parent = Pair(mContext.mPersons[mum.partner!!]!!, mum) //这里可能有强转导致的并发问题
        }
        val person = CultivationHelper.getPersonInfo(Pair(etNameFirst.text.toString().trim(), etNameLast.text.toString().trim()),
                if(rbMale.isChecked) NameUtil.Gender.Male else NameUtil.Gender.Female  , 100, parent, false,
                CultivationHelper.PersonFixedInfoMix(null, null, etTianFu.text.toString().toInt(), etLingGen.text.toString().toInt()))
        if(parent != null){
            synchronized(parent.first.children){
                parent.first.children.add(person.id)
            }
            synchronized(parent.second.children){
                parent.second.children.add(person.id)
            }
        }
        mContext.combinedPersonRelationship(person)
        this.dismiss()
    }


    lateinit var  mContext:CultivationActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_add_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
    }

}