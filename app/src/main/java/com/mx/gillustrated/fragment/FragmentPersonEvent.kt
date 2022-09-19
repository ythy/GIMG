package com.mx.gillustrated.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEventAdapter
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent

class FragmentPersonEvent : Fragment(){

    lateinit var mContext:CultivationActivity

    @BindView(R.id.lv_events)
    lateinit var mListView: ListView

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
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        mEventData = mPerson.events.toMutableList()
        mEventData.sortByDescending { it.happenTime }
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