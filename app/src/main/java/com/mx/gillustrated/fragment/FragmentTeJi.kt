package com.mx.gillustrated.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationTeJiAdapter
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.databinding.FragmentVpTejiBinding
import com.mx.gillustrated.dialog.FragmentDialogTeJi
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.TeJi


class FragmentTeJi: Fragment() {

    private val mConfigTeji = mConfig.teji
    private var _binding: FragmentVpTejiBinding? = null
    private val binding get() = _binding!!
    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    private val mTeJi: MutableList<TeJi> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVpTejiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        val id = requireArguments().getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        binding.lvTeji.adapter = CultivationTeJiAdapter(requireContext(), mTeJi, object : CultivationTeJiAdapter.TeJiAdapterCallback {
            override fun onDeleteHandler(item: TeJi) {
                mPerson.teji.removeIf {
                    it == item.id && item.form == 0
                }
                updateList()
            }
        })
        binding.btnAddTeji.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogTeJi.
            newInstance( object : FragmentDialogTeJi.TeJiSelectorCallback{
                override fun onItemSelected(item: TeJi) {
                    updateTeji(item)
                }
            }, 1)
            newFragment.isCancelable = true
            newFragment.show(ft, "dialog_teji")
        }
        updateList()
    }

    fun updateList(){
        val tejis = mPerson.teji.map {
            mConfigTeji.find { e-> e.id == it}!!.toTeji()
        }.toMutableList()
         mPerson.equipmentList.filter { it.detail.teji.size > 0 }
         .forEach { equipment ->
            tejis.addAll(equipment.detail.teji.map { tejiString ->
                val teji = mConfigTeji.find { e-> e.id == tejiString}!!.toTeji()
                teji.form = if (equipment.detail.type == 8) 1 else 2
                teji
            })
        }
        mPerson.equipmentList.filter { it.detail.specTeji.size > 0 }
                .forEach { equipment ->
                    val index = equipment.detail.spec.indexOf(mPerson.specIdentity)
                    val teji = mConfigTeji.find { e-> e.id == equipment.detail.specTeji[index]}!!.toTeji()
                    teji.form = 3
                    teji.name = equipment.detail.specTejiName[index]
                    tejis.add(teji)
                }

        mPerson.label.forEach {
            val label = mConfig.label.find { f-> f.id == it }!!.copy()
            label.teji.forEach { id->
                val teji = mConfig.teji.find { f-> f.id == id }!!.toTeji()
                teji.form = 4
                tejis.add(teji)
            }
        }
        mPerson.tipsList.forEach {
            it.detail.teji.forEach { id->
                val teji = mConfig.teji.find { f-> f.id == id }!!.toTeji()
                teji.form = 5
                tejis.add(teji)
            }
        }

        tejis.sortByDescending { it.rarity }
        mTeJi.clear()
        mTeJi.addAll(tejis)
        (binding.lvTeji.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvTeji.invalidateViews()
    }

    fun updateTeji(teJi: TeJi){
        if( mPerson.teji.find { it == teJi.id } != null)
            return
        mPerson.teji.add(teJi.id)
        updateList()
    }


}