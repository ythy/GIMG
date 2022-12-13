package com.mx.gillustrated.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.CultivationSetting.SpecPersonFirstNameWeight
import com.mx.gillustrated.dialog.FragmentDialogPerson
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person

class FragmentPersonInfo(private val mCallback: FragmentDialogPerson.IViewpageCallback)  : Fragment(){

    lateinit var mContext: CultivationActivity

//    @BindView(R.id.ll_home)
//    lateinit var mHome:LinearLayout

    @BindView(R.id.et_name_first)
    lateinit var etFirstName:EditText

    @BindView(R.id.et_name_last)
    lateinit var etLastName:EditText

    @BindView(R.id.tv_ancestor)
    lateinit var tvAncestor:TextView

    @BindView(R.id.tv_age)
    lateinit var tvAge:TextView

    @BindView(R.id.et_name)
    lateinit var etName:EditText

    @BindView(R.id.et_profile)
    lateinit var etProfile:EditText

    @BindView(R.id.et_linggen)
    lateinit var etLingGen:EditText

    @BindView(R.id.et_tianfu)
    lateinit var etTianFu:EditText

    @BindView(R.id.btn_assign)
    lateinit var btnAssign:Button

    @BindView(R.id.btn_be)
    lateinit var btnBe:Button

    @BindView(R.id.sch_singled)
    lateinit var mSwitchSingled:Switch

    @BindView(R.id.sch_dink)
    lateinit var mSwitchDink:Switch

    @BindView(R.id.sch_never_dead)
    lateinit var mSwitchNeverDead:Switch


    @BindView(R.id.tv_xiuwei)
    lateinit var tvXiuwei:TextView

    @OnClick(R.id.btn_revive)
    fun onReviveHandler(){
        if(mBtnRevive.text == "Kill"){
            mContext.killPerson(mPerson.id)
            mBtnRevive.text = "Revive"
        }else{
            mContext.revivePerson(mPerson.id)
            mBtnRevive.text = "Kill"
        }
        mCallback.update(3)
    }

    @OnClick(R.id.btn_clan)
    fun onCreateClanHandler(){
        if(mPerson.specIdentity > 0 || mContext.mClans[mPerson.ancestorId] != null)
            return
        CultivationHelper.abdicateInClan(mPerson, mContext.mClans, mContext.mPersons)
        Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
        mCallback.update(3)
        updateView()
    }


    @BindView(R.id.btn_revive)
    lateinit var mBtnRevive:Button

    @OnClick(R.id.btn_life)
    fun onLifetimeHandler(){
        val success = mContext.addPersonLifetime(mPerson.id)
        if(success){
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
            updateView()
        }
    }

    @OnClick(R.id.btn_assign)
    fun onAssignClickHandler(){
        val partner = mContext.mPersons.map { it.value }.find { it.name == etName.text.toString() || PinyinUtil.convert(it.name) == etName.text.toString() }
        if(partner != null){
            mPerson.partner = partner.id
            mPerson.partnerName = partner.name
            if(partner.partner == null){
                partner.partner = mPerson.id
                partner.partnerName = mPerson.name
            }
            CultivationHelper.addPersonEvent(partner, "与${mPerson.name}\u7ed3\u4f34")
            CultivationHelper.addPersonEvent(mPerson, "与${partner.name}\u7ed3\u4f34")
            CultivationHelper.writeHistory("${CultivationHelper.getPersonBasicString(partner)} 与 ${CultivationHelper.getPersonBasicString(mPerson)} \u7ed3\u4f34了")
            updateView()
        }
    }

    @OnClick(R.id.btn_profile)
    fun onProfileClickHandler(){
        mPerson.profile = etProfile.text.toString().toInt()
        mCallback.update(2)
        Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.btn_props)
    fun onPropsClickHandler(){
        val alliance = mContext.mAlliance[mPerson.allianceId]
        CultivationHelper.updatePersonInborn(mPerson, etTianFu.text.toString().toInt(), etLingGen.text.toString().toInt(), alliance)
        mCallback.update(1)
        updateView()
        Toast.makeText(context, "重置成功", Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.btn_name)
    fun onSaveClickHandler(){
        val first = etFirstName.text.toString()
        val second  = etLastName.text.toString()
        mPerson.name = first + second
        mPerson.lastName = first
        changedNameLoopHandler(mPerson)

        if (mContext.mClans[mPerson.ancestorId] != null && mPerson.ancestorId == mPerson.id){
            val clan = mContext.mClans[mPerson.ancestorId!!]!!
            clan.name = first
            clan.clanPersonList.forEach { (_: String, u: Person) ->
                val ming = u.name.substring(u.lastName.length)
                u.name = first + ming
                u.lastName = first
                changedNameLoopHandler(u)
            }
        }
        mCallback.update(3)
        Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
    }

    fun changedNameLoopHandler(person: Person){
        mContext.mPersons.forEach { (_: String, u: Person) ->
            if(u.partner == person.id){
                u.partnerName = person.name
            }
            if(u.parent?.first == person.id){
                u.parentName = Pair(person.name, u.parentName!!.second)
            }
            if(u.parent?.second == person.id){
                u.parentName = Pair(u.parentName!!.first, person.name)
            }
        }
    }

    @OnClick(R.id.btn_be)
    fun onBeClickHandler(){
        val partner = mContext.getOnlinePersonDetail(mPerson.partner) ?: mContext.getOfflinePersonDetail(mPerson.partner)
        mPerson.partner = null
        mPerson.partnerName = null
        if(partner?.partner == mPerson.id){
            partner.partner = null
            partner.partnerName = null
        }
        updateView()
        Toast.makeText(context, "BE成功", Toast.LENGTH_SHORT).show()
    }

    @OnCheckedChanged(R.id.sch_singled)
    fun onFavSwitch(checked:Boolean){
        mPerson.singled = checked
    }

    @OnCheckedChanged(R.id.sch_dink)
    fun onDinkSwitch(checked:Boolean){
        mPerson.dink = checked
    }

    @OnCheckedChanged(R.id.sch_never_dead)
    fun onNeverDeadSwitch(checked:Boolean){
        mPerson.neverDead = checked
    }

    lateinit var mPerson: Person

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vp_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
        init()
    }

    fun init(){
       // val draw = CultivationHome(mContext)
       // mHome.addView(draw)
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        if(mContext.getOnlinePersonDetail(mPerson.id) == null){
            mBtnRevive.text = "Revive"
        }else{
            mBtnRevive.text = "Kill"
        }
        mSwitchSingled.isChecked = mPerson.singled
        mSwitchDink.isChecked = mPerson.dink
        mSwitchNeverDead.isChecked =  mPerson.neverDead
        etProfile.setText(mPerson.profile.toString())
        if(mPerson.specIdentity > 0){
            val persons =  CultivationSetting.getAllSpecPersons()
            val spec = persons.find { it.identity == mPerson.specIdentity }
            if(spec == null){
                etTianFu.setText(SpecPersonFirstNameWeight.first.toString())
                etLingGen.setText(SpecPersonFirstNameWeight.second.toString())
            }else{
                etTianFu.setText(spec.tianfuWeight.toString())
                etLingGen.setText(spec.linggenWeight.toString())
            }
        }
        updateView()
    }

    private fun updateView(){
        if (mPerson.gender == NameUtil.Gender.Female && mPerson.partner == null){
            btnAssign.visibility = View.VISIBLE
            etName.isEnabled = true
        }else{
            btnAssign.visibility = View.GONE
            etName.isEnabled = false
        }
        etName.setText(CultivationHelper.showing(mPerson.partnerName ?: ""))

        if (mPerson.partner == null){
            btnBe.visibility = View.GONE
        }else{
            btnBe.visibility = View.VISIBLE
        }

        etFirstName.setText(CultivationHelper.showing(mPerson.lastName))
        etLastName.setText(CultivationHelper.showing(mPerson.name.substring(mPerson.lastName.length)))

        tvAge.text = CultivationHelper.showAge(mPerson)
        tvAncestor.text = "${mPerson.ancestorOrignId}/${mPerson.ancestorOrignLevel}-${mPerson.ancestorId}/${mPerson.ancestorLevel}"

        val alliance = mContext.mAlliance[mPerson.allianceId]
        if(alliance != null){
            val tianValue = CultivationHelper.getPersonTianfu(mPerson.tianfus.find { it.type == 2 }?.id)?.bonus ?: 0
            tvXiuwei.text = "(${mPerson.lingGenType.qiBasic}+P:${mPerson.extraXiuwei}+S:${mPerson.battlexiuwei}+A:${mPerson.allianceXiuwei}+C:${mPerson.clanXiuwei}" +
                    "+N:${mPerson.nationXiuwei}+E:${mPerson.equipmentXiuwei}+N:${CultivationHelper.getNationXiuwei(mPerson)}+B:${mPerson.bossXiuwei})" +
                    "${mPerson.lingGenType.qiBasic + mPerson.extraXiuwei + mPerson.battlexiuwei + mPerson.allianceXiuwei + mPerson.clanXiuwei + mPerson.nationXiuwei +
                            mPerson.equipmentXiuwei + CultivationHelper.getNationXiuwei(mPerson) + mPerson.bossXiuwei}" +
                    "*${1 + (tianValue.toDouble()/100.0) + alliance.xiuweiMulti.toDouble()/100.0}(C:${tianValue.toDouble()/100.0}+A:${alliance.xiuweiMulti.toDouble()/100.0})"
        }

    }
}