package com.mx.gillustrated.activity

import java.io.File
import java.lang.ref.WeakReference
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CardTypeListAdapter
import com.mx.gillustrated.adapter.CardTypeListAdapter.DespairTouchListener
import com.mx.gillustrated.adapter.SpinnerCommonAdapter
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.ResourceController
import com.mx.gillustrated.listener.ListenerListViewScrollHandler
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.CardTypeInfo
import com.mx.gillustrated.vo.GameInfo
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import com.mx.gillustrated.databinding.ActivityGameinfoBinding

class GameInfoActivity : BaseActivity() {

    private var mList: MutableList<CardTypeInfo> = mutableListOf()
    private var mGameType: Int = 0
    private var mAdapter: CardTypeListAdapter? = null
    private var mResourceController: ResourceController? = null
    private var mGameList: MutableList<GameInfo>? = null
    lateinit var binding:ActivityGameinfoBinding

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal class MainHandler(activity: GameInfoActivity) : Handler(Looper.getMainLooper()) {

            private val weakReference: WeakReference<GameInfoActivity> = WeakReference(activity)

            override fun handleMessage(msg: Message) {
                val activity = weakReference.get()!!
                if (msg.what == 1) {
                    activity.mList.clear()
                    val result = msg.obj as List<CardTypeInfo>
                    activity.mList.addAll(result)
                    activity.updateList(true)
                } else if (msg.what == 2) {
                    activity.mGameList = msg.obj as MutableList<GameInfo>
                    activity.mGameList!!.add(0, GameInfo(0, "关联"))
                    activity.binding.spinnerAssociation.adapter = SpinnerCommonAdapter(activity, activity.mGameList!!)
                    val index = activity.mSP.getInt(SHARE_ASSOCIATION_GAME_ID + activity.mGameType, 0)
                    activity.binding.spinnerAssociation.setSelection(activity.getGameSelection(index))
                }
            }
        }
    }



    private var mainHandler: Handler = MainHandler(this)

    private var despairTouchListener: DespairTouchListener = object : DespairTouchListener {

        override fun onSaveBtnClickListener(info: CardTypeInfo) {
            val result = mOrmHelper.cardTypeInfoDao.createOrUpdate(info)
            if (result.isCreated || result.isUpdated) {
                Toast.makeText(this@GameInfoActivity, if (result.isCreated) "新增成功" else "更新成功", Toast.LENGTH_SHORT).show()
                searchMain()
            }
        }

        override fun onDelBtnClickListener(info: CardTypeInfo) {
            AlertDialog.Builder(this@GameInfoActivity)
                    .setMessage("确定要删除吗")
                    .setPositiveButton("Ok"
                    ) { _, _ ->
                        val result = mOrmHelper.cardTypeInfoDao.delCardTypeInfoById(info.id, info.gameId)
                        if (result > -1) {
                            Toast.makeText(this@GameInfoActivity, "删除成功", Toast.LENGTH_SHORT).show()
                            searchMain()
                        }
                    }
                    .setNegativeButton("Cancel", null).show()
        }


    }

    private var onAddBtnClickListerner: View.OnClickListener = View.OnClickListener {
        mList.add(0, CardTypeInfo(mGameType))
        updateList(false)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityGameinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mGameType = intent.getIntExtra("game", 0)
        mResourceController = ResourceController(this, mGameType)

        binding.btnGameAdd.setOnClickListener(onAddBtnClickListerner)
        val gameinfoList = mOrmHelper.gameInfoDao.queryForId(mGameType)
        binding.etGameName.setText(gameinfoList.name)
        binding.etGameDetail.setText(gameinfoList.detail)
        binding.pageVBox.visibility = View.GONE

        binding.etNumber1.setText(mResourceController!!.number1)
        binding.etNumber2.setText(mResourceController!!.number2)
        binding.etNumber3.setText(mResourceController!!.number3)
        binding.etNumber4.setText(mResourceController!!.number4)
        binding.etNumber5.setText(mResourceController!!.number5)

        binding.chkOrientation.isChecked = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false)
        binding.chkOrientationEvent.isChecked = mSP.getBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, false)
        binding.chkImgDate.isChecked = mSP.getBoolean(SHARE_IMAGE_DATE + mGameType, true)
        binding.chkEventGap.isChecked = mResourceController!!.eventImagesGap
        binding.chkHeader.isChecked = mSP.getBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, false)
        binding.chkCost.isChecked = mSP.getBoolean(SHARE_SHOW_COST_COLUMN + mGameType, false)

        val pagerSize = mSP.getInt(SHARE_PAGE_SIZE + mGameType, 50)
        val pagerArray = resources.getStringArray(R.array.pagerArray)
        var position = 1
        for (i in pagerArray.indices)
            if (Integer.parseInt(pagerArray[i]) == pagerSize)
                position = i
        binding.spinnerPager.setSelection(position)

        binding.lvGameInfoMain.setOnScrollListener(ListenerListViewScrollHandler(binding.lvGameInfoMain, binding.pageVBox))
        mAdapter = CardTypeListAdapter(this, mList)
        mAdapter!!.setDespairTouchListener(despairTouchListener)
        initListener()

        searchGameList()
        searchMain()
    }

    private fun initListener(){
        binding.etNumber1.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mResourceController!!.number1 = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etNumber2.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mResourceController!!.number2 = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etNumber3.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mResourceController!!.number3 = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etNumber4.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mResourceController!!.number4 = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etNumber5.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mResourceController!!.number5 = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.chkOrientation.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION + mGameType, isChecked).apply()
        }
        binding.chkOrientationEvent.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, isChecked).apply()
        }
        binding.chkEventGap.setOnCheckedChangeListener { _, isChecked ->
            mResourceController!!.eventImagesGap = isChecked
        }
        binding.chkImgDate.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_IMAGE_DATE + mGameType, isChecked).apply()
        }
        binding.chkHeader.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, isChecked).apply()
        }
        binding.chkCost.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_SHOW_COST_COLUMN + mGameType, isChecked).apply()
        }
        binding.spinnerPager.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val array = resources.getStringArray(R.array.pagerArray)
                mSP.edit().putInt(SHARE_PAGE_SIZE + mGameType, Integer.parseInt(array[position])).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
        binding.spinnerAssociation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mSP.edit().putInt(SHARE_ASSOCIATION_GAME_ID + mGameType, mGameList!![position].id).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
        binding.btnSaveAll.setOnClickListener {
            val gameInfo = GameInfo()
            gameInfo.id = this.mGameType
            gameInfo.detail = binding.etGameDetail.text.toString()
            gameInfo.name = binding.etGameName.text.toString()
            val result = mOrmHelper.gameInfoDao.update(gameInfo)
            if (result == 1)
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }
        binding.btnDelAll.setOnClickListener {
            AlertDialog.Builder(this@GameInfoActivity)
                .setMessage("确定要删除吗")
                .setPositiveButton("Ok"
                ) { _, _ ->
                    mOrmHelper.cardInfoDao.delCardInfoByGameId(mGameType)
                    mOrmHelper.eventInfoDao.delEventInfoByGameId(mGameType)
                    mOrmHelper.cardTypeInfoDao.delCardTypeInfoByGameId(mGameType)
                    mOrmHelper.gameInfoDao.deleteById(mGameType)

                    val imagesFileDir = File(
                        Environment.getExternalStorageDirectory(),
                        MConfig.SD_PATH + "/" + mGameType)
                    if (imagesFileDir.exists()) {
                        val child = imagesFileDir.listFiles()
                        if (child?.isNotEmpty() == true){
                            deleteImages(child.toList())
                        }
                        imagesFileDir.delete()
                    }
                    val eventFileDir = File(
                        Environment.getExternalStorageDirectory(),
                        MConfig.SD_EVENT_PATH + "/" + mGameType)
                    if (eventFileDir.exists()) {
                        val child = eventFileDir.listFiles()
                        if (child?.isNotEmpty() == true){
                            deleteImages(child.toList())
                        }
                        eventFileDir.delete()
                    }

                    val intent = Intent(
                        this@GameInfoActivity,
                        GameListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    this@GameInfoActivity.finish()
                }.setNegativeButton("Cancel", null).show()
        }

    }

    private fun searchMain() {
        mainHandler.post {
            val list = mOrmHelper.cardTypeInfoDao.queryForEq(CardTypeInfo.COLUMN_GAMETYPE, mGameType)
            val msg = mainHandler.obtainMessage()
            msg.what = 1
            msg.obj = list
            mainHandler.sendMessage(msg)
        }
    }

    private fun searchGameList() {
        mainHandler.post {
            val list = mOrmHelper.gameInfoDao.queryForAll()
            val msg = mainHandler.obtainMessage()
            msg.what = 2
            msg.obj = list
            mainHandler.sendMessage(msg)
        }
    }

    private fun getGameSelection(id: Int): Int {
        if (mGameList != null && mGameList!!.size > 0) {
            for (i in mGameList!!.indices) {
                if (mGameList!![i].id == id)
                    return i
            }
        }
        return 0
    }

    private fun updateList(flag: Boolean) {
        if (flag)
            binding.lvGameInfoMain.adapter = mAdapter
        else
            mAdapter!!.notifyDataSetChanged()
    }

}
