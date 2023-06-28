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
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseArray
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.util.forEach
import com.mx.gillustrated.databinding.ActivityDetailBinding
import com.mx.gillustrated.databinding.ChildEventBinding
import com.mx.gillustrated.databinding.ChildImagesGapBinding
import java.io.FileOutputStream
import kotlin.math.abs

class DetailActivity : BaseActivity() {

    companion object {
        private const val SELECT_PIC_PROFILE = 30
        private const val SELECT_PIC_LIST = 40
    }

    private lateinit var mCardInfo: CardInfo
    private var mId: Int = 0
    private var mMainSearchInfo: Array<String>? = null
    private var mMainSearchOrderBy: String? = null
    private var mCurrentPosition: Int = 0
    private var mMainTotalCount: Int = 0

    private lateinit var mImagesFiles: SparseArray<File>
    private lateinit var mImagesView: SparseArray<ChildImagesGapBinding>
    private var mEventList: MutableList<EventInfo> = mutableListOf()
    private var mResourceController: ResourceController? = null
    private var mCharListAdapter: CharacterListAdapter? = null
    private var mCharListData: MutableList<CharacterInfo>? = null

    lateinit var binding:ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        mId = intent.getIntExtra("card", 0)
        mMainSearchInfo = intent.getStringArrayExtra("cardSearchCondition")
        mMainSearchOrderBy = intent.getStringExtra("orderBy")
        mCurrentPosition = intent.getIntExtra("positon", -1)
        mMainTotalCount = intent.getIntExtra("totalCount", 0)
        mCardInfo = mOrmHelper.cardInfoDao.queryForId(mId)
        mResourceController = ResourceController(this, mCardInfo.gameId)

        binding.tvHeaderHp.text = mResourceController!!.number1
        binding.tvHeaderA.text = mResourceController!!.number2
        binding.tvHeaderD.text = mResourceController!!.number3
        binding.tvHeaderE1.text = mResourceController!!.number4
        binding.tvHeaderE2.text = mResourceController!!.number5

        val cardTypes = mOrmHelper.cardTypeInfoDao.queryForEq(CardInfo.COLUMN_GAMETYPE, mCardInfo.gameId)
        val adapterName = SpinnerCommonAdapter(this, cardTypes)
        binding.spinnerAttr.adapter = adapterName

        binding.btnNext.setOnClickListener { searchCardSide(1) }
        binding.btnLast.setOnClickListener { searchCardSide(-1) }

        initChar()
        showEvents()
        showCardInfo()
        initListener()
    }

    private fun initListener(){
        binding.btnRead.setOnClickListener {
            setCardInfoFromPreviousCard()
        }
        binding.btnSave.setOnClickListener {
            saveCard()
        }
        binding.btnSave2.setOnClickListener {
            saveCard()
        }
        binding.btnDel.setOnClickListener {
            removeCard()
        }
        binding.btnDel2.setOnClickListener {
            mImagesView.forEach { _, value ->
                value.btnDel.visibility = View.VISIBLE
                value.btnAdjust.visibility = View.VISIBLE
            }
        }
        binding.btnAddChar.setOnClickListener {
            addChar()
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 5000) }
        }
        binding.btnAddEvent.setOnClickListener {
            addEvent()
            binding.scrollView.post { binding.scrollView.smoothScrollTo(0, 5000) }
        }
        binding.btnSaveEvent.setOnClickListener {
            val events = ArrayList<CardEventInfo>()
            try {
                for (i in 0 until binding.llShowEvent.childCount ) {
                    val spinner =  binding.llShowEvent.getChildAt(i).findViewById<View>(R.id.spinnerEvent) as Spinner
                    val info = spinner.selectedItem as EventInfo
                    events.add(CardEventInfo(mCardInfo.id, info.id))
                }
                mOrmHelper.cardEventInfoDao.addCardEvents(events)
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        binding.btnSetProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, SELECT_PIC_PROFILE)
        }
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, SELECT_PIC_LIST)
        }
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
        binding.lvChar.adapter = mCharListAdapter
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
            val item = mCharListAdapter!!.getView(itemPos, null, binding.lvChar)
            val px = 500 * binding.lvChar.resources.displayMetrics.density
            item.measure(View.MeasureSpec.makeMeasureSpec(px.toInt(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            totalItemsHeight += item.measuredHeight
        }

        // Get total height of all item dividers.
        val totalDividersHeight = binding.lvChar.dividerHeight * (numberOfItems - 1)
        // Get padding
        val totalPadding = binding.lvChar.paddingTop + binding.lvChar.paddingBottom

        // Set list height.
        val params = binding.lvChar.layoutParams
        params.height = totalItemsHeight + totalDividersHeight + totalPadding
        binding.lvChar.layoutParams = params
        binding.lvChar.requestLayout()
    }

    private fun showEvents() {
        binding.llShowEvent.removeAllViews()
        mEventList = mOrmHelper.eventInfoDao.getListByGameId(mCardInfo.gameId, "Y")
        mEventList.add(0, EventInfo(""))

        val events = mOrmHelper.cardEventInfoDao.getListByCardId(mCardInfo.id)
        for (i in events!!.indices) {
            if (isExistInEvent(events[i].eventId)) {
                val spinner = addEvent()
                CommonUtil.setSpinnerItemSelectedByValue2(spinner, events[i].eventId.toString())
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

        mCurrentPosition = newPositon
        mCardInfo = mOrmHelper.cardInfoDao.queryForId(result.id)
        mId = mCardInfo.id
        initChar()
        showEvents()
        showCardInfo()

    }

    private fun showCardInfo() {
        val info = mCardInfo
        binding.etDetailHP.setText(info.maxHP)
        binding.etDetailAttack.setText(info.maxAttack)
        binding.etDetailDefense.setText(info.maxDefense)
        binding.etDetailExtra1.setText(info.extraValue1)
        binding.etDetailExtra2.setText(info.extraValue2)
        binding.etDetailName.setText(info.name)
        binding.etDetailFrontName.setText(info.frontName)
        binding.etDetail.setText(info.remark)
        binding.etDetailNid.setText(info.nid.toString())
        val attr = info.attrId.toString()
        CommonUtil.setSpinnerItemSelectedByValue2(binding.spinnerAttr, attr)

        CommonUtil.setSpinnerItemSelectedByValue(binding.spinnerLevel,
                info.level.toString())

        binding.etDetailCost.setText(info.cost.toString())
        binding.tvId.text = info.id.toString()

        binding.chkProfile.isChecked = "Y" == info.profile

        mImagesFiles = SparseArray()
        mImagesView = SparseArray()
        binding.llImages.removeAllViews()
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
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, Uri.fromFile(imageFile)))

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val bindingImages = ChildImagesGapBinding.inflate(layoutInflater, binding.llImages, false )
                    binding.llImages.addView(bindingImages.root)
                    mImagesView.append(index, bindingImages)

                    val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mCardInfo.gameId, false)
                    val isShowImageDate = mSP.getBoolean(SHARE_IMAGE_DATE + mCardInfo.gameId, true)

                    if (isShowImageDate)
                        bindingImages.tvDate.text = CommonUtil.getFileLastModified(imageFile)

                    bindingImages.imgDetails.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bitmap!!, 90) else bitmap)
                    val oldIndex = index
                    bindingImages.btnAdjust.setOnClickListener {
                        val intent = Intent(this@DetailActivity, ImageAdjustActivity::class.java)
                        intent.putExtra("source", mImagesFiles.get(oldIndex).absolutePath)
                        startActivity(intent)
                    }
                    bindingImages. btnDel.tag = "$index*0"
                    bindingImages.btnDel.setOnClickListener { v ->
                        val tag = v.tag.toString().split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val key = Integer.parseInt(tag[0])
                        val line = mImagesView.get(key)
                        val timenow = Calendar.getInstance().time.time
                        if (abs(timenow - java.lang.Long.valueOf(tag[1])) > 5000) {
                            Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                            v.tag = "$key*$timenow"
                        } else {
                            v.tag = "$key*0"
                            CommonUtil.deleteImage(this@DetailActivity, mImagesFiles.get(key))
                            binding.llImages.removeView(line.root)
                            mImagesView.remove(key)
                            mImagesFiles.remove(key)
                            Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun addEvent(): Spinner {

        val eventBinding:ChildEventBinding = ChildEventBinding.inflate(layoutInflater, binding.llShowEvent, false)
        binding.llShowEvent.addView(eventBinding.root)

        UIUtils.setSpinnerSingleClick(eventBinding.spinnerEvent)
        val adapter = SpinnerCommonAdapter(this@DetailActivity, mEventList)
        eventBinding.spinnerEvent.adapter = adapter

        eventBinding.btnDetail.setOnClickListener {
            val info = eventBinding.spinnerEvent.selectedItem as EventInfo
            val intent = Intent(this@DetailActivity, EventInfoActivity::class.java)
            intent.putExtra("event", info.id)
            intent.putExtra("game", mCardInfo.gameId)
            startActivity(intent)
        }


        eventBinding.btnDel.tag = 0

        eventBinding.btnDel.setOnClickListener { v ->
            val tag = v.tag.toString()
            val info = eventBinding.spinnerEvent.selectedItem as EventInfo
            val id = info.id
            if (id == 0) {
                binding.llShowEvent.removeView(eventBinding.root)
            } else {
                val timeNow = Calendar.getInstance().time.time
                if (abs(timeNow - java.lang.Long.valueOf(tag)) > 5000) {
                    Toast.makeText(this@DetailActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                    v.tag = timeNow
                } else {
                    v.tag = 0
                    mOrmHelper.cardEventInfoDao.delCardEvents(CardEventInfo(mCardInfo.id, id))
                    binding.llShowEvent.removeView(eventBinding.root)
                    Toast.makeText(this@DetailActivity, "删除成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return eventBinding.spinnerEvent
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

                        if (path.lowercase().endsWith("jpg") || path.lowercase().endsWith("png") ||
                                path.lowercase().endsWith("jpeg")) {
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

    private fun setCardInfoFromPreviousCard() {
        if (mId > 1) {
            val preCard = mOrmHelper.cardInfoDao.queryForId(mId - 1)
            binding.etDetailFrontName.setText(preCard.frontName)
            binding.etDetail.setText(preCard.remark)
            binding.etDetailHP.setText(preCard.maxHP)
            binding.etDetailAttack.setText(preCard.maxAttack)
            binding.etDetailDefense.setText(preCard.maxDefense)
            binding.etDetailExtra1.setText(preCard.extraValue1)
            binding.etDetailExtra2.setText(preCard.extraValue2)
            CommonUtil.setSpinnerItemSelectedByValue2(binding.spinnerAttr, preCard.attrId.toString())
            val preEvents = mOrmHelper.cardEventInfoDao.getListByCardId(mId - 1)
            if (preEvents != null && preEvents.isNotEmpty()) {
                binding.llShowEvent.removeAllViews()
                for (i in preEvents.indices) {
                    if (isExistInEvent(preEvents[i].eventId)) {
                        val spinner = addEvent()
                        CommonUtil.setSpinnerItemSelectedByValue2(spinner, preEvents[i].eventId.toString())
                    }
                }
            }
        }
    }

    private fun saveCard() {
        var card: CardInfo
        var result: Long = 0
        // 名称优先批量更新
        if (!binding.chkModify.isChecked
                && mCardInfo.name != ""
                && mCardInfo.name != binding.etDetailName.text.toString().trim { it <= ' ' }) {
            val cardOld = mCardInfo
            card = CardInfo()
            card.name = binding.etDetailName.text.toString().trim { it <= ' ' }
            card.pinyinName = PinyinUtil.convert(card.name!!)
            result = mOrmHelper.cardInfoDao.updateCardName(card, cardOld)
        }

        card = CardInfo()
        card.id = mId
        card.nid = Integer.parseInt(binding.etDetailNid.text.toString())
        card.gameId = mCardInfo.gameId
        val cardTypeInfo = binding.spinnerAttr.selectedItem as CardTypeInfo
        card.attrId = cardTypeInfo.id
        card.level = binding.spinnerLevel.selectedItem.toString()

        card.cost = if (binding.etDetailCost.text.toString().trim { it <= ' ' } == "")
            0
        else
            Integer.parseInt(binding.etDetailCost.text.toString())
        card.name = binding.etDetailName.text.toString().trim { it <= ' ' }
        card.pinyinName = PinyinUtil.convert(card.name!!)
        card.frontName = binding.etDetailFrontName.text.toString().trim { it <= ' ' }
        card.remark = binding.etDetail.text.toString().trim { it <= ' ' }
        card.profile = if (binding.chkProfile.isChecked) "Y" else "N"
        card.maxHP = binding.etDetailHP.text.toString().trim { it <= ' ' }
        card.maxAttack = binding.etDetailAttack.text.toString().trim { it <= ' ' }
        card.maxDefense = binding.etDetailDefense.text.toString().trim { it <= ' ' }
        card.extraValue1 = binding.etDetailExtra1.text.toString().trim { it <= ' ' }
        card.extraValue2 = binding.etDetailExtra2.text.toString().trim { it <= ' ' }
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

    private fun removeCard() {
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

}
