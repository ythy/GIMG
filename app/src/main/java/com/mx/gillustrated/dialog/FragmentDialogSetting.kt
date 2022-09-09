package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper

@RequiresApi(Build.VERSION_CODES.N)
class FragmentDialogSetting : DialogFragment() {

    companion object {
        fun newInstance(): FragmentDialogSetting {
            return FragmentDialogSetting()
        }
    }

    @BindView(R.id.tv_e1)
    lateinit var mTvE1:TextView

    @BindView(R.id.tv_e2)
    lateinit var mTvE2:TextView

    @BindView(R.id.tv_e3)
    lateinit var mTvE3:TextView

    @BindView(R.id.tv_e4)
    lateinit var mTvE4:TextView

    @BindView(R.id.tv_e5)
    lateinit var mTvE5:TextView

    @BindView(R.id.tv_e6)
    lateinit var mTvE6:TextView

    @BindView(R.id.tv_b1)
    lateinit var mTvB1:TextView

    @BindView(R.id.tv_b2)
    lateinit var mTvB2:TextView

    @BindView(R.id.tv_b3)
    lateinit var mTvB3:TextView

    @BindView(R.id.tv_b4)
    lateinit var mTvB4:TextView

    @BindView(R.id.tv_bang)
    lateinit var mTvBang:TextView

    @BindView(R.id.tv_clan)
    lateinit var mTvClan:TextView

    @BindView(R.id.tv_single)
    lateinit var mTvSingle:TextView

    @OnClick(R.id.tv_bang, R.id.tv_clan, R.id.tv_single, R.id.tv_b1, R.id.tv_b2, R.id.tv_b3, R.id.tv_b4,
            R.id.tv_e1, R.id.tv_e2, R.id.tv_e3, R.id.tv_e4, R.id.tv_e5, R.id.tv_e6)
    fun onDataClick(text:TextView){
        val ft = mActivity.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(text.tag.toString().toInt())
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }

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
        mActivity.mSP.edit().putInt("cultivation_jie", text.toString().toInt()).apply()
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
        val battleRound = CultivationHelper.mBattleRound
        mTvBang.text = "帮:${battleRound.bang}"
        mTvClan.text = "族:${battleRound.clan}"
        mTvSingle.text = "个:${battleRound.single}"

        mTvE1.text = "${CultivationHelper.EnemyNames[0]}:${battleRound.enemy[0]}"
        mTvE2.text = "${CultivationHelper.EnemyNames[1]}:${battleRound.enemy[1]}"
        mTvE3.text = "${CultivationHelper.EnemyNames[2]}:${battleRound.enemy[2]}"
        mTvE4.text = "${CultivationHelper.EnemyNames[3]}:${battleRound.enemy[3]}"
        mTvE5.text = "${CultivationHelper.EnemyNames[4]}:${battleRound.enemy[4]}"
        mTvE6.text = "${CultivationHelper.EnemyNames[5]}:${battleRound.enemy[5]}"

        mTvB1.text = "霸:${battleRound.boss[0]}"
        mTvB2.text = "暗:${battleRound.boss[1]}"
        mTvB3.text = "滚:${battleRound.boss[2]}"
        mTvB4.text = "王:${battleRound.boss[3]}"

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

}
