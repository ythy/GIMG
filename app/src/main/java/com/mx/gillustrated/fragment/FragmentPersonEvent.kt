package com.mx.gillustrated.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent

class FragmentPersonEvent : Fragment(){

    lateinit var mContext:CultivationActivity

    @BindView(R.id.lv_events)
    lateinit var mListView: ListView

    private var mEventDataString = mutableListOf<String>()
    private var mEventData = mutableListOf<PersonEvent>()
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
        val id = this.requireArguments().getString("id", "")
        mPerson = mContext.getOnlinePersonDetail(id) ?: mContext.getOfflinePersonDetail(id)!!
        mListView.adapter = ArrayAdapter(this.requireContext(), R.layout.list_simple_item_text1,
            android.R.id.text1, mEventDataString)
        updateEvent()
    }


    fun updateEvent(){
        val eventChanged = mEventData.size != mPerson.events.size
        mPerson.events.forEach {
            if(mEventData.find { e-> e.nid == it.nid} == null){
                mEventData.add(it)
                mEventDataString.add(0, CultivationHelper.showing(it.content))
            }
        }
        if(eventChanged){
            (mListView.adapter as BaseAdapter).notifyDataSetChanged()
            mListView.invalidateViews()
        }
    }

}