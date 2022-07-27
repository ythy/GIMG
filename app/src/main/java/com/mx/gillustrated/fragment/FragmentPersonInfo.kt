package com.mx.gillustrated.fragment

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHome
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.util.*

class FragmentPersonInfo  : Fragment(){

    lateinit var mContext: CultivationActivity

//    @BindView(R.id.ll_home)
//    lateinit var mHome:LinearLayout

    @BindView(R.id.et_name)
    lateinit var etName:EditText

    @BindView(R.id.btn_assign)
    lateinit var btnAssign:Button

    @OnClick(R.id.btn_assign)
    fun onAssignClickHandler(){
        val partner = mContext.mPersons.map { it.value }.find { it.name == etName.text.toString() || PinyinUtil.convert(it.name) == etName.text.toString() }
        if(partner != null){
            mPerson.partner = partner.id
            mPerson.partnerName = partner.name
            CultivationHelper.addPersonEvent(partner, "${CultivationHelper.mCurrentXun / 12}年 与${mPerson.name}结伴")
            CultivationHelper.addPersonEvent(mPerson, "${CultivationHelper.mCurrentXun / 12}年 与${partner.name}结伴")
            CultivationHelper.writeHistory("${CultivationHelper.getPersonBasicString(partner)} 与 ${CultivationHelper.getPersonBasicString(mPerson)} 结伴了", null, 0)
            updateView()
        }
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
        mPerson = mContext.getOnlinePersonDetail(id) ?: mContext.getOfflinePersonDetail(id)!!
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
    }
}