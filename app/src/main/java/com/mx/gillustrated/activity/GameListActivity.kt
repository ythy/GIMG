package com.mx.gillustrated.activity

import com.mx.gillustrated.adapter.GameListAdapter
import com.mx.gillustrated.adapter.GameListAdapter.DespairTouchListener
import com.mx.gillustrated.listener.ListenerListViewScrollHandler
import com.mx.gillustrated.vo.GameInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.Toast
import com.mx.gillustrated.databinding.ActivityGameBinding
import java.lang.ref.WeakReference

class GameListActivity : BaseActivity() {

    private var mAdapter: GameListAdapter? = null
    private var mList: MutableList<GameInfo> = mutableListOf()
    private var mainHandler: MainHandler = MainHandler(this)
    lateinit var binding:ActivityGameBinding

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal class MainHandler(activity: GameListActivity) : Handler(Looper.getMainLooper()){

            private val weakReference:WeakReference<GameListActivity> = WeakReference(activity)

            override fun handleMessage(msg: Message) {
                val activity = weakReference.get()!!
                if (msg.what == 1) {
                    activity.mList.clear()
                    activity.mList.addAll(msg.obj as List<GameInfo>)
                    activity.updateList(true)
                }
            }
        }

    }


    private var despairTouchListener: DespairTouchListener = object : DespairTouchListener {

        override fun onSaveBtnClickListener(info: GameInfo) {
            val result = mOrmHelper.gameInfoDao.createOrUpdate(info)
            if (result.isCreated || result.isUpdated) {
                Toast.makeText(this@GameListActivity, if (result.isCreated) "新增成功" else "更新成功", Toast.LENGTH_SHORT).show()
                searchMain()
            }

        }

        override fun onDetailBtnClickListener(info: GameInfo) {
            val intent = Intent(this@GameListActivity, GameInfoActivity::class.java)
            intent.putExtra("game", info.id)
            startActivity(intent)
        }

    }

    private var onAddBtnClickListerner: View.OnClickListener = View.OnClickListener {
        mList.add(0, GameInfo())
        updateList(false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnDespairAdd.setOnClickListener(onAddBtnClickListerner)
        binding.pageVBox.visibility = View.GONE
        binding.lvDespairMain.setOnScrollListener(ListenerListViewScrollHandler(binding.lvDespairMain, binding.pageVBox))
        mAdapter = GameListAdapter(this, mList)
        mAdapter!!.setDespairTouchListener(despairTouchListener)

        searchMain()

    }

    private fun searchMain() {
        mainHandler.post {
            val list = mOrmHelper.gameInfoDao.queryForAll()
            val msg = mainHandler.obtainMessage()
            msg.what = 1
            msg.obj = list
            mainHandler.sendMessage(msg)
        }
    }

    private fun updateList(flag: Boolean) {
        if (flag)
            binding.lvDespairMain.adapter = mAdapter
        else
            mAdapter!!.notifyDataSetChanged()
    }

}

