package com.mx.gillustrated.activity

import java.util.ArrayList

import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.EventsAdapter
import com.mx.gillustrated.listener.ListenerListViewScrollHandler
import com.mx.gillustrated.vo.EventInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.RelativeLayout

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemClick
import java.lang.ref.WeakReference

class EventsActivity : BaseActivity() {

    @BindView(R.id.lvDespairMain)
    internal lateinit var mLvDespairMain: ListView

    @BindView(R.id.pageVBox)
    internal lateinit var pageVboxLayout: RelativeLayout

    private var mAdapter: EventsAdapter? = null
    private var mList: MutableList<EventInfo>? = null
    private var mGameId: Int = 0
    private val mainHandler = ManiHandler(this)


    @OnClick(R.id.btnDespairAdd)
    internal fun onAddBtnClickListener() {
        val intent = Intent(this@EventsActivity, EventInfoActivity::class.java)
        intent.putExtra("event", 0)
        intent.putExtra("game", mGameId)
        startActivity(intent)
    }

    @OnItemClick(R.id.lvDespairMain)
    internal fun listItemClickHandler(position: Int) {
        val intent = Intent(this@EventsActivity, EventInfoActivity::class.java)
        intent.putExtra("event", mList!![position].id)
        intent.putExtra("game", mGameId)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_events)
        ButterKnife.bind(this)

        mGameId = intent.getIntExtra("game", 0)
        pageVboxLayout.visibility = View.GONE
        mLvDespairMain.setOnScrollListener(ListenerListViewScrollHandler(mLvDespairMain, pageVboxLayout))
        mList = ArrayList()
        mAdapter = EventsAdapter(this, mList)

        searchMain()

    }

    private fun searchMain() {
        mainHandler.post {
            val list = mOrmHelper.eventInfoDao.getListByGameId(mGameId, null)
            val msg = mainHandler.obtainMessage()
            msg.what = 1
            msg.obj = list
            mainHandler.sendMessage(msg)
        }
    }

    private fun updateList(flag: Boolean) {
        if (flag)
            mLvDespairMain.adapter = mAdapter
        else {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal class ManiHandler(eventsActivity: EventsActivity) : Handler(){

            private val weakReference: WeakReference<EventsActivity> = WeakReference(eventsActivity)

            override fun handleMessage(msg: Message) {
                val activity = weakReference.get()!!
                if (msg.what == 1) {
                    activity.mList!!.clear()
                    activity.mList!!.addAll(msg.obj as List<EventInfo>)
                    activity.updateList(true)
                }
            }
        }
    }

}

