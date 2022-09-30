package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationRankAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.SimpleData

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogRank constructor(private val mType:Int)  : DialogFragment() {

    companion object{

        fun newInstance(type:Int): FragmentDialogRank {
            return FragmentDialogRank(type)
        }

    }

    @BindView(R.id.lv_person)
    lateinit var mListView: ListView

    @BindView(R.id.tv_total)
    lateinit var mTotalText: TextView

    @BindView(R.id.tv_title)
    lateinit var mTitle: TextView


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        this.dismiss()
    }

    @OnItemClick(R.id.lv_person)
    fun onItemClick(position:Int){

        val ft = mContext.supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("id", mListData[position].id)

        when (mType){
            0-> {
                val newFragment = FragmentDialogAlliance.newInstance()
                newFragment.isCancelable = false
                newFragment.arguments = bundle
                newFragment.show(ft, "dialog_alliance_info")
            }
            1-> {
                val newFragment = FragmentDialogClan.newInstance()
                newFragment.isCancelable = false
                newFragment.arguments = bundle
                newFragment.show(ft, "dialog_clan_info")
            }
            else ->{
                val newFragment = FragmentDialogPerson.newInstance()
                newFragment.isCancelable = false
                newFragment.arguments = bundle
                newFragment.show(ft, "dialog_person_info")
            }
        }
    }

    lateinit var mContext: CultivationActivity
    private var mListData: MutableList<SimpleData> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_rank_list, container, false)
        mContext = activity as CultivationActivity
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListView.adapter = CultivationRankAdapter(this.context!!, mListData)
        updateView()
    }

    private fun updateView(){
        val list:MutableList<SimpleData> = mutableListOf()
        var title = ""
        when(mType){
            0 ->{
                title = "Bang"
                val banglist = mutableListOf<SimpleData>()
                mContext.mPersons.forEach{ p->
                    banglist.addAll(p.value.equipmentList.filter { e-> e.first == "7006101" }.map {
                        SimpleData(p.value.allianceId, p.value.allianceName, "", mType, mutableListOf(), it.second)
                    })
                }
                list.addAll(banglist.distinctBy { it.seq })
            }
            1 ->{
                title = "Clan"
                val clanlist = mutableListOf<SimpleData>()
                mContext.mPersons.forEach{ p->
                    clanlist.addAll(p.value.equipmentList.filter { e-> e.first == "7006201" }.mapNotNull {
                        val clan = mContext.mClans[p.value.ancestorId]
                        if(clan == null)
                            null
                        else
                            SimpleData(clan.id, clan.name, "", mType, mutableListOf(), it.second)
                    })
                }
                list.addAll(clanlist.distinctBy { it.seq })
            }
            2 ->{
                title = "Single"
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentList.filter { e-> e.first == "7006301" }.map {
                        SimpleData(p.value.id, p.value.name, "", mType, mutableListOf(), it.second)
                    })
                }
            }
            in 10..19 ->{
                val index = mType - 10
                val equipment = CultivationHelper.mConfig.equipment.filter { it.type == 14 }[index]
                title = CultivationHelper.showing(equipment.name)
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentList.filter { e-> e.first == equipment.id }.map {
                        SimpleData(p.value.id, p.value.name, "", mType, mutableListOf(), it.second)
                    })
                }
            }
            in 20..29 ->{
                val index = mType - 20
                val equipment = CultivationHelper.mConfig.equipment.filter { it.type == 15 }[index]
                title = CultivationHelper.showing(equipment.name)
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentList.filter { e-> e.first == equipment.id }.map {
                        SimpleData(p.value.id, p.value.name, "", mType, mutableListOf(), it.second)
                    })
                }
            }
        }
        mTitle.text = title
        updateList(list)
    }

    private fun updateList(list:MutableList<SimpleData>){
        mListData.clear()
        mListData.addAll(list.sortedByDescending { it.seq })
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
        mTotalText.text = list.size.toString()
    }
}