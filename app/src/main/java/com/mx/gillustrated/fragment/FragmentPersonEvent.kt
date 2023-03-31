package com.mx.gillustrated.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEventAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.dialog.FragmentDialogEmperor
import com.mx.gillustrated.dialog.FragmentDialogPerson
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent

class FragmentPersonEvent(private val mCallback: FragmentDialogPerson.IViewpageCallback) : Fragment(){

    lateinit var mContext:CultivationActivity

    @BindView(R.id.lv_events)
    lateinit var mListView: ListView

    @BindView(R.id.tv_seq)
    lateinit var mSeq: TextView

    @BindView(R.id.tv_deadCount)
    lateinit var mDead: TextView

    @OnClick(R.id.tv_seq)
    fun onSpecClickHandler(){
        if(mPerson.allianceId != "6000601")
            return
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogEmperor.newInstance(mPerson.id)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_emperor")
    }

    @OnCheckedChanged(R.id.sch_del)
    fun onDelHandler(checked:Boolean){
        if(checked)
            specRender()
        else
            normalRender()
    }

    @BindView(R.id.sch_sex)
    lateinit var mSS: Switch

    @OnCheckedChanged(R.id.sch_sex)
    fun onSSHandler(checked:Boolean){
        mCallback.update(4, if(checked) "Y" else "N")
    }


    private var mEventData = mutableListOf<PersonEvent>()
    private var mEventDataString = mutableListOf<String>()

    lateinit var mPerson:Person

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vp_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
        init()
    }

    fun init(){
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        mDead.text = "${mPerson.deadExceptTimes}-${mPerson.chongFailTimes}"
        if(mPerson.specIdentity > 0)
            mSeq.text = mPerson.specIdentity.toString()
        if(mPerson.profile in 1701..1799 && mPerson.gender == NameUtil.Gender.Female){
            mSS.visibility = View.VISIBLE
        }else{
            mSS.visibility = View.GONE
        }
        normalRender()
    }

    private fun normalRender(){
        mEventDataString = mPerson.events.toMutableList().sortedByDescending { it.happenTime }.map {
                CultivationHelper.showing("${CultivationHelper.getYearString(it.happenTime)} ${it.content}")
        }.toMutableList()
        mListView.adapter = ArrayAdapter(this.context!!, R.layout.list_simple_item_text1,
                android.R.id.text1, mEventDataString)
    }

    private fun specRender(){
        mEventData = mPerson.events.toMutableList().sortedByDescending { it.happenTime }.toMutableList()
        mListView.adapter = CultivationEventAdapter(this.context!!, mEventData, object : CultivationEventAdapter.EventAdapterCallback{
            override fun onDeleteHandler(event: PersonEvent) {
                mPerson.events.remove(event)
                mEventData.remove(event)
                (mListView.adapter as CultivationEventAdapter).notifyDataSetChanged()
                mListView.invalidateViews()
            }
        })
    }

    fun updateEvent(){

    }

}