package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    @BindView(R.id.tv_enemy)
    lateinit var mEnemy:TextView

    @BindView(R.id.tv_boss)
    lateinit var mBoss:TextView

    @BindView(R.id.tv_battle)
    lateinit var mBattle:TextView

    @BindView(R.id.et_jie)
    lateinit var mEtJie:EditText

    @OnTextChanged(R.id.et_jie)
    fun onJieTextChangedHandler(text:CharSequence){
        mActivity.mSP.edit().putInt("cultivation_jie", text.toString().toInt()).apply()
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
        mBattle.text = "个:${battleRound.single} 帮:${battleRound.bang} 族:${battleRound.clan}"
        mEnemy.text = CultivationHelper.EnemyNames.mapIndexed { index, s ->
            "$s: ${battleRound.enemy[index]}"
        }.joinToString(" ")
        mBoss.text = "霸:${battleRound.boss[0]} 暗:${battleRound.boss[1]} 滚:${battleRound.boss[2]} 王:${battleRound.boss[3]}"
        mEtJie.setText(mActivity.mSP.getInt("cultivation_jie", 81).toString())
    }

}
