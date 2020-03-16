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
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick
import butterknife.OnItemSelected
import butterknife.OnTextChanged

class GameInfoActivity : BaseActivity() {

    private var btnAdd: ImageButton? = null
    private var mList: MutableList<CardTypeInfo> = mutableListOf()
    private var mGameType: Int = 0
    private var mAdapter: CardTypeListAdapter? = null
    private var mResourceController: ResourceController? = null
    private var mGameList: MutableList<GameInfo>? = null

    companion object {

        @Suppress("UNCHECKED_CAST")
        internal class MainHandler(activity: GameInfoActivity) : Handler() {

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
                    activity.spinnerAssociation.adapter = SpinnerCommonAdapter(activity, activity.mGameList!!)
                    val index = activity.mSP.getInt(SHARE_ASSOCIATION_GAME_ID + activity.mGameType, 0)
                    activity.spinnerAssociation.setSelection(activity.getGameSelection(index))
                }
            }
        }
    }



    @BindView(R.id.et_number1)
    internal lateinit var mEtNumber1: EditText

    @BindView(R.id.et_number2)
    internal lateinit var mEtNumber2: EditText

    @BindView(R.id.et_number3)
    internal lateinit var mEtNumber3: EditText

    @BindView(R.id.et_number4)
    internal lateinit var mEtNumber4: EditText

    @BindView(R.id.et_number5)
    internal lateinit var mEtNumber5: EditText

    @BindView(R.id.etGameDetail)
    internal lateinit var mEtGameDetail: EditText

    @BindView(R.id.etGameName)
    internal lateinit var mEtGameName: EditText

    @BindView(R.id.chkOrientation)
    internal lateinit var chkOrientation: CheckBox

    @BindView(R.id.chkOrientationEvent)
    internal lateinit var chkOrientationE: CheckBox

    @BindView(R.id.chkEventGap)
    internal lateinit var chkEventGap: CheckBox

    @BindView(R.id.chkImgDate)
    internal lateinit var chkImgDate: CheckBox

    @BindView(R.id.chkHeader)
    internal lateinit var chkHeader: CheckBox

    @BindView(R.id.chkCost)
    internal lateinit var chkCost: CheckBox

    @BindView(R.id.spinnerPager)
    internal lateinit var spinnerPager: Spinner

    @BindView(R.id.spinnerAssociation)
    internal lateinit var spinnerAssociation: Spinner

    @BindView(R.id.lvGameInfoMain)
    internal lateinit var mLvGameMain: ListView

    @BindView(R.id.pageVBox)
    internal lateinit var pageVBoxLayout: RelativeLayout


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

    @OnTextChanged(R.id.et_number1)
    internal fun onNumber1TextChanged(text: CharSequence) {
        mResourceController!!.number1 = text.toString()
    }

    @OnTextChanged(R.id.et_number2)
    internal fun onNumber2TextChanged(text: CharSequence) {
        mResourceController!!.number2 = text.toString()
    }

    @OnTextChanged(R.id.et_number3)
    internal fun onNumber3TextChanged(text: CharSequence) {
        mResourceController!!.number3 = text.toString()
    }

    @OnTextChanged(R.id.et_number4)
    internal fun onNumber4TextChanged(text: CharSequence) {
        mResourceController!!.number4 = text.toString()
    }

    @OnTextChanged(R.id.et_number5)
    internal fun onNumber5TextChanged(text: CharSequence) {
        mResourceController!!.number5 = text.toString()
    }

    @OnCheckedChanged(R.id.chkOrientation)
    internal fun onOrientationCheckedChanged(checkBox: CheckBox) {
        mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION + mGameType, checkBox.isChecked).apply()
    }

    @OnCheckedChanged(R.id.chkOrientationEvent)
    internal fun onOrientationECheckedChanged(checkBox: CheckBox) {
        mSP.edit().putBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, checkBox.isChecked).apply()
    }

    @OnCheckedChanged(R.id.chkEventGap)
    internal fun onEventGapCheckedChanged(checkBox: CheckBox) {
        mResourceController!!.eventImagesGap = checkBox.isChecked
    }

    @OnCheckedChanged(R.id.chkImgDate)
    internal fun onImageDateCheckedChanged(checkBox: CheckBox) {
        mSP.edit().putBoolean(SHARE_IMAGE_DATE + mGameType, checkBox.isChecked).apply()
    }

    @OnCheckedChanged(R.id.chkHeader)
    internal fun onHeaderCheckedChanged(checkBox: CheckBox) {
        mSP.edit().putBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, checkBox.isChecked).apply()
    }

    @OnCheckedChanged(R.id.chkCost)
    internal fun onCostCheckedChanged(checkBox: CheckBox) {
        mSP.edit().putBoolean(SHARE_SHOW_COST_COLUMN + mGameType, checkBox.isChecked).apply()
    }

    @OnItemSelected(R.id.spinnerPager)
    internal fun onPagerChanged(position: Int) {
        val array = resources.getStringArray(R.array.pagerArray)
        mSP.edit().putInt(SHARE_PAGE_SIZE + mGameType, Integer.parseInt(array[position])).apply()
    }

    @OnItemSelected(R.id.spinnerAssociation)
    internal fun onAssociationChanged(position: Int) {
        mSP.edit().putInt(SHARE_ASSOCIATION_GAME_ID + mGameType, this.mGameList!![position].id).apply()
    }


    @OnClick(R.id.btnSaveAll)
    internal fun onSaveClickHandler() {
        val gameInfo = GameInfo()
        gameInfo.id = this.mGameType
        gameInfo.detail = mEtGameDetail.text.toString()
        gameInfo.name = mEtGameName.text.toString()
        val result = mOrmHelper.gameInfoDao.update(gameInfo)
        if (result == 1)
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.btnDelAll)
    internal fun onDeleteAllDataHandler() {
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
                        for (i in child.indices) {
                            CommonUtil.deleteImage(this@GameInfoActivity, child[i])
                        }
                        imagesFileDir.delete()
                    }
                    val eventFileDir = File(
                            Environment.getExternalStorageDirectory(),
                            MConfig.SD_EVENT_PATH + "/" + mGameType)
                    if (eventFileDir.exists()) {
                        val child = eventFileDir.listFiles()
                        for (i in child.indices) {
                            CommonUtil.deleteImage(this@GameInfoActivity, child[i])
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_gameinfo)
        ButterKnife.bind(this)

        mGameType = intent.getIntExtra("game", 0)
        mResourceController = ResourceController(this, mGameType)
        btnAdd = findViewById<View>(R.id.btnGameAdd) as ImageButton
        btnAdd!!.setOnClickListener(onAddBtnClickListerner)
        val gameinfoList = mOrmHelper.gameInfoDao.queryForId(mGameType)
        mEtGameName.setText(gameinfoList.name)
        mEtGameDetail.setText(gameinfoList.detail)
        pageVBoxLayout.visibility = View.GONE

        mEtNumber1.setText(mResourceController!!.number1)
        mEtNumber2.setText(mResourceController!!.number2)
        mEtNumber3.setText(mResourceController!!.number3)
        mEtNumber4.setText(mResourceController!!.number4)
        mEtNumber5.setText(mResourceController!!.number5)

        chkOrientation.isChecked = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false)
        chkOrientationE.isChecked = mSP.getBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameType, false)
        chkImgDate.isChecked = mSP.getBoolean(SHARE_IMAGE_DATE + mGameType, true)
        chkEventGap.isChecked = mResourceController!!.eventImagesGap
        chkHeader.isChecked = mSP.getBoolean(SHARE_SHOW_HEADER_IMAGES + mGameType, false)
        chkCost.isChecked = mSP.getBoolean(SHARE_SHOW_COST_COLUMN + mGameType, false)

        val pagerSize = mSP.getInt(SHARE_PAGE_SIZE + mGameType, 50)
        val pagerArray = resources.getStringArray(R.array.pagerArray)
        var position = 1
        for (i in pagerArray.indices)
            if (Integer.parseInt(pagerArray[i]) == pagerSize)
                position = i
        spinnerPager.setSelection(position)

        mLvGameMain.setOnScrollListener(ListenerListViewScrollHandler(mLvGameMain, pageVBoxLayout))
        mAdapter = CardTypeListAdapter(this, mList)
        mAdapter!!.setDespairTouchListener(despairTouchListener)

        searchGameList()
        searchMain()
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
            mLvGameMain.adapter = mAdapter
        else
            mAdapter!!.notifyDataSetChanged()
    }

}
