package com.mx.gillustrated.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
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

    @OnClick(R.id.btn_close)
    fun onCloseClickHandler(){
        this.dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        init()
    }

    fun init() {
        val battleRound = CultivationHelper.mBattleRound
        mBattle.text = "个:${battleRound.single} 帮:${battleRound.bang} 族:${battleRound.clan}"
        mEnemy.text = CultivationHelper.EnemyNames.mapIndexed { index, s ->
            "$s: ${battleRound.enemy[index]}"
        }.joinToString(" ")
        mBoss.text = "霸:${battleRound.boss[0]} 暗:${battleRound.boss[1]} 滚:${battleRound.boss[2]} 王:${battleRound.boss[3]}"
    }

}
