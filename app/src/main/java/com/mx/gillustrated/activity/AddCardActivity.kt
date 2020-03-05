package com.mx.gillustrated.activity

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Arrays
import java.util.Comparator
import com.mx.gillustrated.R
import com.mx.gillustrated.adapter.SpinnerCommonAdapter
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.CardInfo
import com.mx.gillustrated.vo.CardTypeInfo
import com.mx.gillustrated.vo.MatrixInfo
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick

class AddCardActivity : BaseActivity() {

    private var btnSave: ImageButton? = null
    private var spinnerAttr: Spinner? = null
    private var spinnerLevel: Spinner? = null
    private var spinnerType: Spinner? = null
    private var etId: EditText? = null
    private var etNid: EditText? = null
    private var etName: EditText? = null
    private var etFrontName: EditText? = null
    private var etCost: EditText? = null
    private var etHP: EditText? = null
    private var etAttack: EditText? = null
    private var etDefense: EditText? = null
    private var ivNumber: ImageView? = null
    private var ivAll: ImageView? = null
    private var mFileNumber: File? = null
    private var mFileAll: File? = null
    private var mBitMapNumber: Bitmap? = null
    private var mBitMapAll: Bitmap? = null
    private var mGameType: Int = 0
    private var mImagesFileDir: File? = null
    private var btnDelNumber: ImageButton? = null
    private var btnDelAll: ImageButton? = null

    @BindView(R.id.etDetailExtra1)
    lateinit var etExtra1: EditText

    @BindView(R.id.etDetailExtra2)
    lateinit var etExtra2: EditText


    @BindView(R.id.chkAdjustImg)
    lateinit var chkAdjustImg: CheckBox

    @BindView(R.id.tvAdjustImgTop)
    lateinit var tvAdjustImgTop: EditText

    @BindView(R.id.tvAdjustImgBottom)
    lateinit var tvAdjustImgBottom: EditText

    @BindView(R.id.tvAdjustImgLeft)
    lateinit var tvAdjustImgLeft: EditText

    @BindView(R.id.tvAdjustImgRight)
    lateinit var tvAdjustImgRight: EditText

    private var onTypeSelectListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(arg0: AdapterView<*>, arg1: View, index: Int,
                                    arg3: Long) {

            val array = resources.getStringArray(R.array.addType)
            if (array[index] == "更新附加图" || array[index] == "更新数值图" || array[index] == "新增单张图" || array[index] == "新增无图") {
                ivNumber!!.setImageDrawable(null)
                btnDelNumber!!.visibility = View.GONE
                mBitMapNumber = null
            }
            if (array[index] == "新增无图") {
                ivAll!!.setImageDrawable(null)
                btnDelAll!!.visibility = View.GONE
                mBitMapAll = null
            }
        }

        override fun onNothingSelected(arg0: AdapterView<*>) {

        }

    }


    private var btnDelNumberClickListener: View.OnClickListener = View.OnClickListener {
        CommonUtil.deleteImages(baseContext, mFileNumber)
        try {
            showPicture()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private var btnDelAllClickListener: View.OnClickListener = View.OnClickListener {
        CommonUtil.deleteImages(baseContext, mFileAll)
        try {
            showPicture()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private var btnSaveClickListener: View.OnClickListener = View.OnClickListener {
        mProgressDialog.show()
        object : Thread() {
            override fun run() {

                val card = CardInfo()
                if ("" != etNid!!.text.toString().trim { it <= ' ' })
                    card.nid = Integer.parseInt(etNid!!.text.toString())

                val cardTypeInfo = spinnerAttr!!.selectedItem as CardTypeInfo
                card.attrId = cardTypeInfo.id
                card.gameId = mGameType
                card.level = spinnerLevel!!.selectedItem.toString()
                card.name = etName!!.text.toString().trim { it <= ' ' }
                card.pinyinName = PinyinUtil.convert(card.name)
                card.frontName = etFrontName!!.text.toString().trim { it <= ' ' }
                card.profile = "Y"
                if (etCost!!.text.toString().trim { it <= ' ' } != "")
                    card.cost = Integer.parseInt(etCost!!.text
                            .toString())
                else
                    card.cost = 0
                if (etHP!!.text.toString().trim { it <= ' ' } != "")
                    card.maxHP = etHP!!.text
                            .toString().trim { it <= ' ' }
                if (etAttack!!.text.toString().trim { it <= ' ' } != "")
                    card.maxAttack = etAttack!!.text
                            .toString().trim { it <= ' ' }
                if (etDefense!!.text.toString().trim { it <= ' ' } != "")
                    card.maxDefense = etDefense!!.text
                            .toString().trim { it <= ' ' }
                if (etExtra1.text.toString().trim { it <= ' ' } != "")
                    card.extraValue1 = etExtra1.text
                            .toString().trim { it <= ' ' }
                if (etExtra2.text.toString().trim { it <= ' ' } != "")
                    card.extraValue2 = etExtra2.text
                            .toString().trim { it <= ' ' }

                val type = spinnerType!!.selectedItemPosition

                mImagesFileDir = File(
                        Environment.getExternalStorageDirectory(),
                        MConfig.SD_PATH + "/" + mGameType)
                if (!mImagesFileDir!!.exists()) {
                    mImagesFileDir!!.mkdirs()
                }

                if (type == 0 || type == 1 || type == 4) { // type=4 && m_BitMapAll=null
                    mOrmHelper.cardInfoDao.create(card)
                    val newId = card.id.toLong()
                    if (newId != 0L) {
                        if (mBitMapAll != null) {
                            if (type == 0 && mBitMapNumber != null) {
                                createImages(newId.toInt(), mBitMapNumber!!, 1)
                            }
                            if (type == 0 || type == 1) {
                                if (mBitMapNumber != null)
                                    createImages(newId.toInt(), mBitMapAll!!, if (type == 0) 2 else 1)
                                else
                                    createImages(newId.toInt(), mBitMapAll!!, 1)
                            }

                            CommonUtil.deleteImages(this@AddCardActivity,
                                    mFileAll)
                            if (type == 0)
                                CommonUtil.deleteImages(this@AddCardActivity,
                                        mFileNumber)
                        }
                        val msg = Message.obtain()
                        msg.what = 1
                        msg.arg1 = newId.toInt()
                        addHandler.sendMessage(msg)
                    }
                } else {
                    // 更新数值图  更新附加图
                    val id = Integer.parseInt(etId!!.text.toString().trim { it <= ' ' })
                    val nextnum = getNextImagesIndex(id)
                    val imageFile = File(mImagesFileDir!!.path,
                            CommonUtil.getImageFrontName(id, 1))
                    val nextFile = File(mImagesFileDir!!.path,
                            CommonUtil.getImageFrontName(id, nextnum))
                    if (type == 2) {
                        imageFile.renameTo(nextFile)
                        createImages(id, mBitMapAll!!, 1)
                        card.id = id
                        card.level = null
                        card.frontName = null
                        card.name = null
                        if (card.cost > 0 || "" != card.maxHP || "" != card.maxAttack || "" != card.maxDefense)
                            mOrmHelper.cardInfoDao.update(card)
                    } else if (type == 3)
                        try {
                            CommonUtil.exportImgFromBitmap(mBitMapAll!!, nextFile)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    CommonUtil.deleteImages(this@AddCardActivity,
                            mFileAll)
                    addHandler.sendEmptyMessage(2)
                }
            }
        }.start()
    }

    internal var addHandler: Handler = AddHandler(this)

    @OnClick(R.id.btnSaveMatrix)
    internal fun onSaveMatrixBtnClick() {
        val top = Integer.parseInt(this.tvAdjustImgTop.text.toString())
        val bottom = Integer.parseInt(this.tvAdjustImgBottom.text.toString())
        val left = Integer.parseInt(this.tvAdjustImgLeft.text.toString())
        val right = Integer.parseInt(this.tvAdjustImgRight.text.toString())
        mSP.edit().putString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, "$top,$bottom,$left,$right").apply()
        if (chkAdjustImg.isChecked) {
            try {
                this.showPicture()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


    }

    @OnCheckedChanged(R.id.chkAdjustImg)
    internal fun onCheckAdjustImgTopChanged(checked: Boolean) {
        mSP.edit().putBoolean(SHARE_IMAGES_MATRIX + mGameType, checked).apply()
        try {
            this.showPicture()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        ButterKnife.bind(this)

        mGameType = intent.getIntExtra("game", 0)

        btnSave = findViewById<View>(R.id.btnSave) as ImageButton
        btnSave!!.setOnClickListener(btnSaveClickListener)

        btnDelNumber = findViewById<View>(R.id.btnDelNumber) as ImageButton
        btnDelNumber!!.setOnClickListener(btnDelNumberClickListener)
        btnDelAll = findViewById<View>(R.id.btnDelAll) as ImageButton
        btnDelAll!!.setOnClickListener(btnDelAllClickListener)

        spinnerAttr = findViewById<View>(R.id.spinnerAttr) as Spinner
        spinnerLevel = findViewById<View>(R.id.spinnerLevel) as Spinner
        CommonUtil.setSpinnerItemSelectedByValue(spinnerLevel!!, "5")
        etNid = findViewById<View>(R.id.etDetailNid) as EditText
        etName = findViewById<View>(R.id.etDetailName) as EditText
        etFrontName = findViewById<View>(R.id.etDetailFrontName) as EditText
        etCost = findViewById<View>(R.id.etDetailCost) as EditText
        etId = findViewById<View>(R.id.etDetailId) as EditText
        etHP = findViewById<View>(R.id.etDetailHP) as EditText
        etAttack = findViewById<View>(R.id.etDetailAttack) as EditText
        etDefense = findViewById<View>(R.id.etDetailDefense) as EditText
        spinnerType = findViewById<View>(R.id.spinnerType) as Spinner
        spinnerType!!.setSelection(0)
        spinnerType!!.onItemSelectedListener = onTypeSelectListener

        ivNumber = findViewById<View>(R.id.imgWithNumber) as ImageView
        ivAll = findViewById<View>(R.id.imgAll) as ImageView

        val cardTypes = mOrmHelper.cardTypeInfoDao.queryForEq("game_type", mGameType)
        val adapterName = SpinnerCommonAdapter(this, cardTypes)
        spinnerAttr!!.adapter = adapterName

        setImagesMatrixConfig()

        try {
            showPicture()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun setImagesMatrixConfig() {
        val numbers = mSP.getString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, "0,0,0,0")!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        tvAdjustImgTop.setText(numbers[0])
        tvAdjustImgBottom.setText(numbers[1])
        tvAdjustImgLeft.setText(numbers[2])
        tvAdjustImgRight.setText(numbers[3])
        chkAdjustImg.isChecked = mSP.getBoolean(SHARE_IMAGES_MATRIX + mGameType, false)
    }

    private fun getMatrixBitmap(input: Bitmap): Bitmap {
        val top = Integer.parseInt(this.tvAdjustImgTop.text.toString())
        val bottom = Integer.parseInt(this.tvAdjustImgBottom.text.toString())
        val left = Integer.parseInt(this.tvAdjustImgLeft.text.toString())
        val right = Integer.parseInt(this.tvAdjustImgRight.text.toString())

        val matrixInfo = MatrixInfo()
        matrixInfo.y = top
        matrixInfo.x = left
        matrixInfo.height = top + bottom
        matrixInfo.width = left + right

        return CommonUtil.cutBitmap(input, matrixInfo, true)
    }


    @Throws(FileNotFoundException::class, IOException::class)
    private fun showPicture() {
        if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState()) {
            var fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SRC_PATH)
            if (!fileDir.exists())
                fileDir = File(Environment.getExternalStorageDirectory(),
                        MConfig.SRC_PATH_SAMSUNG)
            if (!fileDir.exists())
                return

            val file = File(fileDir.path)
            val fs = file.listFiles() ?: return
            Arrays.sort(fs, object : Comparator<File> {
                override fun compare(f1: File, f2: File): Int {
                    val diff = f1.lastModified() - f2.lastModified()
                    return when {
                        diff > 0 -> -1
                        diff == 0L -> 0
                        else -> 1
                    }
                }

                override fun equals(other: Any?): Boolean {
                    return true
                }

            })

            btnDelAll!!.visibility = View.GONE
            btnDelNumber!!.visibility = View.GONE
            ivAll!!.setImageBitmap(null)
            ivNumber!!.setImageBitmap(null)
            val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false)
            for (i in fs.indices) {
                if (i == 2)
                    break

                var bmp = MediaStore.Images.Media.getBitmap(
                        this.contentResolver, Uri.fromFile(fs[i]))
                if (this.chkAdjustImg.isChecked)
                    bmp = getMatrixBitmap(bmp)

                if (i == 0) {
                    mFileAll = fs[i]
                    mBitMapAll = bmp
                    ivAll!!.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                    btnDelAll!!.visibility = View.VISIBLE
                } else {
                    mFileNumber = fs[i]
                    mBitMapNumber = bmp
                    ivNumber!!.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                    btnDelNumber!!.visibility = View.VISIBLE
                }
            }

        }

    }

    private fun createImages(id: Int, bitmap: Bitmap, num: Int) {
        val bos: FileOutputStream
        val imageFile = File(mImagesFileDir!!.path,
                CommonUtil.getImageFrontName(id, num))
        try {
            bos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    30, bos)
            bos.flush()
            bos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getNextImagesIndex(id: Int): Int {
        var checkNum = 3
        while (true) {
            val check = File(mImagesFileDir!!.path,
                    CommonUtil.getImageFrontName(id, checkNum))
            if (!check.exists())
                break
            else
                checkNum++
        }
        return checkNum
    }


    private class AddHandler internal constructor(activity: AddCardActivity) : Handler() {

        private val mActivity: WeakReference<AddCardActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                Toast.makeText(mActivity.get(), "id: " + msg.arg1, Toast.LENGTH_SHORT).show()
                mActivity.get()?.forwardBack()
            } else if (msg.what == 2) {
                mActivity.get()?.forwardBack()
            }
        }
    }


    private fun forwardBack() {
        val intent = Intent(this@AddCardActivity,
                MainActivity::class.java)
        intent.putExtra("game", mGameType)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this@AddCardActivity.finish()
        mProgressDialog.dismiss()
    }

}
