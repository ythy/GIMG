package com.mx.gillustrated.activity

import android.Manifest
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
import android.content.pm.PackageManager
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.inputmethod.BaseInputConnection
import android.widget.Toast
import com.mx.gillustrated.databinding.ActivityMainBinding
import com.mx.gillustrated.dialog.FragmentDialogTheme

class MainActivity : BaseActivity() {

    var mGameType = 0 //游戏类别
    private lateinit var mMainActivityHeader: MainActivityHeader
    private lateinit var mMainActivityTop: MainActivityTop
    private lateinit var mMainActivityListView: MainActivityListView
    internal var mainHandler: Handler = MainHandler(this)
    lateinit var binding: ActivityMainBinding

    companion object {
        const val MY_PERMISSIONS_REQUEST = 114
        val permissions:Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)

        private class MainHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {

            private val weakReference: WeakReference<MainActivity> = WeakReference(activity)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val mainActivity:MainActivity = weakReference.get()!!
                when (msg.what) {
                    1 -> {
                        mainActivity.setGameList()
                    }
                    2, 4 -> {
                        val index = msg.what
                        object : Thread() {
                            override fun run() {
                                CommonUtil.generateHeaderImg(mainActivity, mainActivity.mMainActivityListView.idListWithProfile, mainActivity.mGameType, index != 2)
                                mainActivity.mainHandler.sendEmptyMessage(3)
                            }
                        }.start()
                    }
                    3 -> {
                        Toast.makeText(mainActivity, "生成头像完成", Toast.LENGTH_SHORT).show()
                    }
                    5 -> {
                        Toast.makeText(mainActivity, "删除完成", Toast.LENGTH_SHORT).show()
                    }
                    6 -> {
                        mainActivity.mMainActivityTop.setGameList(mainActivity.mGameType,
                                msg.data!!.getParcelableArrayList<GameInfo>("list")!!.toList() )
                    }
                }
            }

        }
    }

    /* test call jni method

    static {
        System.loadLibrary("hello-jni");
    }
    public native String  stringFromJNI();*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mGameType = intent.getIntExtra("game", CommonUtil.getGameType(this))
        requestPermission()
    }

    private fun initListener(){
        binding.btnShowEvents.setOnClickListener {
            val intentEvent = Intent(this@MainActivity, EventsActivity::class.java)
            intentEvent.putExtra("game", mGameType)
            startActivity(intentEvent)
        }
        binding.btnShowMenu.setOnClickListener {
            val mInputConnection = BaseInputConnection(binding.btnShowMenu, true)
            val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU)
            mInputConnection.sendKeyEvent(down)
            val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU)
            mInputConnection.sendKeyEvent(up)
        }
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, AddCardActivity::class.java)
            intent.putExtra("game", mGameType)
            startActivity(intent)
        }
        binding.etPinyin.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val breaks = binding.etPinyin.tag != null && binding.etPinyin.tag as Boolean//防止change事件二次检索
                if (breaks) {
                    binding.etPinyin.tag = false
                    return
                }
                val input = binding.etPinyin.text.toString()
                mMainActivityListView.searchData(input)
            }
            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun init(){
        //Log.d("NATIVE ",  stringFromJNI());
        initListener()
        mMainActivityHeader = MainActivityHeader(this,
                object : MainActivityHeader.HeaderHandle{
                    override fun onHeaderClick(c: String) {
                        mMainActivityListView.searchDataOrderBy(c)
                    }

                },
                mGameType)

        mMainActivityTop = MainActivityTop(this, binding, mOrmHelper, object : MainActivityTop.TopHandle {
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
                binding.etPinyin.tag = true
                binding.etPinyin.setText("")
            }

            override fun onGameTypeChanged(type: Int) {
                mGameType = type
                mMainActivityHeader.setResourceController(mGameType)
            }
        })

        mMainActivityListView = MainActivityListView(this, binding, mOrmHelper, object : MainActivityListView.DataViewHandle {
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
    private fun requestPermission() {
        if (!hasPermission()) {
            requestPermissions(permissions, MY_PERMISSIONS_REQUEST)
        } else
            init()
    }

    private fun hasPermission(): Boolean {
        var result = true
        permissions.forEach {
            val res = checkCallingOrSelfPermission(it)
            if (res != PackageManager.PERMISSION_GRANTED)
                result = false
        }
        return result
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }

    private fun setGameList() {
        Thread{
            ServiceUtils.createConnect(object : DBCall<List<GameInfo>>() {
                override fun enqueue(): List<GameInfo> {
                    return mOrmHelper.gameInfoDao.queryForAll()
                }
            }).doOnNext { list ->
                if (list.isNotEmpty()) {
                    val msg = Message.obtain()
                    msg.data.putParcelableArrayList("list", list as ArrayList<out Parcelable>)
                    msg.what = 6
                    mainHandler.sendMessage(msg)
                } else {
                    DataBakUtil.getDataFromFiles(mOrmHelper)
                    mainHandler.sendEmptyMessage(1)
                }
            }.subscribe()
        }.start()

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
            R.id.menu_delete -> onDeleteListData()
            R.id.action_h5-> {
                val intent = Intent(this, WebActivity::class.java)
                startActivity(intent)
            }
            R.id.action_flutter-> {
                val intent = Intent(this, FlutterEventsActivity::class.java)
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra("game", mGameType)
                intent.putExtra(Intent.EXTRA_TEXT, "MX")
                startActivity(intent)
            }
            R.id.action_game-> {
                val intent = Intent(this, CultivationActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            R.id.action_theme-> {
                val ft = supportFragmentManager.beginTransaction()
                val prev = supportFragmentManager.findFragmentByTag("dialog_theme")
                if (prev != null) {
                    ft.remove(prev)
                }
                ft.addToBackStack(null)
                // Create and show the dialog.
                val newFragment = FragmentDialogTheme.newInstance()
                newFragment.show(ft, "dialog_theme")
            }
        }
        return true
    }

    private fun onDeleteListData() {
        AlertDialog.Builder(this)
                .setMessage("确定要删除吗?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ -> Thread(DeleteRunnable()).start() }
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
                if (child != null){
                    for (i in child.indices) {
                        val name = child[i].name
                        val id = Integer.parseInt(name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(2))
                        if (idList.contains(id))
                            CommonUtil.deleteImage(this@MainActivity, child[i])
                    }
                }
            }

            val imagesHeaderFileDir = File(
                    Environment.getExternalStorageDirectory(),
                    MConfig.SD_HEADER_PATH + "/" + mGameType)
            if (imagesHeaderFileDir.exists()) {
                val child = imagesHeaderFileDir.listFiles()
                if (child != null) {
                    for (i in child.indices) {
                        val name = child[i].name
                        val id = Integer.parseInt(
                            name.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[0]
                        )
                        if (idList.contains(id))
                            CommonUtil.deleteImage(this@MainActivity, child[i])
                    }
                }
            }
            mOrmHelper.cardInfoDao.delCardsById(idList)
            mainHandler.sendEmptyMessage(5)
        }

    }

}

