package com.mx.gillustrated.fragment

import android.os.Build
import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationTeJiAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.dialog.FragmentDialogEquipment
import com.mx.gillustrated.dialog.FragmentDialogTeJi
import com.mx.gillustrated.vo.cultivation.Equipment
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.TeJi

@RequiresApi(Build.VERSION_CODES.N)
class FragmentTeJi: Fragment() {

    private val mConfigTeji = CultivationHelper.mConfig.teji

    @BindView(R.id.lv_teji)
    lateinit var mListView: ListView


    @OnClick(R.id.btn_add_teji)
    fun onAddClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogTeJi.
                newInstance( object : FragmentDialogTeJi.TeJiSelectorCallback{
                    override fun onItemSelected(teJi: TeJi) {
                        updateTeji(teJi)
                    }
                }, 1)
        newFragment.isCancelable = true
        newFragment.show(ft, "dialog_teji")
    }

    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    private val mTeJi: MutableList<TeJi> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vp_teji, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
        init()
    }

    fun init(){
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        mListView.adapter = CultivationTeJiAdapter(this.context!!, mTeJi, object : CultivationTeJiAdapter.TeJiAdapterCallback {
            override fun onDeleteHandler(teji: TeJi) {
                mPerson.teji.removeIf {
                    it == teji.id && teji.form == 0
                }
                updateList()
            }
        })
        updateList()
    }

    fun updateList(){
        val tejis = mPerson.teji.map {
            mConfigTeji.find { e-> e.id == it}!!.toTeji()
        }.toMutableList()
        CultivationHelper.mConfig.equipment.filter {
            it.type == 8 && it.spec.contains(mPerson.specIdentity) && it.teji.size > 0}
        .forEach {
            tejis.addAll(it.teji.map {
                val teji = mConfigTeji.find { e-> e.id == it}!!.toTeji()
                teji.form = 1
                teji
            })
        }
         mPerson.equipmentList.filter { it.detail.teji.size > 0 }
         .forEach {
            tejis.addAll(it.detail.teji.map {
                val teji = mConfigTeji.find { e-> e.id == it}!!.toTeji()
                teji.form = 2
                teji
            })
        }
        tejis.addAll(mConfig.teji.filter { it.type == 6 && it.spec.contains(mPerson.specIdentity)}.map {
            val teji = it.toTeji()
            teji.form = 3
            if(teji.specName.isNotEmpty()){
                val index = teji.spec.indexOf(mPerson.specIdentity)
                if(index < teji.specName.size) {
                    teji.name = teji.specName[index]
                }
            }
            teji
        })
        mPerson.label.forEach {
            val label = mConfig.label.find { f-> f.id == it }!!.copy()
            label.teji.forEach { id->
                val teji = mConfig.teji.find { f-> f.id == id }!!.toTeji()
                teji.form = 4
                tejis.add(teji)
            }
        }
        tejis.sortByDescending { it.rarity }
        mTeJi.clear()
        mTeJi.addAll(tejis)
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
    }

    fun updateTeji(teJi: TeJi){
        if( mPerson.teji.find { it == teJi.id } != null)
            return
        mPerson.teji.add(teJi.id)
        updateList()
    }


}