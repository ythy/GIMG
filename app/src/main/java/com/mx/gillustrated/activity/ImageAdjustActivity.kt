package com.mx.gillustrated.activity

import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import java.io.File
import android.graphics.Bitmap
import android.view.View.OnTouchListener
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.MatrixInfo
import com.mx.gillustrated.databinding.ActivityImageAdjustBinding
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
    lateinit var binding:ActivityImageAdjustBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageAdjustBinding.inflate(layoutInflater)
        this.setContentView(binding.root)
        originImagePath = this.intent.getStringExtra("source") ?: ""
        this.initView()
    }

    private fun showCutImage(){
        binding.imagePreview.setImageBitmap(cutImageBitMap)
        binding.imagePreview.visibility = View.VISIBLE
        binding.btnCutCancle.visibility =  View.VISIBLE
        binding.image.visibility = View.GONE
        binding.cut.visibility = View.GONE
    }

    private fun initView(){
        binding.btnCutCancle.setOnClickListener {
            binding.imagePreview.visibility = View.GONE
            binding.btnCutCancle.visibility =  View.GONE
            binding.image.visibility = View.VISIBLE
            binding.cut.visibility = View.VISIBLE
        }
        binding.btnCutSave.setOnClickListener {
            if(binding.imagePreview.visibility == View.VISIBLE){
                CommonUtil.exportImgFromBitmap(cutImageBitMap,  File(originImagePath))
                this.finish()
            }else{
                //Matrix  0 4 缩放   2 5 位移
                val imageMatrixArray = FloatArray(9)
                binding.image.imageMatrix.getValues(imageMatrixArray)
                //matrixInfo.x 说明
                //由于cut缩放导致cutX需要调整回原始X
                //mImage缩放同样问题，缩放后的X是不变的，需要把x改成目测X
                //最后除以缩放的倍数
                val matrixInfo = MatrixInfo()
                matrixInfo.x =
                    ((binding.cut.x - (binding.cut.width * binding.cut.scaleX - binding.cut.width) / 2 - (binding.image.x + binding.image.width * (1 - binding.image.scaleX) / 2 + imageMatrixArray[2] * binding.image.scaleX)) / binding.image.scaleX / imageMatrixArray[0]).roundToInt()
                matrixInfo.y =
                    ((binding.cut.y - (binding.cut.height * binding.cut.scaleY - binding.cut.height) / 2 - (binding.image.y + binding.image.height * (1 - binding.image.scaleY) / 2 + imageMatrixArray[5] * binding.image.scaleY)) / binding.image.scaleY / imageMatrixArray[4]).roundToInt()
                matrixInfo.width =
                    (binding.cut.width * binding.cut.scaleX / binding.image.scaleX / imageMatrixArray[0]).roundToInt()
                matrixInfo.height =
                    (binding.cut.height * binding.cut.scaleY / binding.image.scaleY / imageMatrixArray[4]).roundToInt()

                cutImageBitMap = CommonUtil.cutBitmap(originImage, matrixInfo, false)
                showCutImage()
            }
        }

        originImage = MediaStore.Images.Media.getBitmap(
                this.contentResolver, Uri.fromFile(File(originImagePath)))
        binding.image.setImageBitmap(originImage)
        binding.image.post {
            binding.image.setOnTouchListener(mImagesOnTouchListener)
        }
        binding.cut.post {
            binding.cut.setOnTouchListener(mCutImagesOnTouchListener)
        }
    }

    private val mImagesOnTouchListener = object : OnTouchListener{

        var lastPointToView = PointF()
        var lastSpace: Float = Float.MIN_VALUE
        var animateType:AnimateType = AnimateType.NONE

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when ( event.action and MotionEvent.ACTION_MASK ) {
                MotionEvent.ACTION_DOWN -> {
                    lastPointToView = PointF( binding.image.x - event.rawX , binding.image.y - event.rawY)
                    animateType = AnimateType.MOVE
                }
                MotionEvent.ACTION_MOVE -> {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when ( animateType ){
                        AnimateType.MOVE -> move(binding.image, event.rawX + lastPointToView.x, event.rawY + lastPointToView.y)
                        AnimateType.RESIZE ->{
                            val space = spacing(event) - lastSpace
                            val lastWidth:Float = binding.image.width * binding.image.scaleX
                            if(abs(space) > MIN_SCALE_START_WIDTH && lastWidth + space < MAX_SCALE_WIDTH && lastWidth + space > MIN_SCALE_WIDTH ){
                                resize(binding.image, (lastWidth + space ) / binding.image.width  )
                            }
                        }else -> {

                        }
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
                    val cutX = binding.cut.x - event.rawX
                    val cutY = binding.cut.y - event.rawY
                    //开始判断点击位置
                    if(event.x >= binding.cut.width - 100 && event.y >= binding.cut.height - 100 ){
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
                        AnimateType.MOVE -> move(binding.cut, event.rawX + lastPointToView.x, event.rawY + lastPointToView.y)
                        AnimateType.RESIZE -> {
                            val scaleX = ( binding.cut.width * binding.cut.scaleX  + event.rawX - lastPoint.x ) / binding.cut.width
                            val scaleY = ( binding.cut.height * binding.cut.scaleY  + event.rawY - lastPoint.y ) / binding.cut.height
                            resize(binding.cut, scaleX, scaleY, binding.cut.x + (event.rawX - lastPoint.x )/2, binding.cut.y + (event.rawY - lastPoint.y )/2 )
                            lastPoint = PointF(event.rawX, event.rawY)
                        }
                        else -> {


                        }
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
        return sqrt(x.toDouble() * x.toDouble() + y * y).toFloat()
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