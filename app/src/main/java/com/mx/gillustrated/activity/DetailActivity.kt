package com.mx.gillustrated.activity

import android.app.Activity
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.ArrayList
import java.util.Calendar
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.CharacterListAdapter
import com.mx.gillustrated.adapter.SpinnerCommonAdapter
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.ResourceController
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.util.UIUtils
import com.mx.gillustrated.vo.CardCharacterInfo
import com.mx.gillustrated.vo.CardEventInfo
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.CardTypeInfo
import com.mx.gillustrated.vo.CharacterInfo
import com.mx.gillustrated.vo.EventInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class DetailActivity : BaseActivity() {

    companion object {
        private const val SELECT_PIC_PROFILE = 30
        private const val SELECT_PIC_LIST = 40
    }

    private lateinit var mUI: UI
    private lateinit var mCardInfo: CardInfo
    private var mId: Int = 0
    private var mMainSearchInfo: Array<String>? = null
    private var mMainSearchOrderBy: String? = null
    private var mCurrentPosition: Int = 0
    private var mMainTotalCount: Int = 0

    private lateinit var mImagesFiles: SparseArray<File>
    private lateinit var mImagesView: SparseArray<View>
    private var mEventList: MutableList<EventInfo> = mutableListOf()
    private var mResourceController: ResourceController? = null
    private var mCharListAdapter: CharacterListAdapter? = null
    private var mCharListData: MutableList<CharacterInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        ButterKnife.bind(this)

        val intent = intent
        mId = intent.getIntExtra("card", 0)
        mMainSearchInfo = intent.getStringArrayExtra("cardSearchCondition")
        mMainSearchOrderBy = intent.getStringExtra("orderBy")
        mCurrentPosition = intent.getIntExtra("positon", -1)
        mMainTotalCount = intent.getIntExtra("totalCount", 0)
        mCardInfo = mOrmHelper.cardInfoDao.queryForId(mId)
        mResourceController = ResourceController(this, mCardInfo.gameId)
        mUI = UI(this)
        UIEvent(this)

        mUI.tvHeaderNumber1.text = mResourceController!!.number1
        mUI.tvHeaderNumber2.text = mResourceController!!.number2
        mUI.tvHeaderNumber3.text = mResourceController!!.number3
        mUI.tvHeaderNumber4.text = mResourceController!!.number4
        mUI.tvHeaderNumber5.text = mResourceController!!.number5

        val cardTypes = mOrmHelper.cardTypeInfoDao.queryForEq(CardInfo.COLUMN_GAMETYPE, mCardInfo.gameId)
        val adapterName = SpinnerCommonAdapter(this, cardTypes)
        mUI.spinnerAttr.adapter = adapterName

        val mBtnNext = findViewById<View>(R.id.btnNext) as ImageButton
        mBtnNext.setOnClickListener { searchCardSide(1) }
        val mBtnLast = findViewById<View>(R.id.btnLast) as ImageButton
        mBtnLast.setOnClickListener { searchCardSide(-1) }
    }

    override fun onResume() {
        super.onResume()
        initChar()
        showEvents()
        showCardInfo()
    }

    private fun initCharDataAdapter() {
        mCharListData = ArrayList()
        val list = mOrmHelper.cardCharacterInfoDao.getListByCardId(mId)
        for (i in list!!.indices) {
            val info = mOrmHelper.characterInfoDao.queryForId(list[i].charId)
            mCharListData!!.add(info)
        }

        val associationId = mSP.getInt(SHARE_ASSOCIATION_GAME_ID + this.mCardInfo.gameId, 0)
        var cardLists: List<CardInfo>? = null
        if (associationId > 0) {
            cardLists = mOrmHelper.cardInfoDao.queryCards(CardInfo(associationId), CardInfo.ID, true, 0, 1000)
        }

        this.mCharListAdapter = CharacterListAdapter(this, mCharListData!!, cardLists)
        mCharListAdapter!!.setCharacterTouchListener(object : CharacterListAdapter.CharacterTouchListener {
            override fun onSaveBtnClickListener(info: CharacterInfo, index: Int) {
                mOrmHelper.characterInfoDao.createOrUpdate(info)
                val result = mOrmHelper.cardCharacterInfoDao.addCardCharacter(CardCharacterInfo(mId, info.id))
                if (result)
                    Toast.makeText(applicationContext, "新增角色成功", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(applicationContext, "修改角色成功", Toast.LENGTH_SHORT).show()

                initCharDataAdapter()
            }

            override fun onDelBtnClickListener(info: CharacterInfo, index: Int) {
                AlertDialog.Builder(this@DetailActivity)
                        .setMessage("确定要删除吗")
                        .setPositiveButton("Ok"
                        ) { _, _ ->
                            mCharListData!!.removeAt(index)
                            if (info.id > 0) {
                                mOrmHelper.cardCharacterInfoDao.delCardChar(CardCharacterInfo(mId, info.id))
                            }
                            Toast.makeText(applicationContext, "删除成功", Toast.LENGTH_SHORT).show()
                            mCharListAdapter!!.notifyDataSetChanged()
                            setListViewHeightBasedOnItems()
                        }.setNegativeButton("Cancel", null).show()
            }
        })
        mUI.mListChar.adapter = mCharListAdapter
        setListViewHeightBasedOnItems()
    }

    private fun initChar() {
        initCharDataAdapter()
    }

    private fun addChar() {
        val characterInfo = CharacterInfo()
        characterInfo.gameId = this.mCardInfo.gameId
        mCharListData!!.add(characterInfo)
        mCharListAdapter!!.notifyDataSetChanged()
        setListViewHeightBasedOnItems()
    }

    fun setListViewHeightBasedOnItems() {

        val numberOfItems = mCharListAdapter!!.count
        // Get total height of all items.
        var totalItemsHeight = 0
        for (itemPos in 0 until numberOfItems) {
            val item = mCharListAdapter!!.getView(itemPos, null, mUI.mListChar)
            val px = 500 * mUI.mListChar.resources.displayMetrics.density
            item.measure(View.MeasureSpec.makeMeasureSpec(px.toInt(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            totalItemsHeight += item.measuredHeight
        }

        // Get total height of all item dividers.
        val totalDividersHeight = mUI.mListChar.dividerHeight * (numberOfItems - 1)
        // Get padding
        val totalPadding = mUI.mListChar.paddingTop + mUI.mListChar.paddingBottom

        // Set list height.
        val params = mUI.mListChar.layoutParams
        params.height = totalItemsHeight + totalDividersHeight + totalPadding
        mUI.mListChar.layoutParams = params
        mUI.mListChar.requestLayout()
    }

    private fun showEvents() {
        mUI.llShowEvent.removeAllViews()
        mEventList = mOrmHelper.eventInfoDao.getListByGameId(mCardInfo.gameId, "Y")
        mEventList.add(0, EventInfo(""))

        val events = mOrmHelper.cardEventInfoDao.getListByCardId(mCardInfo.id)
        for (i in events!!.indices) {
            if (isExistInEvent(events[i].eventId)) {
                val spinner = addEvent()
                CommonUtil.setSpinnerItemSelectedByValue2(spinner!!, events[i].eventId.toString())
            }
        }
    }

    private fun isExistInEvent(id: Int): Boolean {
        for (i in mEventList.indices) {
            if (mEventList[i].id == id)
                return true
        }
        return false
    }

    private fun searchCardSide(type: Int) {
        var newPositon = mCurrentPosition
        newPositon += type
        if (newPositon < 0 || newPositon >= mMainTotalCount) {
            Toast.makeText(baseContext, "顶端/底端", Toast.LENGTH_SHORT).show()
            return
        }
        val order = mMainSearchOrderBy!!.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val isDesc = mMainSearchOrderBy!!.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] == CardInfo.SORT_ASC
        val result = mOrmHelper.cardInfoDao.queryCards(CardInfo(mMainSearchInfo!!), order, isDesc, newPositon.toLong(), 1L)!![0]

        if (result != null) {
            mCurrentPosition = newPositon
            mCardInfo = result
            mId = mCardInfo.id
            initChar()
            showEvents()
            showCardInfo()
        }
    }

    private fun showCardInfo() {
        val info = mCardInfo
        mUI.etHP.setText(info.maxHP)
        mUI.etAttack.setText(info.maxAttack)
        mUI.etDefense.setText(info.maxDefense)
        mUI.etExtra1.setText(info.extraValue1)
        mUI.etExtra2.setText(info.extraValue2)
        mUI.etName.setText(info.name)
        mUI.etFrontName.setText(info.frontName)
        mUI.etDetail.setText(info.remark)
        mUI.etNid.setText(info.nid.toString())
        val attr = info.attrId.toString()
        CommonUtil.setSpinnerItemSelectedByValue2(mUI.spinnerAttr, attr)

        CommonUtil.setSpinnerItemSelectedByValue(mUI.spinnerLevel,
                info.level.toString())

        mUI.etCost.setText(info.cost.toString())
        mUI.tvId.text = info.id.toString()

        mUI.chkProfile.isChecked = "Y" == info.profile

        mImagesFiles = SparseArray()
        mImagesView = SparseArray()
        mUI.mLLImages.removeAllViews()
        showImages()
    }

    private fun showImages() {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_PATH + "/" + mCardInfo.gameId)
            var index = 0
            while (++index < 20) {
                val imageFile = File(fileDir.path, CommonUtil.getImageFrontName(mId, index))
                var bitmap: Bitmap? = null
                if (imageFile.exists()) {
                    mImagesFiles.append(index, imageFile)
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver, Uri.fromFile(imageFile))
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val child = LayoutInflater.from(this@DetailActivity).inflate(
                            R.layout.child_images_gap, mUI.mLLImages, false)
                    mUI.mLLImages.addView(child)
                    mImagesView.append(index, child)

                    val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mCardInfo.gameId, false)
                    val isShowImageDate = mSP.getBoolean(SHARE_IMAGE_DATE + mCardInfo.gameId, true)
                    val tvDate = child.findViewById<View>(R.id.tvDate) as TextView
                    if (isShowImageDate)
                        tvDate.text = CommonUtil.getFileLastModified(imageFile)
                    val image = child.findViewById<View>(R.id.imgDetails) as ImageView
                    image.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bitmap!!, 90) else bitmap)
                    val oldIndex = index
                    val btnAdjust = child.findViewById<View>(R.id.btnAdjust) as ImageButton
                    btnAdjust.setOnClickListener {
                        val intent = Intent(this@DetailActivity, ImageAdjustActivity::class.java)
                        intent.putExtra("source", mImagesFiles.get(oldIndex).absolutePath)
                        startActivity(intent)
                    }
                    val btnDel = child.findViewById<View>(R.id.btnDel) as ImageButton
                    btnDel.tag = "$index*0"
                    btnDel.setOnClickListener { v ->
                        val tag = v.tag.toString().split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val key = Integer.parseInt(tag[0])
                        val line = mImagesView.get(key)
                        val timenow = Calendar.getInstance().time.time
                        if (Math.abs(timenow - java.lang.Long.valueOf(tag[1])) > 5000) {
                            Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                            v.tag = "$key*$timenow"
                        } else {
                            v.tag = "$key*0"
                            CommonUtil.deleteImage(this@DetailActivity, mImagesFiles.get(key))
                            mUI.mLLImages.removeView(line)
                            mImagesView.remove(key)
                            mImagesFiles.remove(key)
                            Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun addEvent(): Spinner? {
        val child = LayoutInflater.from(this@DetailActivity).inflate(
                R.layout.child_event, mUI.llShowEvent, false)
        mUI.llShowEvent.addView(child)

        val event = InlineEvent(child)
        UIUtils.setSpinnerSingleClick(event.spinner)
        val adapter = SpinnerCommonAdapter(this@DetailActivity, mEventList)
        event.spinner.adapter = adapter

        event.btnDetail.setOnClickListener {
            val info = event.spinner.selectedItem as EventInfo
            val intent = Intent(this@DetailActivity, EventInfoActivity::class.java)
            intent.putExtra("event", info.id)
            intent.putExtra("game", mCardInfo.gameId)
            startActivity(intent)
        }


        event.btnDel.tag = 0

        event.btnDel.setOnClickListener { v ->
            val tag = v.tag.toString()
            val info = event.spinner.selectedItem as EventInfo
            val id = info.id
            if (id == 0) {
                mUI.llShowEvent.removeView(child)
            } else {
                val timeNow = Calendar.getInstance().time.time
                if (Math.abs(timeNow - java.lang.Long.valueOf(tag)) > 5000) {
                    Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                    v.tag = timeNow
                } else {
                    v.tag = 0
                    mOrmHelper.cardEventInfoDao.delCardEvents(CardEventInfo(mCardInfo.id, id))
                    mUI.llShowEvent.removeView(child)
                    Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return event.spinner
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PIC_LIST || requestCode == SELECT_PIC_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data
                val projections = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(uri!!,
                        projections, null, null, null)
                try {
                    if (cursor != null) {
                        val cr = contentResolver
                        val colIndex = cursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor.moveToFirst()
                        val path = cursor.getString(colIndex)

                        if (path.toLowerCase().endsWith("jpg") || path.toLowerCase().endsWith("png") ||
                                path.toLowerCase().endsWith("jpeg")) {
                            val bitmap = BitmapFactory.decodeStream(cr
                                    .openInputStream(uri))
                            if (requestCode == SELECT_PIC_LIST) {
                                createImages(bitmap, false)
                                showImages()
                            } else
                                createImages(bitmap, true)
                        } else {
                            alert()
                        }
                    } else {
                        alert()
                    }

                } catch (e: Exception) {
                } finally {
                    cursor?.close()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun alert() {
        Toast.makeText(this, "您选择的不是有效的图片", Toast.LENGTH_SHORT).show()
    }

    private fun createImages(bitmap: Bitmap, isHeader: Boolean) {
        val mImagesFileDir = File(
                Environment.getExternalStorageDirectory(),
                (if (isHeader) MConfig.SD_HEADER_PATH else MConfig.SD_PATH) + "/" + mCardInfo.gameId)
        if (!mImagesFileDir.exists()) {
            mImagesFileDir.mkdirs()
        }
        var imageFile: File
        val bos: FileOutputStream
        if (isHeader) {
            imageFile = File(mImagesFileDir.path, mId.toString() + "_h.png")
        } else {
            var checknum = 1
            while (true) {
                imageFile = File(mImagesFileDir.path,
                        CommonUtil.getImageFrontName(mId, checknum))
                if (!imageFile.exists())
                    break
                else
                    checknum++
            }
        }
        try {
            bos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG,
                    100, bos)
            bos.flush()
            bos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            Toast.makeText(this, "图片创建完成", Toast.LENGTH_SHORT).show()
        }
    }

    fun setCardInfoFromPreviousCard() {
        if (mId > 1) {
            val preCard = mOrmHelper.cardInfoDao.queryForId(mId - 1)
            mUI.etFrontName.setText(preCard.frontName)
            mUI.etDetail.setText(preCard.remark)
            mUI.etHP.setText(preCard.maxHP)
            mUI.etAttack.setText(preCard.maxAttack)
            mUI.etDefense.setText(preCard.maxDefense)
            mUI.etExtra1.setText(preCard.extraValue1)
            mUI.etExtra2.setText(preCard.extraValue2)
            CommonUtil.setSpinnerItemSelectedByValue2(mUI.spinnerAttr, preCard.attrId.toString())
            val preEvents = mOrmHelper.cardEventInfoDao.getListByCardId(mId - 1)
            if (preEvents != null && preEvents.isNotEmpty()) {
                mUI.llShowEvent.removeAllViews()
                for (i in preEvents.indices) {
                    if (isExistInEvent(preEvents[i].eventId)) {
                        val spinner = addEvent()
                        CommonUtil.setSpinnerItemSelectedByValue2(spinner!!, preEvents[i].eventId.toString())
                    }
                }
            }
        }
    }

    fun saveCard() {
        var card: CardInfo
        var result: Long = 0
        // 名称优先批量更新
        if (!mUI.chkModify.isChecked
                && mCardInfo.name != ""
                && mCardInfo.name != mUI.etName.text.toString().trim { it <= ' ' }) {
            val cardOld = mCardInfo
            card = CardInfo()
            card.name = mUI.etName.text.toString().trim { it <= ' ' }
            card.pinyinName = PinyinUtil.convert(card.name!!)
            result = mOrmHelper.cardInfoDao.updateCardName(card, cardOld)
        }

        card = CardInfo()
        card.id = mId
        card.nid = Integer.parseInt(mUI.etNid.text.toString())
        card.gameId = mCardInfo.gameId
        val cardTypeInfo = mUI.spinnerAttr.selectedItem as CardTypeInfo
        card.attrId = cardTypeInfo.id
        card.level = mUI.spinnerLevel.selectedItem.toString()

        card.cost = if (mUI.etCost.text.toString().trim { it <= ' ' } == "")
            0
        else
            Integer.parseInt(mUI.etCost.text.toString())
        card.name = mUI.etName.text.toString().trim { it <= ' ' }
        card.pinyinName = PinyinUtil.convert(card.name!!)
        card.frontName = mUI.etFrontName.text.toString().trim { it <= ' ' }
        card.remark = mUI.etDetail.text.toString().trim { it <= ' ' }
        card.profile = if (mUI.chkProfile.isChecked) "Y" else "N"
        card.maxHP = mUI.etHP.text.toString().trim { it <= ' ' }
        card.maxAttack = mUI.etAttack.text.toString().trim { it <= ' ' }
        card.maxDefense = mUI.etDefense.text.toString().trim { it <= ' ' }
        card.extraValue1 = mUI.etExtra1.text.toString().trim { it <= ' ' }
        card.extraValue2 = mUI.etExtra2.text.toString().trim { it <= ' ' }
        result += mOrmHelper.cardInfoDao.update(card).toLong()

        if (result > 0) {
            val intent = Intent(this,
                    MainActivity::class.java)
            intent.putExtra("game", mCardInfo.gameId)
            intent.putExtra("orderBy", mMainSearchOrderBy)
            intent.putExtra("spinnerIndexs", intent.getStringExtra("spinnerIndexs"))
            intent.putExtra("position", mCurrentPosition)
            intent.putExtra("currentPage", intent.getIntExtra("currentPage", 1))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            this.startActivity(intent)
            this.finish()
        } else
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT)
                    .show()
    }

    fun removeCard() {
        AlertDialog.Builder(this)
                .setMessage("确定要删除吗")
                .setPositiveButton("Ok"
                ) { _, _ ->
                    val result = mOrmHelper.cardInfoDao.deleteById(mCardInfo.id).toLong()
                    if (result != -1L) {

                        for (i in 0 until mImagesFiles.size()) {
                            CommonUtil.deleteImage(this,
                                    mImagesFiles.get(mImagesFiles.keyAt(i)))
                        }

                        val intent = Intent(
                                this,
                                MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        this.startActivity(intent)
                        this.finish()
                    } else
                        Toast.makeText(this,
                                "删除失败", Toast.LENGTH_SHORT)
                                .show()
                }.setNegativeButton("Cancel", null).show()
    }

    internal class InlineEvent(view: View) {

        @BindView(R.id.spinnerEvent)
        lateinit var spinner: Spinner

        @BindView(R.id.btnDetail)
        lateinit var btnDetail: ImageButton

        @BindView(R.id.btnDel)
        lateinit var btnDel: ImageButton

        init {
            ButterKnife.bind(this, view)
        }

    }

    class UI(view: BaseActivity) {
        init {
            ButterKnife.bind(this, view)
        }

        @BindView(R.id.etDetailExtra1)
        lateinit var etExtra1: EditText

        @BindView(R.id.etDetailExtra2)
        lateinit var etExtra2: EditText

        @BindView(R.id.tv_header_hp)
        lateinit var tvHeaderNumber1: TextView

        @BindView(R.id.tv_header_A)
        lateinit var tvHeaderNumber2: TextView

        @BindView(R.id.tv_header_D)
        lateinit var tvHeaderNumber3: TextView

        @BindView(R.id.tv_header_E1)
        lateinit var tvHeaderNumber4: TextView

        @BindView(R.id.tv_header_E2)
        lateinit var tvHeaderNumber5: TextView

        @BindView(R.id.chkProfile)
        lateinit var chkProfile: CheckBox

        @BindView(R.id.llShowEvent)
        lateinit var llShowEvent: LinearLayout

        @BindView(R.id.scrollView)
        lateinit var mScrollView: ScrollView

        @BindView(R.id.lvChar)
        lateinit var mListChar: ListView

        @BindView(R.id.etDetailHP)
        lateinit var etHP: EditText

        @BindView(R.id.etDetailAttack)
        lateinit var etAttack: EditText

        @BindView(R.id.etDetailDefense)
        lateinit var etDefense: EditText

        @BindView(R.id.etDetailName)
        lateinit var etName: EditText

        @BindView(R.id.etDetailFrontName)
        lateinit var etFrontName: EditText

        @BindView(R.id.etDetail)
        lateinit var etDetail: EditText

        @BindView(R.id.etDetailNid)
        lateinit var etNid: EditText

        @BindView(R.id.tvId)
        lateinit var tvId: TextView

        @BindView(R.id.spinnerAttr)
        lateinit var spinnerAttr: Spinner

        @BindView(R.id.spinnerLevel)
        lateinit var spinnerLevel: Spinner

        @BindView(R.id.etDetailCost)
        lateinit var etCost: EditText

        @BindView(R.id.chkModify)
        lateinit var chkModify: CheckBox

        @BindView(R.id.llImages)
        lateinit var mLLImages: LinearLayout


    }

    class UIEvent(activity: DetailActivity) {

        init {
            ButterKnife.bind(this, activity)
        }

        private val weak: WeakReference<DetailActivity> = WeakReference(activity)

        @OnClick(R.id.btnRead)
        fun onBtnReadHandler() {
            weak.get()!!.setCardInfoFromPreviousCard()
        }

        @OnClick(R.id.btnSave, R.id.btnSave2)
        fun onBtnSaveHandler() {
            weak.get()!!.saveCard()
        }

        @OnClick(R.id.btnDel)
        fun btnDelClickListener() {
            weak.get()!!.removeCard()
        }

        @OnClick(R.id.btnDel2)
        fun btnDel2ClickListener() {
            val activity = weak.get()!!
            for (i in 0 until activity.mImagesView.size()) {
                val btnDel = activity.mImagesView.valueAt(i)?.findViewById<View>(R.id.btnDel) as ImageButton
                btnDel.visibility = View.VISIBLE
                val btnAjust = activity.mImagesView.valueAt(i)?.findViewById<View>(R.id.btnAdjust) as ImageButton
                btnAjust.visibility = View.VISIBLE
            }
        }

        @OnClick(R.id.btnAddChar)
        internal fun onAddCharClickHandler() {
            val activity = weak.get()!!
            activity.addChar()
            activity.mUI.mScrollView.post { activity.mUI.mScrollView.smoothScrollTo(0, 5000) }
        }


        @OnClick(R.id.btnAddEvent)
        internal fun onAddEventClickHandler() {
            val activity = weak.get()!!
            activity.addEvent()
            activity.mUI.mScrollView.post { activity.mUI.mScrollView.smoothScrollTo(0, 5000) }
        }

        @OnClick(R.id.btnSaveEvent)
        internal fun onSaveEvnetClickHandler() {
            val activity = weak.get()!!
            val events = ArrayList<CardEventInfo>()
            try {
                for (i in 0 until activity.mUI.llShowEvent.childCount ) {
                    val spinner =  activity.mUI.llShowEvent.getChildAt(i).findViewById<View>(R.id.spinnerEvent) as Spinner
                    val info = spinner.selectedItem as EventInfo
                    events.add(CardEventInfo(activity.mCardInfo.id, info.id))
                }
                activity.mOrmHelper.cardEventInfoDao.addCardEvents(events)
                Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        @OnClick(R.id.btnSetProfile)
        internal fun onSetProfileClickHandler() {
            val activity = weak.get()!!
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            activity.startActivityForResult(intent, SELECT_PIC_PROFILE)
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        @OnClick(R.id.btnAddImage)
        internal fun onAddImageClickHandler() {
            val activity = weak.get()!!
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            activity.startActivityForResult(intent, SELECT_PIC_LIST)
        }

    }

}
