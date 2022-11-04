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
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.SimpleData

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogRank constructor(private val mType:Int, private val mId:String)  : DialogFragment() {

    companion object{

        fun newInstance(type:Int, id:String = ""): FragmentDialogRank {
            return FragmentDialogRank(type, id)
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
            in 3..9 ->{

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
                repeat(CultivationHelper.mBattleRound.bang){ r->
                    val round = r + 1
                    val alliance = mContext.mAlliance.map { it.value }.find { it.battleRecord[round] == 1 }
                    if (alliance != null){
                        list.add(SimpleData(alliance.id, alliance.name, "", mType, mutableListOf(), round))
                    }
                }
            }
            1 ->{
                title = "Clan"
                repeat(CultivationHelper.mBattleRound.clan){ r->
                    val round = r + 1
                    val clan = mContext.mClans.map { it.value }.find { it.battleRecord[round] == 1 }
                    if (clan != null){
                        list.add(SimpleData(clan.id, clan.name, "", mType, mutableListOf(), round))
                    }
                }
            }
            2 ->{
                title = "Single"
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentListPair.filter { e-> e.first == "7006301" }.map {
                        SimpleData(p.value.id, p.value.name, "", mType, mutableListOf(), it.second)
                    })
                }
            }
            3 ->{
                title = "Bang Ranking"
                val alliance = mContext.mAlliance[mId]!!
                repeat(CultivationHelper.mBattleRound.bang){
                    val round = it + 1
                    if (alliance.battleRecord[round] != null){
                        list.add(SimpleData(it.toString(), alliance.battleRecord[round].toString(), "", mType, mutableListOf(), round))
                    }
                }
            }
            4 ->{
                title = "Clan Ranking"
                val clan = mContext.mClans[mId]!!
                repeat(CultivationHelper.mBattleRound.clan){
                    val round = it + 1
                    if (clan.battleRecord[round] != null){
                        list.add(SimpleData(it.toString(), clan.battleRecord[round].toString(), "", mType, mutableListOf(), round))
                    }
                }
            }
            5 ->{
                title = "Nation Ranking"
                val nation = mContext.mNations[mId]!!
                repeat(CultivationHelper.mBattleRound.nation){
                    val round = it + 1
                    if (nation.battleRecord[round] != null){
                        list.add(SimpleData(it.toString(), nation.battleRecord[round].toString(), "", mType, mutableListOf(), round))
                    }
                }
            }
            in 10..19 ->{
                val index = mType - 10
                val equipment = CultivationHelper.mConfig.equipment.filter { it.type == 14 }[index]
                title = CultivationHelper.showing(equipment.name)
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentListPair.filter { e-> e.first == equipment.id }.map {
                        SimpleData(p.value.id, p.value.name, "", mType, mutableListOf(), it.second)
                    })
                }
            }
            in 20..29 ->{
                val index = mType - 20
                val equipment = CultivationHelper.mConfig.equipment.filter { it.type == 15 }[index]
                title = CultivationHelper.showing(equipment.name)
                mContext.mPersons.forEach{ p->
                    list.addAll(p.value.equipmentListPair.filter { e-> e.first == equipment.id }.map {
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