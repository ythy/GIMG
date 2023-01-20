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
import com.mx.gillustrated.component.CultivationEnemyHelper
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.SimpleData

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogRank constructor(private val mType:Int, private val mId:String, private val mSpec:String)  : DialogFragment() {

    companion object{

        fun newInstance(type:Int, id:String = "", spec:String = ""): FragmentDialogRank {
            return FragmentDialogRank(type, id, spec)
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
            in 3..7 ->{

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
                repeat(CultivationHelper.mBattleRound.single){ r->
                    val round = r + 1
                    val person = mContext.mPersons.map { it.value }.find { it.battleRecord[round] == 1 }
                    if (person != null){
                        list.add(SimpleData(person.id, person.name, "", mType, mutableListOf(), round))
                    }
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
            6 ->{
                title = "Single Ranking"
                val person = mContext.mPersons[mId]!!
                repeat(CultivationHelper.mBattleRound.single){
                    val round = it + 1
                    if (person.battleRecord[round] != null){
                        list.add(SimpleData(it.toString(), person.battleRecord[round].toString(), "", mType, mutableListOf(), round))
                    }
                }
            }
            7 ->{
                val equipment = CultivationHelper.mBossRecord[mSpec.toInt()].filter {
                    it.value == mId
                }.map { it.key }
                title = CultivationEnemyHelper.bossSettings[mSpec.toInt()].name
                list.addAll(equipment.map{ index ->
                    SimpleData("", "", "", mType, mutableListOf(), index)
                })
            }
            8 ->{ // seq = name
                title = "Exclusives"
                val exclusives =  CultivationHelper.mConfig.equipment.filter { it.type == 8 }.sortedByDescending { it.id }
                list.addAll(
                    exclusives.map{ equipment ->
                        val persons = mContext.mPersons.map { it.value }.filter { equipment.spec.contains(it.specIdentity) }
                        SimpleData(if (persons.size == 1) persons[0].id else "",
                                if (persons.size == 1) persons[0].name else  persons.joinToString { "${it.name}(${equipment.specName[equipment.spec.indexOf(it.specIdentity)]})" },
                                equipment.name, mType, mutableListOf(), -1)
                    }
                )
            }
            in 20..29 ->{
                val index = mType - 20
                val record = CultivationHelper.mBossRecord[index]
                title = CultivationHelper.showing(CultivationEnemyHelper.bossSettings[index].name)
                list.addAll(record.map { r->
                    SimpleData(r.value,  mContext.mPersons[r.value]?.name ?: "", "", mType, mutableListOf(), r.key)
                })

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