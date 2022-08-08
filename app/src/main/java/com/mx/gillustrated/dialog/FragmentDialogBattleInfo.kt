package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationBattleHelper.BattleObject
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.vo.cultivation.BattleInfo
import com.mx.gillustrated.vo.cultivation.Person
import java.io.File

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogBattleInfo  : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogBattleInfo {
            return FragmentDialogBattleInfo()
        }
    }

    @BindView(R.id.lv_in_battle)
    lateinit var mListView: ListView

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        this.dismiss()
    }

    lateinit var dialogView1:DialogView1
    lateinit var dialogView2:DialogView2
    lateinit var mBattle:BattleInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_battleinfo, container, false)
        val id = this.arguments!!.getString("id", "")
        mBattle = CultivationBattleHelper.mBattles[id]!!
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mListView.adapter = ArrayAdapter(this.context!!, R.layout.list_simple_item_text1,
                android.R.id.text1, mBattle.details)
        dialogView1 = DialogView1(view)
        dialogView2 = DialogView2(view)
        showTitle()
    }

    private fun showTitle(){
        setProfile(mBattle.attacker, mBattle.attackerValue, dialogView1)
        setProfile(mBattle.defender, mBattle.defenderValue, dialogView2)
    }

    private fun setProfile(person: Person?, battleValue:BattleObject, view:DialogView){
        view.name.text = showing(battleValue.name) + " ${battleValue.hpBasis}(${battleValue.hp - battleValue.hpBasis})"
        view.hp.text = "${battleValue.hp}/${battleValue.maxhp}"
        view.attack.text = "${battleValue.attack}/${battleValue.attackBasis}"
        view.defence.text = "${battleValue.defence}/${battleValue.defenceBasis}"
        view.speed.text = "${battleValue.speed}/${battleValue.speedBasis}"

        if(person == null)
            return
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + person.gender)
            var file = File(imageDir.path, person.profile.toString() + ".png")
            if (!file.exists()) {
                file = File(imageDir.path, person.profile.toString() + ".jpg")
            }
            if (file.exists()) {
                val bmp = MediaStore.Images.Media.getBitmap(context?.contentResolver, Uri.fromFile(file))
                view.profile.setImageBitmap(bmp)
            } else
                view.profile.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface DialogView{
        var name:TextView
        var profile: ImageView
        var hp: TextView
        var attack: TextView
        var defence: TextView
        var speed: TextView
    }

    class DialogView1 constructor(view: View):DialogView{

        @BindView(R.id.tv_name_1)
        override lateinit var name:TextView

        @BindView(R.id.iv_profile_1)
        override lateinit var profile: ImageView

        @BindView(R.id.tv_hp_1)
        override lateinit var hp: TextView

        @BindView(R.id.tv_attack_1)
        override lateinit var attack: TextView

        @BindView(R.id.tv_defence_1)
        override lateinit var defence: TextView

        @BindView(R.id.tv_speed_1)
        override lateinit var speed: TextView


        init {
            ButterKnife.bind(this, view)
        }
    }

    class DialogView2 constructor(view: View):DialogView{

        @BindView(R.id.tv_name_2)
        override lateinit var name:TextView

        @BindView(R.id.iv_profile_2)
        override lateinit var profile: ImageView

        @BindView(R.id.tv_hp_2)
        override lateinit var hp: TextView

        @BindView(R.id.tv_attack_2)
        override lateinit var attack: TextView

        @BindView(R.id.tv_defence_2)
        override lateinit var defence: TextView

        @BindView(R.id.tv_speed_2)
        override lateinit var speed: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

}