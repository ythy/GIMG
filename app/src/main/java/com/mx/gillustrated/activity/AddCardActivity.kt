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
import android.os.*
import android.provider.MediaStore
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import androidx.core.util.valueIterator
import com.mx.gillustrated.databinding.ActivityAddBinding

@Suppress("DEPRECATION")
class AddCardActivity : BaseActivity() {

    private var mFileNumber: File? = null
    private var mFileAll: File? = null
    private var mBitMapNumber: Bitmap? = null
    private var mBitMapAll: Bitmap? = null

    private var mFileExtras:SparseArray<File> = SparseArray()
    private var mBitmapExtras:SparseArray<Bitmap> = SparseArray()

    private var mGameType: Int = 0
    private var mImagesFileDir: File? = null
    lateinit var binding:ActivityAddBinding

    private var onTypeSelectListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(arg0: AdapterView<*>, arg1: View, index: Int,
                                    arg3: Long) {
            if(index == 5){
                binding.spinnerExtra.visibility = View.VISIBLE
                binding.spinnerExtra.setSelection(1)
            }else{
                binding.spinnerExtra.visibility = View.GONE
            }
            try {
                showPicture()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        override fun onNothingSelected(arg0: AdapterView<*>) {

        }

    }



    private var btnDelNumberClickListener: View.OnClickListener = View.OnClickListener {
        deleteImages(mFileNumber!!, object :OnCallback{
            override fun deleted() {
                try {
                    showPicture()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private var btnDelAllClickListener: View.OnClickListener = View.OnClickListener {
        deleteImages(mFileAll!!, object :OnCallback{
            override fun deleted() {
                try {
                    showPicture()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private var btnSaveClickListener: View.OnClickListener = View.OnClickListener {
        mProgressDialog.show()
        object : Thread() {
            override fun run() {

                val card = CardInfo()
                if ("" != binding.etDetailNid.text.toString().trim { it <= ' ' })
                    card.nid = Integer.parseInt(binding.etDetailNid.text.toString())

                val cardTypeInfo = binding.spinnerAttr.selectedItem as CardTypeInfo
                card.attrId = cardTypeInfo.id
                card.gameId = mGameType
                card.level = binding.spinnerLevel.selectedItem.toString()
                card.name = binding.etDetailName.text.toString().trim { it <= ' ' }
                card.pinyinName = PinyinUtil.convert(card.name!!)
                card.frontName = binding.etDetailFrontName.text.toString().trim { it <= ' ' }
                card.profile = "Y"
                if (binding.etDetailCost.text.toString().trim { it <= ' ' } != "")
                    card.cost = Integer.parseInt(binding.etDetailCost.text
                            .toString())
                else
                    card.cost = 0
                if (binding.etDetailHP.text.toString().trim { it <= ' ' } != "")
                    card.maxHP = binding.etDetailHP.text
                            .toString().trim { it <= ' ' }
                if (binding.etDetailAttack.text.toString().trim { it <= ' ' } != "")
                    card.maxAttack = binding.etDetailAttack.text
                            .toString().trim { it <= ' ' }
                if (binding.etDetailDefense.text.toString().trim { it <= ' ' } != "")
                    card.maxDefense = binding.etDetailDefense.text
                            .toString().trim { it <= ' ' }
                if (binding.etDetailExtra1.text.toString().trim { it <= ' ' } != "")
                    card.extraValue1 = binding.etDetailExtra1.text
                            .toString().trim { it <= ' ' }
                if (binding.etDetailExtra2.text.toString().trim { it <= ' ' } != "")
                    card.extraValue2 = binding.etDetailExtra2.text
                            .toString().trim { it <= ' ' }

                val type = binding.spinnerType.selectedItemPosition

                mImagesFileDir = File(
                        Environment.getExternalStorageDirectory(),
                        MConfig.SD_PATH + "/" + mGameType)
                if (!mImagesFileDir!!.exists()) {
                    mImagesFileDir!!.mkdirs()
                }

                if (type == 0 || type == 1 || type == 4 || type == 5) { // type=4 && m_BitMapAll=null
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

                            if (type == 0)
                                deleteImages(listOf(mFileNumber!!, mFileAll!!), object :OnCallback{
                                    override fun deleted() {
                                        val msg = Message.obtain()
                                        msg.what = 1
                                        msg.arg1 = newId.toInt()
                                        addHandler.sendMessage(msg)
                                    }

                                })
                            else if(type == 1)
                                deleteImages(mFileAll!!, object :OnCallback{
                                    override fun deleted() {
                                        val msg = Message.obtain()
                                        msg.what = 1
                                        msg.arg1 = newId.toInt()
                                        addHandler.sendMessage(msg)
                                    }

                                })
                        }else if(!mBitmapExtras.isEmpty()) { //0 - 5
                            val total = mBitmapExtras.size()
                            mBitmapExtras.forEach { key, value ->
                                createImages(newId.toInt(), value, total - key)
                            }
                            val files = mutableListOf<File>()
                            mFileExtras.forEach { key, value ->  files.add(value)}
                            deleteImages(files, object :OnCallback{
                                override fun deleted() {
                                    val msg = Message.obtain()
                                    msg.what = 1
                                    msg.arg1 = newId.toInt()
                                    addHandler.sendMessage(msg)
                                }

                            })
                        }else {
                            val msg = Message.obtain()
                            msg.what = 1
                            msg.arg1 = newId.toInt()
                            addHandler.sendMessage(msg)
                        }
                    }
                } else if(type == 2 || type == 3) {
                    // 更新数值图  更新附加图
                    val id = Integer.parseInt(binding.etDetailId.text.toString().trim { it <= ' ' })
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
                        if (card.cost > 0 || card.maxHP != null || card.maxAttack != null
                                || card.maxDefense != null || card.extraValue1 != null || card.extraValue2 != null )
                            mOrmHelper.cardInfoDao.update(card)
                    } else if (type == 3)
                        try {
                            CommonUtil.exportImgFromBitmap(mBitMapAll!!, nextFile)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    deleteImages(mFileAll!!, object :OnCallback{
                        override fun deleted() {
                            addHandler.sendEmptyMessage(2)
                        }
                    })

                }
            }
        }.start()
    }

    internal var addHandler: Handler = AddHandler(this)


    companion object {

        private class AddHandler constructor(activity: AddCardActivity) : Handler(Looper.getMainLooper()) {

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

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mGameType = intent.getIntExtra("game", 0)


        binding.btnSave.setOnClickListener(btnSaveClickListener)
        binding.btnDelNumber.setOnClickListener(btnDelNumberClickListener)
        binding.btnDelAll.setOnClickListener(btnDelAllClickListener)
        CommonUtil.setSpinnerItemSelectedByValue(binding.spinnerLevel, "5")

        val cardTypes = mOrmHelper.cardTypeInfoDao.queryForEq("game_type", mGameType)
        val adapterName = SpinnerCommonAdapter(this, cardTypes)
        binding.spinnerAttr.adapter = adapterName
        setImagesMatrixConfig()
        binding.spinnerType.onItemSelectedListener = onTypeSelectListener
        binding.spinnerType.setSelection(4)

        binding.btnSaveMatrix.setOnClickListener {
            val top = Integer.parseInt(binding.tvAdjustImgTop.text.toString())
            val bottom = Integer.parseInt(binding.tvAdjustImgBottom.text.toString())
            val left = Integer.parseInt(binding.tvAdjustImgLeft.text.toString())
            val right = Integer.parseInt(binding.tvAdjustImgRight.text.toString())
            mSP.edit().putString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, "$top,$bottom,$left,$right").apply()
            if (binding.chkAdjustImg.isChecked) {
                try {
                    this.showPicture()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        binding.chkAdjustImg.setOnCheckedChangeListener { _, isChecked ->
            mSP.edit().putBoolean(SHARE_IMAGES_MATRIX + mGameType, isChecked).apply()
            try {
                this.showPicture()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    private fun setImagesMatrixConfig() {
        val numbers = mSP.getString(SHARE_IMAGES_MATRIX_NUMBER + mGameType, "0,0,0,0")!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        binding.tvAdjustImgTop.setText(numbers[0])
        binding.tvAdjustImgBottom.setText(numbers[1])
        binding.tvAdjustImgLeft.setText(numbers[2])
        binding.tvAdjustImgRight.setText(numbers[3])
        binding.chkAdjustImg.isChecked = mSP.getBoolean(SHARE_IMAGES_MATRIX + mGameType, false)
    }

    private fun getMatrixBitmap(input: Bitmap): Bitmap {
        val top = Integer.parseInt(binding.tvAdjustImgTop.text.toString())
        val bottom = Integer.parseInt(binding.tvAdjustImgBottom.text.toString())
        val left = Integer.parseInt(binding.tvAdjustImgLeft.text.toString())
        val right = Integer.parseInt(binding.tvAdjustImgRight.text.toString())

        val matrixInfo = MatrixInfo()
        matrixInfo.y = top
        matrixInfo.x = left
        matrixInfo.height = top + bottom
        matrixInfo.width = left + right

        return CommonUtil.cutBitmap(input, matrixInfo, true)
    }


    @Throws(FileNotFoundException::class, IOException::class)
    private fun showPicture() {
        binding.btnDelAll.visibility = View.GONE
        binding.btnDelNumber.visibility = View.GONE
        binding.btnDelExtra1.visibility = View.GONE
        binding.btnDelExtra2.visibility = View.GONE
        binding.btnDelExtra3.visibility = View.GONE
        binding.btnDelExtra4.visibility = View.GONE
        binding.btnDelExtra5.visibility = View.GONE
        binding.btnDelExtra6.visibility = View.GONE
        binding.imgAll.setImageBitmap(null)
        binding.imgWithNumber.setImageBitmap(null)
        binding.imgExtra1.setImageBitmap(null)
        binding.imgExtra2.setImageBitmap(null)
        binding.imgExtra3.setImageBitmap(null)
        binding.imgExtra4.setImageBitmap(null)
        binding.imgExtra5.setImageBitmap(null)
        binding.imgExtra6.setImageBitmap(null)
        mBitMapNumber = null
        mBitMapAll = null
        mFileAll = null
        mFileNumber = null
        mFileExtras = SparseArray()
        mBitmapExtras = SparseArray()
        binding.llImageExtra.visibility = View.GONE
        val type = binding.spinnerType.selectedItemPosition
        if (type == 4)
            return
        if (type == 5)
            binding.llImageExtra.visibility = View.VISIBLE
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
            val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION + mGameType, false)
            val extraTotal =  binding.spinnerExtra.selectedItem.toString().toInt()
            for (i in fs.indices) {
                if (i == 1 && (type == 1 || type == 2 || type == 3))
                    break
                if (i == 2 && type == 0)
                    break
                if (i == extraTotal && type == 5)
                    break
                var bmp = MediaStore.Images.Media.getBitmap(
                        this.contentResolver, Uri.fromFile(fs[i]))
                if (binding.chkAdjustImg.isChecked)
                    bmp = getMatrixBitmap(bmp)
                if(type != 5){
                    if (i == 0) {
                        mFileAll = fs[i]
                        mBitMapAll = bmp
                        binding.imgAll.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                        binding.btnDelAll.visibility = View.VISIBLE
                    } else if (i == 1) {
                        mFileNumber = fs[i]
                        mBitMapNumber = bmp
                        binding.imgWithNumber.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                        binding.btnDelNumber.visibility = View.VISIBLE
                    }
                }else{
                    when(i){
                        0 -> {
                            mFileExtras.put(0, fs[i])
                            mBitmapExtras.put(0, bmp)
                            binding.imgExtra1.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra1.visibility = View.VISIBLE
                        }
                        1 -> {
                            mFileExtras.put(1, fs[i])
                            mBitmapExtras.put(1, bmp)
                            binding.imgExtra2.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra2.visibility = View.VISIBLE
                        }
                        2 -> {
                            mFileExtras.put(2, fs[i])
                            mBitmapExtras.put(2, bmp)
                            binding.imgExtra3.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra3.visibility = View.VISIBLE
                        }
                        3 -> {
                            mFileExtras.put(3, fs[i])
                            mBitmapExtras.put(3, bmp)
                            binding.imgExtra4.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra4.visibility = View.VISIBLE
                        }
                        4 -> {
                            mFileExtras.put(4, fs[i])
                            mBitmapExtras.put(4, bmp)
                            binding.imgExtra5.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra5.visibility = View.VISIBLE
                        }
                        5 -> {
                            mFileExtras.put(5, fs[i])
                            mBitmapExtras.put(5, bmp)
                            binding.imgExtra6.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bmp, 90) else bmp)
                            binding.btnDelExtra6.visibility = View.VISIBLE
                        }
                    }
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
