package com.mx.gillustrated.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.util.forEach
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.ResourceController
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.EventInfo
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import com.mx.gillustrated.databinding.ActivityEventinfoBinding
import com.mx.gillustrated.databinding.ChildImagesBinding
import kotlin.math.abs

/**
 * Created by maoxin on 2017/2/22.
 */

class EventInfoActivity : BaseActivity() {

    private var mEventId: Int = 0
    private var mGameId: Int = 0
    private var mImagesFiles: SparseArray<File>? = null
    private var mImagesView: SparseArray<ChildImagesBinding>? = null
    private var mResourceController: ResourceController? = null
    lateinit var binding:ActivityEventinfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
        binding.btnDel.tag = 0
        mEventId = intent.getIntExtra("event", 0)
        mGameId = intent.getIntExtra("game", 0)
        mResourceController = ResourceController(this, mGameId)
    }

    override fun onResume() {
        super.onResume()
        if (mEventId > 0)
            mainSearch()
    }

    private fun initListener(){
        binding.btnSave.setOnClickListener {
            val request = EventInfo()
            request.id = mEventId
            request.name = binding.etDetailName.text.toString()
            request.duration = binding.etDetailTime.text.toString()
            request.content = binding.etDetailContent.text.toString()
            request.showing = if (binding.cbShowing.isChecked) "Y" else "N"
            request.index = if ("" == binding.etIndex.text.toString()) 0 else Integer.parseInt(binding.etIndex.text.toString())
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
        binding.btnDel.setOnClickListener {
            val time = java.lang.Long.valueOf(it.tag.toString())
            val timeNow = Calendar.getInstance().time.time
            if (abs(timeNow - java.lang.Long.valueOf(time)) > 5000) {
                Toast.makeText(baseContext, "请再次点击删除", Toast.LENGTH_SHORT).show()
                it.tag = timeNow
            } else {
                it.tag = 0
                delEvent()
            }
        }
        binding.btnDel2.setOnClickListener {
            mImagesView?.forEach { _, value ->
                if (value.btnDel.visibility == View.GONE) {
                    value.btnDel.visibility = View.VISIBLE
                    value.btnAdjust.visibility = View.VISIBLE
                    value.etSeq.visibility = View.VISIBLE
                } else {
                    value.btnDel.visibility = View.GONE
                    value.btnAdjust.visibility = View.GONE
                    value.etSeq.visibility = View.GONE
                }
            }
        }
        binding.btnAdd.setOnClickListener {
            if (mEventId == 0)
                return@setOnClickListener
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            this.startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO)
        }

    }


    private fun mainSearch() {
        val result = mOrmHelper.eventInfoDao.queryForId(mEventId)

        binding.etDetailName.setText(result.name)
        binding.etDetailTime.setText(result.duration)
        binding.etDetailContent.setText(result.content)
        binding.etIndex.setText(result.index.toString())
        binding.cbShowing.isChecked = "Y" == result.showing
        showImages()
    }

    private fun showImages() {
        mImagesFiles = SparseArray()
        mImagesView = SparseArray()
        binding.llImages.removeAllViews()
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
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, Uri.fromFile(imageFile)))
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val imageBox = ChildImagesBinding.inflate(layoutInflater, binding.llImages, true)
                    mImagesView!!.append(index, imageBox)

                    if (mResourceController!!.eventImagesGap) {
                        binding.llImages.post {
                            val layoutParams = imageBox.root.layoutParams as LinearLayout.LayoutParams
                            layoutParams.setMargins(0, 0, 0, 20)
                            imageBox.root.layoutParams = layoutParams
                        }
                    }

                    val oldIndex = index

                    val isOrientation = mSP.getBoolean(SHARE_IMAGE_ORIENTATION_EVENT + mGameId, false)
                    imageBox.imgDetails.setImageBitmap(if (isOrientation) CommonUtil.rotatePic(bitmap!!, 90) else bitmap)
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
                        val line = mImagesView!!.get(key).root
                        val timenow = Calendar.getInstance().time.time
                        if (abs(timenow - java.lang.Long.valueOf(tag[1])) > 5000) {
                            Toast.makeText(this@EventInfoActivity, "请再次点击删除", Toast.LENGTH_SHORT).show()
                            v.tag = "$key*$timenow"
                        } else {
                            v.tag = "$key*0"
                            deleteImages(mImagesFiles!!.get(key), object : OnCallback{
                                override fun deleted() {
                                    binding.llImages.removeView(line)
                                    mImagesView!!.remove(key)
                                    mImagesFiles!!.remove(key)
                                    Toast.makeText(this@EventInfoActivity, "删除成功", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }

                    imageBox.etSeq.setText(index.toString())
                    imageBox.etSeq.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
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

                        if (path.lowercase().endsWith("jpg") || path.lowercase().endsWith("png") ||
                                path.lowercase().endsWith("jpeg")) {
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


    companion object {
        private const val SELECT_PIC_BY_PICK_PHOTO = 10
    }

}
