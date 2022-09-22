package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
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
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.util.CommonUtil

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

    @BindView(R.id.et_enemy_punish)
    lateinit var mEnemyPunish:EditText

    @BindView(R.id.et_boss_punish)
    lateinit var mBossPunish:EditText

    @OnTextChanged(R.id.et_jie)
    fun onJieTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current.toIntOrNull() != null && current.toInt() > 0){
            mActivity.mSP.edit().putInt("cultivation_jie", current.toInt()).apply()
        }
    }

    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
       mActivity.mSP.edit().putString("cultivation_punish_million", listOf(
               mEnemyPunish.text,
               mBossPunish.text
       ).joinToString()).apply()
       mActivity.showToast("保存成功!")
    }

    @OnClick(R.id.btn_reset)
    fun onResetClickHandler(){
        mActivity.mSP.edit().putString("cultivation_punish_million", CultivationSetting.SP_PUNISH_MILLION.joinToString()).apply()
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
        val margin = CommonUtil.dip2px(context, 20f)

        val battleRound = CultivationHelper.mBattleRound
        tvbBattle.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - margin, 20))
        tvbBattle.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(index)
            }
        })
        tvbBattle.setDataProvider(listOf("帮:${battleRound.bang}", "族:${battleRound.clan}", "个:${battleRound.single}"), null)

        tvbEnemy.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - margin, 20))
        tvbEnemy.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(10 + index)
            }
        })
        tvbEnemy.setDataProvider(CultivationSetting.EnemyNames.mapIndexed { index, s ->  "$s:${battleRound.enemy[index]}" }, null)

        tvbBoss.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - margin, 20))
        tvbBoss.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(20 + index)
            }
        })
        tvbBoss.setDataProvider(listOf("霸:${battleRound.boss[0]}", "暗:${battleRound.boss[1]}", "滚:${battleRound.boss[2]}", "王:${battleRound.boss[3]}"), null)

        mEtJie.setText(mActivity.mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN).toString())
        val punishWeight = mActivity.mSP.getString("cultivation_punish_million", CultivationSetting.SP_PUNISH_MILLION.joinToString())!!.split(",")
        mEnemyPunish.setText(punishWeight[0].trim())
        mBossPunish.setText(punishWeight[1].trim())
    }

    private fun openDialog(tag:Int){
        val ft = mActivity.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(tag)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }
}
