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


    @OnClick(R.id.btn_save)
    fun onSaveClick(){
        val person = CultivationHelper.getPersonInfo(Pair(etNameFirst.text.toString(), etNameLast.text.toString()),
                if(rbMale.isChecked) NameUtil.Gender.Male else NameUtil.Gender.Female  , 100, null, false,
                CultivationHelper.PersonFixedInfoMix(null, null, etTianFu.text.toString().toInt(), etLingGen.text.toString().toInt()))
        mContext.mPersons[person.id] = person
        CultivationHelper.joinAlliance(person, mContext.mAlliance)
        CultivationHelper.addPersonEvent(person, "${mContext.getYearString()} ${CultivationHelper.getPersonBasicString(person, false)} 加入")
        CultivationHelper.writeHistory("${CultivationHelper.getPersonBasicString(person)} 加入", person)
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