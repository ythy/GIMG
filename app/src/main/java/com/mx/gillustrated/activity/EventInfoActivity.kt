package com.mx.gillustrated.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mx.gillustrated.R
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.ResourceController
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.EventInfo
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

/**
 * Created by maoxin on 2017/2/22.
 */

class EventInfoActivity : BaseActivity() {

    private var mEventId: Int = 0
    private var mGameId: Int = 0
    private var mImagesFiles: SparseArray<File>? = null
    private var mImagesView: SparseArray<ImageBox>? = null
    private var mResourceController: ResourceController? = null

    @BindView(R.id.etDetailName)
    internal lateinit var mName: EditText

    @BindView(R.id.etDetailTime)
    internal lateinit var mDuration: EditText

    @BindView(R.id.etIndex)
    internal lateinit var mIndex: EditText

    @BindView(R.id.etDetailContent)
    internal lateinit var mContent: EditText

    @BindView(R.id.cbShowing)
    internal lateinit var mCbShowing: CheckBox

    @BindView(R.id.btnDel)
    internal lateinit var mBtnDel: ImageButton

    @BindView(R.id.llImages)
    internal lateinit var mLLImages: LinearLayout

    @OnClick(R.id.btnSave)
    internal fun onSave() {
        val request = EventInfo()
        request.id = mEventId
        request.name = mName.text.toString()
        request.duration = mDuration.text.toString()
        request.content = mContent.text.toString()
        request.showing = if (mCbShowing.isChecked) "Y" else "N"
        request.index = if ("" == mIndex.text.toString()) 0 else Integer.parseInt(mIndex.text.toString())
        request.gameId = mGameId
        val result = mOrmHelper.eventInfoDao.createOrUpdate(request)
        if (result.isCreated || result.isUpdated) {
            Toast.makeText(baseContext, if (result.isCreated) "新增成功" else "更新成功", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@EventInfoActivity, EventsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("game", mGameId)
            startActivity(intent)
            this.finish()
        }
    }


    @OnClick(R.id.btnDel)
    internal fun onDelClickHandler() {
        val time = java.lang.Long.valueOf(mBtnDel.tag.toString())
        val timeNow = Calendar.getInstance().time.time
        if (Math.abs(timeNow - java.lang.Long.valueOf(time)) > 5000) {
            Toast.makeText(baseContext, "请再次点击删除", Toast.LENGTH_SHORT).show()
            mBtnDel.tag = timeNow
        } else {
            mBtnDel.tag = 0
            delEvent()
        }
    }

    @OnClick(R.id.btnDel2)
    internal fun onImagesDel() {
        for (i in 0 until mImagesView!!.size()) {
            if (mImagesView!!.valueAt(i).btnDel.visibility == View.GONE) {
                mImagesView!!.valueAt(i).btnDel.visibility = View.VISIBLE
                mImagesView!!.valueAt(i).btnAdjust.visibility = View.VISIBLE
                mImagesView!!.valueAt(i).indexEt.visibility = View.VISIBLE
            } else {
                mImagesView!!.valueAt(i).btnDel.visibility = View.GONE
                mImagesView!!.valueAt(i).btnAdjust.visibility = View.GONE
                mImagesView!!.valueAt(i).indexEt.visibility = View.GONE
            }
        }
    }


    @OnClick(R.id.btnAdd)
    internal fun onAddImages() {
        if (mEventId == 0)
            return
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventinfo)
        ButterKnife.bind(this)

        mBtnDel.tag = 0
        mEventId = intent.getIntExtra("event", 0)
        mGameId = intent.getIntExtra("game", 0)
        mResourceController = ResourceController(this, mGameId)
    }

    override fun onResume() {
        super.onResume()
        if (mEventId > 0)
            mainSearch()
    }

    private fun mainSearch() {
        val result = mOrmHelper.eventInfoDao.queryForId(mEventId)

        mName.setText(result.name)
        mDuration.setText(result.duration)
        mContent.setText(result.content)
        mIndex.setText(result.index.toString())
        mCbShowing.isChecked = "Y" == result.showing
        showImages()
    }

    private fun showImages() {
        mImagesFiles = SparseArray()
        mImagesView = SparseArray()
        mLLImages.removeAllViews()
        if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState()) {
            val fileDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_EVENT_PATH + "/" + mGameId)
            var index = 0
            while (++index < 20) {
                val imageFile = File(fileDir.path, CommonUtil.getImageFrontName(mEventId, index))
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

                    val imageBox = ImageBox(this@EventInfoActivity, mLLImages)
                    mLLImages.addView(imageBox.view)
                    mImagesView!!.append(index, imageBox)

                    if (mResourceController!!.eventImagesGap) {
                        mLLImages.post {
                            val layoutParams = imageBox.view.layoutParams as LinearLayout.LayoutParams
                            layoutParams.setMargins(0, 0, 0, 20)
                            imageBox.view.layoutParams = layoutParams
                        }
                    }

                    val oldIndex = index

                    val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameId, false)
                    imageBox.imageView.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bitmap!!, 90) else bitmap)
                    imageBox.tvDate.text = CommonUtil.getFileLastModified(imageFile)
                    imageBox.btnAdjust.setOnClickListener {
                        val intent = Intent(this@EventInfoActivity, ImageAdjustActivity::class.java)
                        intent.putExtra("source", mImagesFiles!!.get(oldIndex).absolutePath)
                        startActivity(intent)
                    }

                    imageBox.btnDel.tag = "$index*0"
                    imageBox.btnDel.setOnClickListener { v ->
                        val tag = v.tag.toString().split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val key = Integer.parseInt(tag[0])
                        val line = mImagesView!!.get(key).view
                        val timenow = Calendar.getInstance().time.time
                        if (Math.abs(timenow - java.lang.Long.valueOf(tag[1])) > 5000) {
                            Toast.makeText(this@EventInfoActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                            v.tag = "$key*$timenow"
                        } else {
                            v.tag = "$key*0"
                            CommonUtil.deleteImage(this@EventInfoActivity, mImagesFiles!!.get(key))
                            mLLImages.removeView(line)
                            mImagesView!!.remove(key)
                            mImagesFiles!!.remove(key)
                            Toast.makeText(this@EventInfoActivity, "删除成功", Toast.LENGTH_SHORT).show()
                        }
                    }

                    imageBox.indexEt.setText(index.toString())
                    imageBox.indexEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                        if (hasFocus)
                            return@OnFocusChangeListener
                        val editText = v as EditText
                        val newIndex = Integer.parseInt(editText.text.toString())
                        val endIndex = mImagesView!!.keyAt(mImagesView!!.size() - 1)
                        if (newIndex == oldIndex)
                            return@OnFocusChangeListener
                        when {
                            newIndex > endIndex -> CommonUtil.renameFile(mImagesFiles!!.get(oldIndex), CommonUtil.getImageFrontName(mEventId, endIndex + 1))
                            mImagesFiles!!.get(newIndex) == null -> CommonUtil.renameFile(mImagesFiles!!.get(oldIndex), CommonUtil.getImageFrontName(mEventId, newIndex))
                            else -> {
                                for (i in mImagesFiles!!.size() - 1 downTo 0) {
                                    val key = mImagesFiles!!.keyAt(i)
                                    if (newIndex > oldIndex) { //向下移
                                        if (key >= newIndex)
                                            CommonUtil.renameFile(mImagesFiles!!.get(key), CommonUtil.getImageFrontName(mEventId, key + 1))
                                    } else { //向上移
                                        if (key > oldIndex)
                                            CommonUtil.renameFile(mImagesFiles!!.get(key), CommonUtil.getImageFrontName(mEventId, key + 1))
                                        else if (key == oldIndex)
                                            CommonUtil.renameFile(mImagesFiles!!.get(newIndex), CommonUtil.getImageFrontName(mEventId, key + 1))
                                    }
                                }
                                CommonUtil.renameFile(mImagesFiles!!.get(oldIndex), CommonUtil.getImageFrontName(mEventId, newIndex))
                            }
                        }
                        showImages()
                    }
                }
            }
        }
    }

    private fun delEvent() {
        val result = mOrmHelper.eventInfoDao.deleteById(mEventId).toLong()
        if (result > -1) {
            Toast.makeText(baseContext, "删除成功", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@EventInfoActivity, EventsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("game", mGameId)
            startActivity(intent)
            this.finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_PIC_BY_PICK_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                /**
                 * 当选择的图片不为空的话，在获取到图片的途径
                 */
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
                            createImages(bitmap)
                            showImages()
                        } else {
                            alert()
                        }
                    } else {
                        alert()
                    }

                } catch (e: Exception) {
                }finally {
                    cursor?.close()
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun alert() {
        Toast.makeText(this, "您选择的不是有效的图片", Toast.LENGTH_SHORT).show()
    }

    private fun createImages(bitmap: Bitmap) {
        val mImagesFileDir = File(
                Environment.getExternalStorageDirectory(),
                MConfig.SD_EVENT_PATH + "/" + mGameId)
        if (!mImagesFileDir.exists()) {
            mImagesFileDir.mkdirs()
        }

        var imageFile: File
        val bos: FileOutputStream
        var checknum = 1
        while (true) {
            imageFile = File(mImagesFileDir.path,
                    CommonUtil.getImageFrontName(mEventId, checknum))
            if (!imageFile.exists())
                break
            else
                checknum++
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
        }

    }

    internal inner class ImageBox(context: Activity, parent:ViewGroup) {

        @BindView(R.id.imgDetails)
        lateinit var imageView: ImageView

        @BindView(R.id.btnDel)
        lateinit var btnDel: ImageButton

        @BindView(R.id.btnAdjust)
        lateinit var btnAdjust: ImageButton

        @BindView(R.id.tvDate)
        lateinit var tvDate: TextView

        @BindView(R.id.etSeq)
        lateinit var indexEt: EditText

        var view: View = LayoutInflater.from(context).inflate(
                R.layout.child_images, parent, false)

        init {
            ButterKnife.bind(this, view)
        }
    }

    companion object {
        private const val SELECT_PIC_BY_PICK_PHOTO = 10
    }

}
