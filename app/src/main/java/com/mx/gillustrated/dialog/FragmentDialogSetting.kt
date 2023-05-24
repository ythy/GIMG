package com.mx.gillustrated.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.databinding.FragmentDialogSettingBinding
import com.mx.gillustrated.util.CommonUtil

class FragmentDialogSetting : DialogFragment() {

    companion object {
        fun newInstance(): FragmentDialogSetting {
            return FragmentDialogSetting()
        }
    }

    private var _binding: FragmentDialogSettingBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var mActivity: CultivationActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = activity as CultivationActivity
        init()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




    private fun initListener(){
        binding.etJie.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if(current.toIntOrNull() != null && current.toInt() > 0){
                    mActivity.mSP.edit().putInt("cultivation_jie", current.toInt()).apply()
                    CultivationSetting.TEMP_SP_JIE_TURN = current.toInt()
                }
            }
        })
        binding.etTalent.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if(current.toIntOrNull() != null && current.toInt() > 0){
                    mActivity.mSP.edit().putInt("cultivation_talent_protect", current.toInt()).apply()
                    CultivationSetting.TEMP_TALENT_PROTECT = current.toInt()
                }
            }
        })
        binding.etDeadSymbol.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if(current != ""){
                    mActivity.mSP.edit().putString("cultivation_dead_symbol", current).apply()
                    CultivationSetting.TEMP_DEAD_SYMBOL = current
                }
            }
        })
        binding.etSkinBattle.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if(current.toIntOrNull() != null && current.toInt() > 0){
                    mActivity.mSP.edit().putInt("cultivation_skin_battle_min", current.toInt()).apply()
                }
            }
        })
        binding.btnSave.setOnClickListener {
            mActivity.mSP.edit().putInt("cultivation_punish_boss_million", binding.etBossPunish.text.toString().toInt()).apply()
            mActivity.showToast("保存成功!")
        }
        binding.btnReset.setOnClickListener {
            binding.etBossPunish.setText(CultivationSetting.SP_PUNISH_BOSS_MILLION.toString())
            mActivity.mSP.edit().putInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION).apply()
            mActivity.showToast("重置成功!")
        }
        binding.btnClose.setOnClickListener {
            this.dismiss()
        }
        binding.btnEver.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(1)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnSpecCareer.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(2)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnTips.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(3)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnAmulet.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(4)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnLabel.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(5)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnSkin.setOnClickListener {
            val ft = childFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPersonList.newInstance(6)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_person_list")
        }
        binding.btnExclusive.setOnClickListener {
            openDialog(8)
        }
    }


    fun init() {
        binding.llParentMeasure.measure(0,0)
        val margin = CommonUtil.dip2px(context, 20f)

        val battleRound = CultivationHelper.mBattleRound
        binding.tvbBattle.setConfig(TextViewBox.TextViewBoxConfig(binding.llParentMeasure.measuredWidth - margin, 20))
        binding.tvbBattle.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(index)
            }
        })
        binding.tvbBattle.setDataProvider(listOf("帮:${battleRound.bang}", "族:${battleRound.clan}", "个:${battleRound.single}"), null)

        binding.tvbBoss.setConfig(TextViewBox.TextViewBoxConfig(binding.llParentMeasure.measuredWidth - margin, 20))
        binding.tvbBoss.setCallback(object : TextViewBox.Callback {
            override fun onClick(index: Int) {
                openDialog(20 + index)
            }
        })
        binding.tvbBoss.setDataProvider(listOf("霸:${battleRound.boss[0]}", "暗:${battleRound.boss[1]}", "滚:${battleRound.boss[2]}", "王:${battleRound.boss[3]}"), null)

        binding.etJie.setText(mActivity.mSP.getInt("cultivation_jie", CultivationSetting.SP_JIE_TURN).toString())
        binding.etTalent.setText(mActivity.mSP.getInt("cultivation_talent_protect", CultivationSetting.SP_TALENT_PROTECT).toString())
        binding.etDeadSymbol.setText(mActivity.mSP.getString("cultivation_dead_symbol", CultivationSetting.SP_DEAD_SYMBOL))
        binding.etSkinBattle.setText(mActivity.mSP.getInt("cultivation_skin_battle_min", CultivationSetting.SP_SKIN_BATTLE_MIN).toString())

        val punishWeight = mActivity.mSP.getInt("cultivation_punish_boss_million", CultivationSetting.SP_PUNISH_BOSS_MILLION)
        binding.etBossPunish.setText(punishWeight.toString())
    }

    private fun openDialog(tag:Int){
        val ft = mActivity.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(tag)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }
}
