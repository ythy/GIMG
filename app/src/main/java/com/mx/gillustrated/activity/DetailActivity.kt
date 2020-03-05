package com.mx.gillustrated.activity

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
import android.net.Uri
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
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

class DetailActivity : BaseActivity() {

    private var etHP: EditText? = null
    private var etAttack: EditText? = null
    private var etDefense: EditText? = null
    private var etName: EditText? = null
    private var etFrontName: EditText? = null
    private var etDetail: EditText? = null
    private var etNid: EditText? = null
    private var mCardInfo: CardInfo? = null
    private var spinnerAttr: Spinner? = null
    private var spinnerLevel: Spinner? = null
    private var etCost: EditText? = null
    private var chkModify: CheckBox? = null
    private var tvId: TextView? = null
    private var mId: Int = 0

    private var mMainSearchInfo: Array<String>? = null
    private var mMainSearchOrderBy: String? = null
    private var mCurrentPosition: Int = 0
    private var mMainTotalCount: Int = 0

    private var mImagesFiles: SparseArray<File>? = null
    private var mImagesView: SparseArray<View>? = null
    private var mLLImages: LinearLayout? = null
    private var mEventList: MutableList<EventInfo>? = null
    private val mEventView = SparseArray<View>()
    private var mResourceController: ResourceController? = null

    private var mCharListAdapter: CharacterListAdapter? = null
    private var mCharListData: MutableList<CharacterInfo>? = null


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

    private var btnSaveClickListener: View.OnClickListener = View.OnClickListener {
        var card: CardInfo
        var result: Long = 0
        // 名称优先批量更新
        if (!chkModify!!.isChecked
                && mCardInfo!!.name != ""
                && mCardInfo!!.name != etName!!.text.toString().trim { it <= ' ' }) {
            val cardOld = mCardInfo
            card = CardInfo()
            card.name = etName!!.text.toString().trim { it <= ' ' }
            card.pinyinName = PinyinUtil.convert(card.name)
            result = mOrmHelper.cardInfoDao.updateCardName(card, cardOld)
        }

        card = CardInfo()
        card.id = mId
        card.nid = Integer.parseInt(etNid!!.text.toString())
        card.gameId = mCardInfo!!.gameId
        val cardTypeInfo = spinnerAttr!!.selectedItem as CardTypeInfo
        card.attrId = cardTypeInfo.id
        card.level = spinnerLevel!!.selectedItem.toString()

        card.cost = if (etCost!!.text.toString().trim { it <= ' ' } == "")
            0
        else
            Integer.parseInt(etCost!!.text.toString())
        card.name = etName!!.text.toString().trim { it <= ' ' }
        card.pinyinName = PinyinUtil.convert(card.name)
        card.frontName = etFrontName!!.text.toString().trim { it <= ' ' }
        card.remark = etDetail!!.text.toString().trim { it <= ' ' }
        card.profile = if (chkProfile.isChecked) "Y" else "N"
        card.maxHP = etHP!!.text.toString().trim { it <= ' ' }
        card.maxAttack = etAttack!!.text.toString().trim { it <= ' ' }
        card.maxDefense = etDefense!!.text.toString().trim { it <= ' ' }
        card.extraValue1 = etExtra1.text.toString().trim { it <= ' ' }
        card.extraValue2 = etExtra2.text.toString().trim { it <= ' ' }
        result += mOrmHelper.cardInfoDao.update(card).toLong()

        if (result > 0) {
            val intent = Intent(this@DetailActivity,
                    MainActivity::class.java)
            intent.putExtra("game", mCardInfo!!.gameId)
            intent.putExtra("orderBy", mMainSearchOrderBy)
            intent.putExtra("spinnerIndexs", getIntent().getStringExtra("spinnerIndexs"))
            intent.putExtra("position", mCurrentPosition)
            intent.putExtra("currentPage", getIntent().getIntExtra("currentPage", 1))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this@DetailActivity.finish()
        } else
            Toast.makeText(this@DetailActivity, "保存失败", Toast.LENGTH_SHORT)
                    .show()
    }

    private var btnDelClickListener: View.OnClickListener = View.OnClickListener {
        AlertDialog.Builder(this@DetailActivity)
                .setMessage("确定要删除吗")
                .setPositiveButton("Ok"
                ) { _, _ ->
                    val result = mOrmHelper.cardInfoDao.deleteById(mCardInfo!!.id).toLong()
                    if (result != -1L) {

                        for (i in 0 until mImagesFiles!!.size()) {
                            CommonUtil.deleteImage(this@DetailActivity,
                                    mImagesFiles!!.get(mImagesFiles!!.keyAt(i)))
                        }

                        val intent = Intent(
                                this@DetailActivity,
                                MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        this@DetailActivity.finish()
                    } else
                        Toast.makeText(this@DetailActivity,
                                "删除失败", Toast.LENGTH_SHORT)
                                .show()
                }.setNegativeButton("Cancel", null).show()
    }

    private var btnDel2ClickListener: View.OnClickListener = View.OnClickListener {
        for (i in 0 until mImagesView!!.size()) {
            val btnDel = mImagesView!!.valueAt(i).findViewById<View>(R.id.btnDel) as ImageButton
            btnDel.visibility = View.VISIBLE
            val btnAjust = mImagesView!!.valueAt(i).findViewById<View>(R.id.btnAdjust) as ImageButton
            btnAjust.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.btnAddChar)
    internal fun onAddCharClickHandler() {
        addChar()
        mScrollView.post { mScrollView.smoothScrollTo(0, 5000) }
    }


    @OnClick(R.id.btnAddEvent)
    internal fun onAddEventClickHandler() {
        addEvent()
        mScrollView.post { mScrollView.smoothScrollTo(0, 5000) }
    }

    @OnClick(R.id.btnSaveEvent)
    internal fun onSaveEvnetClickHandler() {
        val events = ArrayList<CardEventInfo>()
        for (i in 0 until mEventView.size()) {
            val spinner = mEventView.get(mEventView.keyAt(i)).findViewById<View>(R.id.spinnerEvent) as Spinner
            val info = spinner.selectedItem as EventInfo
            events.add(CardEventInfo(mCardInfo!!.id, info.id))
        }
        mOrmHelper.cardEventInfoDao.addCardEvents(events)
        Toast.makeText(this@DetailActivity, "保存成功", Toast.LENGTH_SHORT).show()
    }

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
        mResourceController = ResourceController(this, mCardInfo!!.gameId)

        chkModify = findViewById<View>(R.id.chkModify) as CheckBox
        etHP = findViewById<View>(R.id.etDetailHP) as EditText
        etAttack = findViewById<View>(R.id.etDetailAttack) as EditText
        etDefense = findViewById<View>(R.id.etDetailDefense) as EditText
        etName = findViewById<View>(R.id.etDetailName) as EditText
        etFrontName = findViewById<View>(R.id.etDetailFrontName) as EditText
        etNid = findViewById<View>(R.id.etDetailNid) as EditText
        etDetail = findViewById<View>(R.id.etDetail) as EditText

        val btnSave = findViewById<View>(R.id.btnSave) as ImageButton
        val btnSave2 = findViewById<View>(R.id.btnSave2) as ImageButton
        btnSave.setOnClickListener(btnSaveClickListener)
        btnSave2.setOnClickListener(btnSaveClickListener)
        val btnDel = findViewById<View>(R.id.btnDel) as ImageButton
        btnDel.setOnClickListener(btnDelClickListener)
        val btnDel2 = findViewById<View>(R.id.btnDel2) as ImageButton
        btnDel2.setOnClickListener(btnDel2ClickListener)

        tvHeaderNumber1.text = mResourceController!!.number1
        tvHeaderNumber2.text = mResourceController!!.number2
        tvHeaderNumber3.text = mResourceController!!.number3
        tvHeaderNumber4.text = mResourceController!!.number4
        tvHeaderNumber5.text = mResourceController!!.number5

        tvId = findViewById<View>(R.id.tvId) as TextView
        mLLImages = findViewById<View>(R.id.llImages) as LinearLayout

        spinnerAttr = findViewById<View>(R.id.spinnerAttr) as Spinner
        val cardTypes = mOrmHelper.cardTypeInfoDao.queryForEq(CardInfo.COLUMN_GAMETYPE, mCardInfo!!.gameId)
        val adapterName = SpinnerCommonAdapter(this, cardTypes)
        spinnerAttr!!.adapter = adapterName

        spinnerLevel = findViewById<View>(R.id.spinnerLevel) as Spinner
        etCost = findViewById<View>(R.id.etDetailCost) as EditText

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

        val associationId = mSP.getInt(SHARE_ASSOCIATION_GAME_ID + this.mCardInfo!!.gameId, 0)
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
        mListChar.adapter = mCharListAdapter
        setListViewHeightBasedOnItems()
    }

    private fun initChar() {
        initCharDataAdapter()
    }

    private fun addChar() {
        val characterInfo = CharacterInfo()
        characterInfo.gameId = this.mCardInfo!!.gameId
        mCharListData!!.add(characterInfo)
        mCharListAdapter!!.notifyDataSetChanged()
        setListViewHeightBasedOnItems()
    }

    fun setListViewHeightBasedOnItems() {

        val numberOfItems = mCharListAdapter!!.count
        // Get total height of all items.
        var totalItemsHeight = 0
        for (itemPos in 0 until numberOfItems) {
            val item = mCharListAdapter!!.getView(itemPos, null, mListChar)
            val px = 500 * mListChar.resources.displayMetrics.density
            item.measure(View.MeasureSpec.makeMeasureSpec(px.toInt(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            totalItemsHeight += item.measuredHeight
        }

        // Get total height of all item dividers.
        val totalDividersHeight = mListChar.dividerHeight * (numberOfItems - 1)
        // Get padding
        val totalPadding = mListChar.paddingTop + mListChar.paddingBottom

        // Set list height.
        val params = mListChar.layoutParams
        params.height = totalItemsHeight + totalDividersHeight + totalPadding
        mListChar.layoutParams = params
        mListChar.requestLayout()
    }

    private fun showEvents() {
        llShowEvent.removeAllViews()
        mEventList = mOrmHelper.eventInfoDao.getListByGameId(mCardInfo!!.gameId, "Y")
        mEventList!!.add(0, EventInfo(""))

        val events = mOrmHelper.cardEventInfoDao.getListByCardId(mCardInfo!!.id)
        for (i in events!!.indices) {
            if (isExistInEvent(events[i].eventId)) {
                val spinner = addEvent()
                CommonUtil.setSpinnerItemSelectedByValue2(spinner!!, events[i].eventId.toString())
            }
        }
    }

    private fun isExistInEvent(id: Int): Boolean {
        if (mEventList == null)
            return false
        for (i in mEventList!!.indices) {
            if (mEventList!![i].id == id)
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
            mId = mCardInfo!!.id
            showEvents()
            showCardInfo()
        }
    }

    private fun showCardInfo() {
        val info = mCardInfo
        etHP!!.setText(info!!.maxHP)
        etAttack!!.setText(info.maxAttack)
        etDefense!!.setText(info.maxDefense)
        etExtra1.setText(info.extraValue1)
        etExtra2.setText(info.extraValue2)
        etName!!.setText(info.name)
        etFrontName!!.setText(info.frontName)
        etDetail!!.setText(info.remark)
        etNid!!.setText(info.nid.toString())
        val attr = info.attrId.toString()
        CommonUtil.setSpinnerItemSelectedByValue2(spinnerAttr!!, attr)

        CommonUtil.setSpinnerItemSelectedByValue(spinnerLevel!!,
                info.level.toString())

        etCost!!.setText(info.cost.toString())
        tvId!!.text = info.id.toString()

        chkProfile.isChecked = "Y" == info.profile

        mImagesFiles = SparseArray()
        mImagesView = SparseArray()
        mLLImages!!.removeAllViews()
        showImages()
    }

    private fun showImages() {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_PATH + "/" + mCardInfo!!.gameId)
            var index = 0
            while (++index < 20) {
                val imageFile = File(fileDir.path, CommonUtil.getImageFrontName(mId, index))
                var bitmap: Bitmap? = null
                if (imageFile.exists()) {
                    mImagesFiles!!.append(index, imageFile)
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver, Uri.fromFile(imageFile))
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val child = LayoutInflater.from(this@DetailActivity).inflate(
                            R.layout.child_images_gap, mLLImages, false)
                    mLLImages!!.addView(child)
                    mImagesView!!.append(index, child)

                    val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mCardInfo!!.gameId, false)
                    val isShowImageDate = mSP.getBoolean(SHARE_IMAGE_DATE + mCardInfo!!.gameId, true)
                    val tvDate = child.findViewById<View>(R.id.tvDate) as TextView
                    if (isShowImageDate)
                        tvDate.text = CommonUtil.getFileLastModified(imageFile)
                    val image = child.findViewById<View>(R.id.imgDetails) as ImageView
                    image.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bitmap!!, 90) else bitmap)
                    val oldIndex = index
                    val btnAdjust = child.findViewById<View>(R.id.btnAdjust) as ImageButton
                    btnAdjust.setOnClickListener {
                        val intent = Intent(this@DetailActivity, ImageAdjustActivity::class.java)
                        intent.putExtra("source", mImagesFiles!!.get(oldIndex).absolutePath)
                        startActivity(intent)
                    }
                    val btnDel = child.findViewById<View>(R.id.btnDel) as ImageButton
                    btnDel.tag = "$index*0"
                    btnDel.setOnClickListener { v ->
                        val tag = v.tag.toString().split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val key = Integer.parseInt(tag[0])
                        val line = mImagesView!!.get(key)
                        val timenow = Calendar.getInstance().time.time
                        if (Math.abs(timenow - java.lang.Long.valueOf(tag[1])) > 5000) {
                            Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                            v.tag = "$key*$timenow"
                        } else {
                            v.tag = "$key*0"
                            CommonUtil.deleteImage(this@DetailActivity, mImagesFiles!!.get(key))
                            mLLImages!!.removeView(line)
                            mImagesView!!.remove(key)
                            mImagesFiles!!.remove(key)
                            Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun addEvent(): Spinner? {
        val child = LayoutInflater.from(this@DetailActivity).inflate(
                R.layout.child_event, llShowEvent, false)
        llShowEvent.addView(child)

        val event = InlineEvent(child)
        UIUtils.setSpinnerSingleClick(event.spinner!!)
        val adapter = SpinnerCommonAdapter(this@DetailActivity, mEventList)
        event.spinner!!.adapter = adapter

        event.btnDetail!!.setOnClickListener {
            val info = event.spinner!!.selectedItem as EventInfo
            val intent = Intent(this@DetailActivity, EventInfoActivity::class.java)
            intent.putExtra("event", info.id)
            intent.putExtra("game", mCardInfo!!.gameId)
            startActivity(intent)
        }

        val roundTag = Math.round(Math.random() * 100000000).toInt()
        event.btnDel!!.tag = "$roundTag*0"

        event.btnDel!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val tag = v.tag.toString().split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val info = event.spinner!!.selectedItem as EventInfo
                val id = info.id
                if (id == 0) {
                    llShowEvent.removeView(child)
                    mEventView.remove(Integer.parseInt(tag[0]))
                } else {
                    val timeNow = Calendar.getInstance().time.time
                    if (Math.abs(timeNow - java.lang.Long.valueOf(tag[1])) > 5000) {
                        Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                        v.tag = tag[0] + "*" + timeNow
                    } else {
                        v.tag = tag[0] + "*" + 0
                        mOrmHelper.cardEventInfoDao.delCardEvents(CardEventInfo(mCardInfo!!.id, id)).toLong()
                        run {
                            llShowEvent.removeView(child)
                            mEventView.remove(Integer.parseInt(tag[0]))
                            Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        mEventView.append(roundTag, child)
        return event.spinner
    }

    internal class InlineEvent(view: View) {

        @JvmField
        @BindView(R.id.spinnerEvent)
        var spinner: Spinner? = null

        @JvmField
        @BindView(R.id.btnDetail)
        var btnDetail: ImageButton? = null

        @JvmField
        @BindView(R.id.btnDel)
        var btnDel: ImageButton? = null

        init {
            ButterKnife.bind(this, view)
        }

    }


}
