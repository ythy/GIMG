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

    @BindView(R.id.tvb_boss)
    lateinit var tvbBoss: TextViewBox

    @BindView(R.id.et_jie)
    lateinit var mEtJie:EditText

    @BindView(R.id.et_talent)
    lateinit var mTalent:EditText

    @BindView(R.id.et_dead_symbol)
    lateinit var mDeadSymbol:EditText

    @BindView(R.id.et_skin_battle)
    lateinit var mSkinBattle:EditText

    @BindView(R.id.et_boss_punish)
    lateinit var mBossPunish:EditText

    @OnTextChanged(R.id.et_jie)
    fun onJieTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current.toIntOrNull() != null && current.toInt() > 0){
            mActivity.mSP.edit().putInt("cultivation_jie", current.toInt()).apply()
            CultivationSetting.TEMP_SP_JIE_TURN = current.toInt()
        }
    }

    @OnTextChanged(R.id.et_talent)
    fun onTalentTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current.toIntOrNull() != null && current.toInt() > 0){
            mActivity.mSP.edit().putInt("cultivation_talent_protect", current.toInt()).apply()
            CultivationSetting.TEMP_TALENT_PROTECT = current.toInt()
        }
    }

    @OnTextChanged(R.id.et_dead_symbol)
    fun onDeadSymbolTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current != ""){
            mActivity.mSP.edit().putString("cultivation_dead_symbol", current).apply()
            CultivationSetting.TEMP_DEAD_SYMBOL = current
        }
    }

    @OnTextChanged(R.id.et_skin_battle)
    fun onSkinBattleMinTextChangedHandler(text:CharSequence){
        val current = text.toString()
        if(current.toIntOrNull() != null && current.toInt() > 0){
            mActivity.mSP.edit().putInt("cultivation_skin_battle_min", current.toInt()).apply()
        }
    }


    @OnClick(R.id.btn_save)
    fun onSaveClickHandler(){
       mActivity.mSP.edit().putInt("cultivation_punish_boss_million", mBossPunish.text.toString().toInt()).apply()
       mActivity.showToast("保存成功!")
    }

    @OnClick(R.id.btn_reset)
    fun onResetClickHandler(){
        mBossPunish.setText(CultivationSetting.SP_PUNISH_BOSS_MILLION.toString())
        mActivity.mSP.edit().putInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION).apply()
        mActivity.showToast("重置成功!")
    }

    @OnClick(R.id.btn_close)
    fun onCloseClickHandler(){
        this.dismiss()
    }

    @OnClick(R.id.btn_ever)
    fun onEverClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(1)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_spec_career)
    fun onSpecClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(2)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_tips)
    fun onTipsClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(3)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_amulet)
    fun onAmuletClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(4)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_label)
    fun onLabelClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(5)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }

    @OnClick(R.id.btn_skin)
    fun onSkinClick(){
        val ft = childFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPersonList.newInstance(6)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_person_list")
    }



    @OnClick(R.id.btn_exclusive)
    fun onExclusiveClick(){
        openDialog(8)
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

        tvbBoss.setConfig(TextViewBox.TextViewBoxConfig(measures.measuredWidth - margin, 20))
        tvbBoss.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(20 + index)
            }
        })
        tvbBoss.setDataProvider(listOf("霸:${battleRound.boss[0]}", "暗:${battleRound.boss[1]}", "滚:${battleRound.boss[2]}", "王:${battleRound.boss[3]}"), null)

        mEtJie.setText(mActivity.mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN).toString())
        mTalent.setText(mActivity.mSP.getInt("cultivation_talent_protect", CultivationSetting.SP_TALENT_PROTECT).toString())
        mDeadSymbol.setText(mActivity.mSP.getString("cultivation_dead_symbol", CultivationSetting.SP_DEAD_SYMBOL))
        mSkinBattle.setText(mActivity.mSP.getInt("cultivation_skin_battle_min", CultivationSetting.SP_SKIN_BATTLE_MIN).toString())

        val punishWeight = mActivity.mSP.getInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION)
        mBossPunish.setText(punishWeight.toString())
    }

    private fun openDialog(tag:Int){
        val ft = mActivity.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(tag)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }
}
