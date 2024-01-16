package com.mx.gillustrated.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEventAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.databinding.FragmentVpEventBinding
import com.mx.gillustrated.dialog.FragmentDialogEmperor
import com.mx.gillustrated.dialog.FragmentDialogPerson
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent

@SuppressLint("SetTextI18n")
class FragmentPersonEvent(private val mCallback: FragmentDialogPerson.IViewpageCallback) : Fragment(){

    lateinit var mContext:CultivationActivity
    private var _binding: FragmentVpEventBinding? = null
    private val binding get() = _binding!!
    private var mEventData = mutableListOf<PersonEvent>()
    private var mEventDataString = mutableListOf<String>()
    lateinit var mPerson:Person

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVpEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        init()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initListener(){
        binding.tvSeq.setOnClickListener {
            if(mPerson.nationId == "6200006" || mPerson.profile in 1701..1799){
                val ft = mContext.supportFragmentManager.beginTransaction()
                val newFragment = FragmentDialogEmperor.newInstance(mPerson.id)
                newFragment.isCancelable = false
                newFragment.show(ft, "dialog_emperor")
            }
        }
        binding.schDel.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
                specRender()
            else
                normalRender()
        }
        binding.schSex.setOnCheckedChangeListener { _, isChecked ->
            mCallback.update(4, if(isChecked) "Y" else "N")
        }
    }

    fun init(){
        val id = requireArguments().getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        if(mPerson.specIdentity > 0)
            binding.tvSeq.text = mPerson.specIdentity.toString()
        if(mPerson.profile in 1701..1799 && mPerson.gender == NameUtil.Gender.Female){
            binding.schSex.visibility = View.VISIBLE
        }else{
            binding.schSex.visibility = View.GONE
        }
        normalRender()
    }

    private fun normalRender(){
        mEventDataString = mPerson.events.toMutableList().sortedByDescending { it.happenTime }.map {
                CultivationHelper.showing("${CultivationHelper.getYearString(it.happenTime)} ${it.content}")
        }.toMutableList()
        binding.lvEvents.adapter = ArrayAdapter(requireContext(), R.layout.list_simple_item_text1,
                android.R.id.text1, mEventDataString)
    }

    private fun specRender(){
        mEventData = mPerson.events.toMutableList().sortedByDescending { it.happenTime }.toMutableList()
        binding.lvEvents.adapter = CultivationEventAdapter(requireContext(), mEventData, object : CultivationEventAdapter.EventAdapterCallback{
            override fun onDeleteHandler(event: PersonEvent) {
                mPerson.events.remove(event)
                mEventData.remove(event)
                (binding.lvEvents.adapter as CultivationEventAdapter).notifyDataSetChanged()
                binding.lvEvents.invalidateViews()
            }
        })
    }

    fun updateEvent(){

    }

}