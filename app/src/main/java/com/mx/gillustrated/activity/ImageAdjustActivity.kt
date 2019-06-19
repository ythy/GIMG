package com.mx.gillustrated.activity

import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.mx.gillustrated.R
import java.io.File



/**
 * Created by maoxin on 2019/6/18.
 */
class ImageAdjustActivity : BaseActivity() {

    enum class AnimateType{
        MOVE, RESIZE, NONE
    }
    val MAX_SCALE_WIDTH:Int = 8000
    val MIN_SCALE_WIDTH:Int = 500
    val MIN_SCALE_START_WIDTH:Int = 10

    val TAG:String = javaClass.name

    @BindView(R.id.image)
    lateinit var mImage:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_image_adjust)
        ButterKnife.bind(this)
        this.initView(this.intent.getStringExtra("source"))
    }

    fun initView(filePath:String){
        val bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, Uri.fromFile(File(filePath)))
        mImage.setImageBitmap(bitmap)
        mImage.post {
            initImageListeners()
        }
    }

    fun initImageListeners(){
        var lastPointToView: PointF = PointF()
        var lastSpace: Float = Float.MIN_VALUE
        var animateType:AnimateType = AnimateType.NONE
        val originImageWidth = mImage.width

        mImage.setOnTouchListener { v, event ->
            when ( event.action and MotionEvent.ACTION_MASK ) {
                MotionEvent.ACTION_DOWN -> {
                    lastPointToView = PointF( mImage.x - event.rawX , mImage.y - event.rawY)
                    animateType = AnimateType.MOVE
                }
                MotionEvent.ACTION_MOVE -> {
                    when ( animateType ){
                        AnimateType.MOVE -> move(mImage, event.rawX + lastPointToView.x, event.rawY + lastPointToView.y)
                        AnimateType.RESIZE ->{
                            val space = spacing(event) - lastSpace
                            val lastWidth:Float = originImageWidth * mImage.scaleX;
                            if(Math.abs(space) > MIN_SCALE_START_WIDTH && lastWidth + space < MAX_SCALE_WIDTH && lastWidth + space > MIN_SCALE_WIDTH ){
                                resize(mImage, (lastWidth + space ) / originImageWidth  )
                            }
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
                else ->
                    false
            }
            true
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt(x.toDouble() * x.toDouble() + y * y).toFloat()
    }

    fun move(view: View, x:Float, y:Float){
        view.animate()
                .x(x)
                .y(y)
                .setDuration(0)
                .start();
    }

    fun resize(view: View, scale:Float){
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(0)
                .start();
    }


}