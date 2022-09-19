package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.TextViewBox

@RequiresApi(Build.VERSION_CODES.N)
class FragmentDialogSetting : DialogFragment() {

    companion object {
        fun newInstance(): FragmentDialogSetting {
            return FragmentDialogSetting()
        }
    }

    @BindView(R.id.ll_parent_measure)
    lateinit var measures: LinearLayout

    @BindView(R.id.tvb_battle)
    lateinit var tvbBattle: TextViewBox

    @BindView(R.id.tvb_enemy)
    lateinit var tvbEnemy: TextViewBox

    @BindView(R.id.tvb_boss)
    lateinit var tvbBoss: TextViewBox

    @BindView(R.id.et_jie)
    lateinit var mEtJie:EditText

    @BindView(R.id.et_enemy_xun)
    lateinit var mEnemyXun:EditText

    @BindView(R.id.et_enemy_weight)
    lateinit var mEnemyWeight:EditText

    @BindView(R.id.et_boss_xun)
    lateinit var mBossXun:EditText

    @BindView(R.id.et_boss_weight)
    lateinit var mBossWeight:EditText

    @BindView(R.id.et_single_xun)
    lateinit var mSingleXun:EditText

    @BindView(R.id.et_single_weight)
    lateinit var mSingleWeight:EditText

    @BindView(R.id.et_bang_xun)
    lateinit var mAllianceXun:EditText

    @BindView(R.id.et_bang_weight)
    lateinit var mAllianceWeight:EditText

    @BindView(R.id.et_clan_xun)
    lateinit var mClanXun:EditText

    @BindView(R.id.et_clan_weight)
    lateinit var mClanWeight:EditText

    @OnTextChanged(R.id.et_jie)
    fun onJieTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current.toIntOrNull() != null && current.toInt() > 0){
            mActivity.mSP.edit().putInt("cultivation_jie", current.toInt()).apply()
        }
    }

    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
       mActivity.mSP.edit().putString("cultivation_random_event", listOf(
               "${mEnemyXun.text}-${mEnemyWeight.text}",
               "${mBossXun.text}-${mBossWeight.text}",
               "${mSingleXun.text}-${mSingleWeight.text}",
               "${mAllianceXun.text}-${mAllianceWeight.text}",
               "${mClanXun.text}-${mClanWeight.text}"
       ).joinToString()).apply()
       mActivity.showToast("保存成功!")
    }

    @OnClick(R.id.btn_reset)
    fun onResetClickHandler(){
        mActivity.mSP.edit().putString("cultivation_random_event", CultivationHelper.SP_EVENT_WEIGHT.joinToString()).apply()
        mActivity.showToast("重置成功!")
    }

    @OnClick(R.id.btn_close)
    fun onCloseClickHandler(){
        this.dismiss()
    }

    @OnClick(R.id.btn_fav)
    fun onFavClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(1)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_spec)
    fun onSpecClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(2)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    lateinit var mActivity: CultivationActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mActivity = activity as CultivationActivity
        init()
    }

    fun init() {
        measures.measure(0,0)
        val battleRound = CultivationHelper.mBattleRound
        tvbBattle.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - 20, 20))
        tvbBattle.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(index)
            }
        })
        tvbBattle.setDataProvider(listOf("帮:${battleRound.bang}", "族:${battleRound.clan}", "个:${battleRound.single}"), null)

        tvbEnemy.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - 20, 20))
        tvbEnemy.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(10 + index)
            }
        })
        tvbEnemy.setDataProvider(CultivationHelper.EnemyNames.mapIndexed { index, s ->  "$s:${battleRound.enemy[index]}" }, null)

        tvbBoss.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - 20, 20))
        tvbBoss.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(20 + index)
            }
        })
        tvbBoss.setDataProvider(listOf("霸:${battleRound.boss[0]}", "暗:${battleRound.boss[1]}", "滚:${battleRound.boss[2]}", "王:${battleRound.boss[3]}"), null)

        mEtJie.setText(mActivity.mSP.getInt("cultivation_jie", CultivationHelper.SP_JIE_TURN).toString())
        val eventWeight = mActivity.mSP.getString("cultivation_random_event", CultivationHelper.SP_EVENT_WEIGHT.joinToString())!!.split(",").map {
            val temp = it.split("-")
            Pair(temp.first().trim(), temp.last().trim())
        }
        mEnemyXun.setText(eventWeight[0].first)
        mEnemyWeight.setText(eventWeight[0].second)
        mBossXun.setText(eventWeight[1].first)
        mBossWeight.setText(eventWeight[1].second)
        mSingleXun.setText(eventWeight[2].first)
        mSingleWeight.setText(eventWeight[2].second)
        mAllianceXun.setText(eventWeight[3].first)
        mAllianceWeight.setText(eventWeight[3].second)
        mClanXun.setText(eventWeight[4].first)
        mClanWeight.setText(eventWeight[4].second)
    }

    private fun openDialog(tag:Int){
        val ft = mActivity.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(tag)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }
}
