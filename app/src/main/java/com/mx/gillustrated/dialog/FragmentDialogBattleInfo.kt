package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.mx.gillustrated.adapter.CultivationBattleAdapter
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationBattleHelper.BattleObject
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.util.NameUtil
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

    @OnClick(R.id.iv_profile_1)
    fun onName1ClickHandler(){
        if(mBattle.attacker == null)
            return
        val ft = activity!!.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false
        val bundle = Bundle()
        bundle.putString("id", mBattle.attacker!!.id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    @OnClick(R.id.iv_profile_2)
    fun onName2ClickHandler(){
        if(mBattle.defender == null)
            return
        val ft = activity!!.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false
        val bundle = Bundle()
        bundle.putString("id", mBattle.defender!!.id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
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
        mBattle.details.forEach {
            it.winner = mBattle.winnerName
            it.looser = mBattle.looserName
        }
        mListView.adapter = CultivationBattleAdapter(this.context!!, mBattle.details)
        dialogView1 = DialogView1(view)
        dialogView2 = DialogView2(view)
        showTitle()
    }

    private fun showTitle(){
        setProfile(mBattle.attacker, mBattle.attackerValue, dialogView1)
        setProfile(mBattle.defender, mBattle.defenderValue, dialogView2, false)
    }

    private fun setProfile(person: Person?, battleValue:BattleObject, view:DialogView, left:Boolean = true){

        val spannable = SpannableString(showing(battleValue.name)+"(${battleValue.follower.size})")
        if(battleValue.name == mBattle.winnerName){
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#108A5E")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }else{
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#BF7C92")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        view.name.setText(spannable, TextView.BufferType.SPANNABLE)

        if(left){
            view.hp.text = "${battleValue.hpBasis}→${battleValue.hp}"
            view.attack.text = "${battleValue.attackInit}→${battleValue.attack}"
            view.defence.text = "${battleValue.defenceInit}→${battleValue.defence}"
            view.speed.text = "${battleValue.speedInit}→${battleValue.speed}"
        }else{
            view.hp.text = "${battleValue.hp}←${battleValue.hpBasis}"
            view.attack.text = "${battleValue.attack}←${battleValue.attackInit}"
            view.defence.text = "${battleValue.defence}←${battleValue.defenceInit}"
            view.speed.text = "${battleValue.speed}←${battleValue.speedInit}"
        }

        if(person == null)
            return
        var profile = person.profile
        if(person.gender == NameUtil.Gender.Female && person.profile in 1701..1799){
            profile = 0
        }
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + person.gender)
            var file = File(imageDir.path,  "$profile.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$profile.jpg")
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