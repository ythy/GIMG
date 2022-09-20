package com.mx.gillustrated.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationEventAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent

class FragmentPersonEvent : Fragment(){

    lateinit var mContext:CultivationActivity

    @BindView(R.id.lv_events)
    lateinit var mListView: ListView

    @OnCheckedChanged(R.id.sch_del)
    fun onDelHandler(checked:Boolean){
        if(checked)
            specRender()
        else
            normalRender()
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
        normalRender()
    }

    fun normalRender(){
        mEventDataString = mPerson.events.toMutableList().sortedByDescending { it.happenTime }.map { CultivationHelper.showing(it.content) }.toMutableList()
        mListView.adapter = ArrayAdapter(this.context!!, R.layout.list_simple_item_text1,
                android.R.id.text1, mEventDataString)
    }

    fun specRender(){
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