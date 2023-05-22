package com.mx.gillustrated.activity

import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import java.io.File
import android.graphics.Bitmap
import android.view.View.OnTouchListener
import butterknife.OnClick
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.MatrixInfo
import android.widget.ImageButton

/**
 * Created by maoxin on 2019/6/18.
 */
class ImageAdjustActivity : BaseActivity() {

    enum class AnimateType{
        MOVE, RESIZE, NONE
    }

    companion object {
        const val MAX_SCALE_WIDTH:Int = 8000
        const val MIN_SCALE_WIDTH:Int = 500
        const val MIN_SCALE_START_WIDTH:Int = 10
        //val TAG:String = ImageAdjustActivity::class.java.name
    }

    private lateinit var originImage:Bitmap
    private lateinit var originImagePath:String
    private lateinit var cutImageBitMap:Bitmap

    @BindView(R.id.image)
    lateinit var mImage:ImageView

    @BindView(R.id.cut)
    lateinit var mCut:ImageView

    @BindView(R.id.imagePreview)
    lateinit var mImagePreview:ImageView

    @BindView(R.id.btnCutCancle)
    lateinit var mCancel: ImageButton

    @OnClick(R.id.btnCutCancle)
    fun onCancelClick() {
        mImagePreview.visibility = View.GONE
        mCancel.visibility =  View.GONE
        mImage.visibility = View.VISIBLE
        mCut.visibility = View.VISIBLE
    }

    @OnClick(R.id.btnCutSave)
    fun onSaveClick() {
        if(mImagePreview.visibility == View.VISIBLE){
            CommonUtil.exportImgFromBitmap(cutImageBitMap,  File(originImagePath))
            this.finish()
        }else{
            //Matrix  0 4 缩放   2 5 位移
            val imageMatrixArray = FloatArray(9)
            mImage.imageMatrix.getValues(imageMatrixArray)
            //matrixInfo.x 说明
            //由于cut缩放导致cutX需要调整回原始X
            //mImage缩放同样问题，缩放后的X是不变的，需要把x改成目测X
            //最后除以缩放的倍数
            val matrixInfo = MatrixInfo()
            matrixInfo.x = Math.round(( mCut.x - ( mCut.width * mCut.scaleX - mCut.width ) / 2  - ( mImage.x + mImage.width * ( 1 - mImage.scaleX ) / 2 + imageMatrixArray[2] *  mImage.scaleX) ) / mImage.scaleX / imageMatrixArray[0])
            matrixInfo.y = Math.round(( mCut.y - ( mCut.height * mCut.scaleY - mCut.height ) / 2  - ( mImage.y + mImage.height * ( 1 - mImage.scaleY ) / 2 + imageMatrixArray[5] * mImage.scaleY) ) / mImage.scaleY / imageMatrixArray[4])
            matrixInfo.width = Math.round( mCut.width * mCut.scaleX / mImage.scaleX / imageMatrixArray[0] )
            matrixInfo.height = Math.round( mCut.height * mCut.scaleY / mImage.scaleY / imageMatrixArray[4] )

            cutImageBitMap = CommonUtil.cutBitmap(originImage, matrixInfo, false)
            showCutImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_image_adjust)
        ButterKnife.bind(this)
        originImagePath = this.intent.getStringExtra("source") ?: ""
        this.initView()
    }

    private fun showCutImage(){
        mImagePreview.setImageBitmap(cutImageBitMap)
        mImagePreview.visibility = View.VISIBLE
        mCancel.visibility =  View.VISIBLE
        mImage.visibility = View.GONE
        mCut.visibility = View.GONE

    }

    private fun initView(){
        originImage = MediaStore.Images.Media.getBitmap(
                this.contentResolver, Uri.fromFile(File(originImagePath)))
        mImage.setImageBitmap(originImage)
        mImage.post {
            mImage.setOnTouchListener(mImagesOnTouchListener)
        }
        mCut.post {
            mCut.setOnTouchListener(mCutImagesOnTouchListener)
        }
    }

    private val mImagesOnTouchListener = object : OnTouchListener{

        var lastPointToView = PointF()
        var lastSpace: Float = Float.MIN_VALUE
        var animateType:AnimateType = AnimateType.NONE

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when ( event.action and MotionEvent.ACTION_MASK ) {
                MotionEvent.ACTION_DOWN -> {
                    lastPointToView = PointF( mImage.x - event.rawX , mImage.y - event.rawY)
                    animateType = AnimateType.MOVE
                }
                MotionEvent.ACTION_MOVE -> {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when ( animateType ){
                        AnimateType.MOVE -> move(mImage, event.rawX + lastPointToView.x, event.rawY + lastPointToView.y)
                        AnimateType.RESIZE ->{
                            val space = spacing(event) - lastSpace
                            val lastWidth:Float = mImage.width * mImage.scaleX
                            if(Math.abs(space) > MIN_SCALE_START_WIDTH && lastWidth + space < MAX_SCALE_WIDTH && lastWidth + space > MIN_SCALE_WIDTH ){
                                resize(mImage, (lastWidth + space ) / mImage.width  )
                            }
                        }else -> TODO()
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN ->{
                    animateType = AnimateType.RESIZE
                    lastSpace = spacing(event)
                }
                MotionEvent.ACTION_UP ->{
                    animateType = AnimateType.NONE
                }
                MotionEvent.ACTION_POINTER_UP ->{
                    animateType = AnimateType.NONE
                }
            }
            return true
        }
    }


    private val mCutImagesOnTouchListener = object : OnTouchListener {
        var lastPointToView = PointF()
        var lastPoint = PointF()
        var animateType:AnimateType = AnimateType.NONE

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when ( event.action and MotionEvent.ACTION_MASK ) {
                MotionEvent.ACTION_DOWN -> {
                    val cutX = mCut.x - event.rawX
                    val cutY = mCut.y - event.rawY
                    //开始判断点击位置
                    if(event.x >= mCut.width - 100 && event.y >= mCut.height - 100 ){
                        lastPoint = PointF(event.rawX, event.rawY)
                        animateType = AnimateType.RESIZE
                    }else{
                        lastPointToView = PointF(cutX, cutY)
                        animateType = AnimateType.MOVE
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when ( animateType ){
                        AnimateType.MOVE -> move(mCut, event.rawX + lastPointToView.x, event.rawY + lastPointToView.y)
                        AnimateType.RESIZE -> {
                            val scaleX = ( mCut.width * mCut.scaleX  + event.rawX - lastPoint.x ) / mCut.width
                            val scaleY = ( mCut.height * mCut.scaleY  + event.rawY - lastPoint.y ) / mCut.height
                            resize(mCut, scaleX, scaleY, mCut.x + (event.rawX - lastPoint.x )/2, mCut.y + (event.rawY - lastPoint.y )/2 )
                            lastPoint = PointF(event.rawX, event.rawY)
                        }
                        else -> TODO()
                    }
                }
                MotionEvent.ACTION_UP ->{
                    animateType = AnimateType.NONE
                }
                else ->
                    return false
            }
            return true
        }
    }


    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt(x.toDouble() * x.toDouble() + y * y).toFloat()
    }

    private fun move(view: View, x:Float, y:Float){
        view.animate()
                .x(x)
                .y(y)
                .setDuration(0)
                .start()
    }

    private fun resize(view: View, scaleX:Float, scaleY: Float = scaleX, x:Float, y:Float){
        view.animate()
                .x(x)
                .y(y)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(0)
                .start()
    }

    private fun resize(view: View, scaleX:Float, scaleY: Float = scaleX){
        view.animate()
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(0)
                .start()
    }


}