package com.mx.gillustrated.activity

import java.io.File
import java.lang.ref.WeakReference
import java.util.ArrayList
import com.mx.gillustrated.common.DBCall
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.MainActivityHeader
import com.mx.gillustrated.component.MainActivityListView
import com.mx.gillustrated.component.MainActivityTop
import com.mx.gillustrated.dialog.DialogExportImg
import com.mx.gillustrated.R
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.DataBakUtil
import com.mx.gillustrated.util.ServiceUtils
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.GameInfo
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged

class MainActivity : BaseActivity() {

    var mGameType = 0 //游戏类别
    private lateinit var mMainActivityHeader: MainActivityHeader
    private lateinit var mMainActivityTop: MainActivityTop
    private lateinit var mMainActivityListView: MainActivityListView
    internal var mainHandler: Handler = MainHandler(this)

    companion object {

        private class MainHandler(activity: MainActivity) : Handler() {

            private val weakReference: WeakReference<MainActivity> = WeakReference(activity)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val mainActivity:MainActivity = weakReference.get()!!
                if (msg.what == 1) {
                    mainActivity.setGameList()
                } else if (msg.what == 2 || msg.what == 4) {
                    val index = msg.what
                    object : Thread() {
                        override fun run() {
                            CommonUtil.generateHeaderImg(mainActivity, mainActivity.mMainActivityListView.idListWithProfile, mainActivity.mGameType, index != 2)
                            mainActivity.mainHandler.sendEmptyMessage(3)
                        }
                    }.start()
                } else if (msg.what == 3) {
                    Toast.makeText(mainActivity, "生成头像完成", Toast.LENGTH_SHORT).show()
                } else if (msg.what == 5) {
                    Toast.makeText(mainActivity, "删除完成", Toast.LENGTH_SHORT).show()

                }
            }

        }
    }



    @JvmField
    @BindView(R.id.etPinyin)
    internal var etPinyin: EditText? = null

    @JvmField
    @BindView(R.id.btnShowMenu)
    internal var btnMenu: ImageButton? = null

    @OnClick(R.id.btnShowEvents)
    internal fun onBtnShowEventsClick() {
        val intentEvent = Intent(this@MainActivity, EventsActivity::class.java)
        intentEvent.putExtra("game", mGameType)
        startActivity(intentEvent)
    }

    @OnClick(R.id.btnShowMenu)
    internal fun onBtnShowMenuClick() {
        val mInputConnection = BaseInputConnection(btnMenu, true)
        val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU)
        mInputConnection.sendKeyEvent(down)
        val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU)
        mInputConnection.sendKeyEvent(up)
    }

    @OnTextChanged(R.id.etPinyin)
    internal fun onEtPinyinChanged() {
        val breaks = etPinyin!!.tag != null && etPinyin!!.tag as Boolean//防止change事件二次检索
        if (breaks) {
            etPinyin!!.tag = false
            return
        }
        val input = etPinyin!!.text.toString()
        mMainActivityListView.searchData(input)
    }

    @OnClick(R.id.btnAdd)
    internal fun btnAddClickListener() {
        val intent = Intent(this@MainActivity, AddCardActivity::class.java)
        intent.putExtra("game", mGameType)
        startActivity(intent)
    }

    /* test call jni method

    static {
        System.loadLibrary("hello-jni");
    }
    public native String  stringFromJNI();*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        mGameType = intent.getIntExtra("game", CommonUtil.getGameType(this))
        ButterKnife.bind(this)

        //Log.d("NATIVE ",  stringFromJNI());

        mMainActivityHeader = MainActivityHeader(this,
                object : MainActivityHeader.HeaderHandle{
                    override fun onHeaderClick(c: String) {
                        mMainActivityListView.searchDataOrderBy(c)
                    }

                },
                mGameType)

        mMainActivityTop = MainActivityTop(this, mOrmHelper, object : MainActivityTop.TopHandle {
            override fun onSearchData() {
                mMainActivityHeader.setHeaderColor(mMainActivityListView.orderBy.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                val card = mMainActivityTop.spinnerInfo
                if (card != null) {
                    card.gameId = mGameType
                    mMainActivityListView.searchData(card)
                }
            }

            override fun onRefresh() {
                mMainActivityListView.setOrderBy(null, null)
                etPinyin!!.tag = true
                etPinyin!!.setText("")
            }

            override fun onGameTypeChanged(type: Int) {
                mGameType = type
                mMainActivityHeader.setResourceController(mGameType)
            }
        })

        mMainActivityListView = MainActivityListView(this, mOrmHelper, object : MainActivityListView.DataViewHandle {
            override fun onListItemClick(info: CardInfo, position: Int, totalCount: Int, currentPage: Int) {
                val intent = Intent(this@MainActivity,
                        DetailActivity::class.java)
                intent.putExtra("card", info.id)
                intent.putExtra("cardSearchCondition", mMainActivityListView.searchCondition.cardSearchParam)
                intent.putExtra("orderBy", mMainActivityListView.orderBy)
                intent.putExtra("positon", position)
                intent.putExtra("totalCount", totalCount)
                intent.putExtra("currentPage", currentPage)
                intent.putExtra("spinnerIndexs", mMainActivityTop.spinnerSelectedIndexes)
                startActivity(intent)
            }

            override fun onSearchCompleted() {
                mProgressDialog.dismiss()
            }

            override fun onSearchStart() {
                mProgressDialog.show()
            }
        })


        setGameList()

        //temp
        //		File fileDirTemp = new File(Environment.getExternalStorageDirectory(),
        //				"backup");
        //		File fileTemp = new File(fileDirTemp.getPath(), "all1.docx");
        //		 try {
        //			CommonUtil.copyBigDataToSD(this, "all1.docx", fileTemp.getPath());
        //		} catch (IOException e) {
        //			e.printStackTrace();
        //		}

    }


    private fun setGameList() {
        ServiceUtils.createConnect(object : DBCall<List<GameInfo>>() {
            override fun enqueue(): List<GameInfo> {
                return mOrmHelper.gameInfoDao.queryForAll()
            }
        }).doOnNext { list ->
            if (list.isNotEmpty()) {
                mMainActivityTop.setGameList(mGameType, list)
            } else {
                object : Thread() {
                    override fun run() {
                        DataBakUtil.getDataFromFiles(mOrmHelper)
                        mainHandler.sendEmptyMessage(1)
                    }
                }.start()
            }
        }.subscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_out -> {
                DataBakUtil.saveDataToFiles(mOrmHelper)
                Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_gamelist -> {
                val intent = Intent(this@MainActivity, GameListActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_header -> DialogExportImg.show(this, mMainActivityListView.dataList[0].id, mGameType, mainHandler)
            R.id.menu_eventlist -> onBtnShowEventsClick()
            R.id.menu_delete -> onDeleteListData()
        }
        return true
    }

    private fun onDeleteListData() {
        AlertDialog.Builder(this)
                .setMessage("确定要删除吗?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { _, _ -> Thread(DeleteRunnable()).start() }
                .show()
    }

    private inner class DeleteRunnable : Runnable {

        override fun run() {
            val list = mMainActivityListView.dataList
            val idList = ArrayList<Int>()
            for (info in list)
                idList.add(info.id)

            val imagesFileDir = File(
                    Environment.getExternalStorageDirectory(),
                    MConfig.SD_PATH + "/" + mGameType)
            if (imagesFileDir.exists()) {
                val child = imagesFileDir.listFiles()
                for (i in child.indices) {
                    val name = child[i].name
                    val id = Integer.parseInt(name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(2))
                    if (idList.contains(id))
                        CommonUtil.deleteImage(this@MainActivity, child[i])
                }
            }

            val imagesHeaderFileDir = File(
                    Environment.getExternalStorageDirectory(),
                    MConfig.SD_HEADER_PATH + "/" + mGameType)
            if (imagesHeaderFileDir.exists()) {
                val child = imagesHeaderFileDir.listFiles()
                for (i in child.indices) {
                    val name = child[i].name
                    val id = Integer.parseInt(name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                    if (idList.contains(id))
                        CommonUtil.deleteImage(this@MainActivity, child[i])
                }
            }
            mOrmHelper.cardInfoDao.delCardsById(idList)
            mainHandler.sendEmptyMessage(5)
        }

    }

}

