package com.mx.gillustrated.activity

import com.mx.gillustrated.adapter.EventsAdapter
import com.mx.gillustrated.listener.ListenerListViewScrollHandler
import com.mx.gillustrated.vo.EventInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import com.mx.gillustrated.databinding.ActivityEventsBinding
import java.lang.ref.WeakReference

class EventsActivity : BaseActivity() {



    private var mAdapter: EventsAdapter? = null
    private var mList: MutableList<EventInfo> = mutableListOf()
    private var mGameId: Int = 0
    private val mainHandler = ManiHandler(this)
    lateinit var binding:ActivityEventsBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mGameId = intent.getIntExtra("game", 0)
        binding.pageVBox.visibility = View.GONE
        binding.lvDespairMain.setOnScrollListener(ListenerListViewScrollHandler(binding.lvDespairMain, binding.pageVBox))
        mAdapter = EventsAdapter(this, mList)
        binding.btnDespairAdd.setOnClickListener {
            val intent = Intent(this@EventsActivity, EventInfoActivity::class.java)
            intent.putExtra("event", 0)
            intent.putExtra("game", mGameId)
            startActivity(intent)
        }
        binding.lvDespairMain.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@EventsActivity, EventInfoActivity::class.java)
            intent.putExtra("event", mList[position].id)
            intent.putExtra("game", mGameId)
            startActivity(intent)
        }
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
            binding.lvDespairMain.adapter = mAdapter
        else {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal class ManiHandler(eventsActivity: EventsActivity) : Handler(Looper.getMainLooper()){

            private val weakReference: WeakReference<EventsActivity> = WeakReference(eventsActivity)

            override fun handleMessage(msg: Message) {
                val activity = weakReference.get()!!
                if (msg.what == 1) {
                    activity.mList.clear()
                    activity.mList.addAll(msg.obj as List<EventInfo>)
                    activity.updateList(true)
                }
            }
        }
    }

}

