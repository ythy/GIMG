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
            CultivationHelper.addPersonEvent(partner, "${CultivationHelper.mCurrentXun / 12}年 与${mPerson.name}\u7ed3\u4f34")
            CultivationHelper.addPersonEvent(mPerson, "${CultivationHelper.mCurrentXun / 12}年 与${partner.name}\u7ed3\u4f34")
            CultivationHelper.writeHistory("${CultivationHelper.getPersonBasicString(partner)} 与 ${CultivationHelper.getPersonBasicString(mPerson)} \u7ed3\u4f34了", null, 0)
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
        CultivationHelper.updatePersonInborn(mPerson, etLingGen.text.toString().toInt(), etTianFu.text.toString().toInt())
        mCallback.update(1)
        Toast.makeText(context, "重置成功", Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.btn_name)
    fun onSaveClickHandler(){
        val first = etFirstName.text.toString()
        val second  = etLastName.text.toString()
        mPerson.name = first + second
        mPerson.lastName = first
        mContext.mPersons.forEach { (_: String, u: Person) ->
            if(u.partner == mPerson.id){
                u.partnerName = mPerson.name
            }
            if(u.parent?.first == mPerson.id){
                u.parentName = Pair(mPerson.name, u.parentName!!.second)
            }
            if(u.parent?.second == mPerson.id){
                u.parentName = Pair(u.parentName!!.first, mPerson.name)
            }
        }
        mCallback.update(3)
        Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
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
        mSwitchSingled.isChecked = mPerson.singled
        mSwitchDink.isChecked = mPerson.dink
        etProfile.setText(mPerson.profile.toString())
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
    }
}