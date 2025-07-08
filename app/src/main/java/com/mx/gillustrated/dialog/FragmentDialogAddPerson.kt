package com.mx.gillustrated.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.GameBaseActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogAddPersonBinding
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person


class FragmentDialogAddPerson: DialogFragment()  {

    companion object{
        fun newInstance(): FragmentDialogAddPerson {
            return FragmentDialogAddPerson()
        }
    }

    private var _binding: FragmentDialogAddPersonBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogAddPersonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mContext = activity as GameBaseActivity
        binding.btnSave.setOnClickListener {
            val mum = mContext.mPersons.map { it.value }.find {
                it.name == binding.etMum.text.toString() || PinyinUtil.convert(it.name) == binding.etMum.text.toString()  }
            var parent:Pair<Person, Person>? = null
            if(mum?.partner != null){
                parent = Pair(mContext.mPersons[mum.partner!!]!!, mum) //这里可能有强转导致的并发问题
            }
            val person = CultivationHelper.getPersonInfo(Pair(binding.etNameFirst.text.toString().trim(), binding.etNameLast.text.toString().trim()),
                    if(binding.rbMale.isChecked) NameUtil.Gender.Male else NameUtil.Gender.Female, parent,
                    CultivationSetting.PersonFixedInfoMix(null, null, binding.etTianfu.text.toString().toInt(), binding.etLinggen.text.toString().toInt()))
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}