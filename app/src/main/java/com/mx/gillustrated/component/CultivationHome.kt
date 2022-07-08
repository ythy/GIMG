package com.mx.gillustrated.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import androidx.core.graphics.ColorUtils
import com.mx.gillustrated.R
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.util.CommonUtil
import java.util.*

@SuppressLint("NewApi")
class CultivationHome constructor(context:Context) : View(context){

    private val mMatrix: Matrix = Matrix()
    private val mMatrixChar: Matrix = Matrix()
    private val mPaint: Paint = Paint()
    private val mSceneSize:Point = Point(CommonUtil.dip2px(context, 280f),
            CommonUtil.dip2px(context, 280f))

    private val mBitmap1 = BitmapFactory.decodeResource(context.resources, R.drawable.scene1)
    private val mBitmap2 = BitmapFactory.decodeResource(context.resources, R.drawable.scene2)
    private val mBitmapCharOrigin = BitmapFactory.decodeResource(context.resources, R.drawable.char2)
    private val mBitmapChar:Bitmap

    private var mBitmap:Bitmap
    private var mPointFChar:PointF
    private var mBackgroundScale:Float
    private var mBackgroundPointF:PointF

    init {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        //mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        val random = Random()
        val bitmapCount = random.nextInt(1)
        mBitmap = when(bitmapCount){
            0 -> mBitmap1
            1 -> mBitmap2
            else -> mBitmap1
        }
        mBackgroundScale = when(bitmapCount){
            0 -> 2.5f
            1 -> 4f
            else -> 2f
        }
        mBackgroundPointF = PointF(random.nextInt((mBitmap.width * mBackgroundScale).toInt() - mSceneSize.x).toFloat(),
                random.nextInt((mBitmap.height * mBackgroundScale).toInt() - mSceneSize.y).toFloat())

        mBitmapChar = mBitmapCharOrigin.copy(Bitmap.Config.ARGB_8888, true)
        fillCharColor(mBitmapChar, "#D22E59" )
        mPointFChar = PointF(random.nextInt(mSceneSize.x - mBitmapChar.width / 2).toFloat(),
                random.nextInt(mSceneSize.y - mBitmapChar.height / 2).toFloat())


    }

    private fun fillCharColor(image: Bitmap, replacement: String) {
        val width = image.width
        val height = image.height
        val replaceColor = Color.parseColor(replacement)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = image.getPixel(x, y)
                if(pixel < 0) {
                    image.setPixel(x, y, Color.argb(150,
                            Color.red(replaceColor), Color.green(replaceColor), Color.blue(replaceColor)))
                }
            }
        }
    }

    //1200 883
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mMatrix.setScale(mBackgroundScale, mBackgroundScale, 0f, 0f)
        mMatrix.postTranslate(-mBackgroundPointF.x, -mBackgroundPointF.y)
        canvas.drawBitmap(mBitmap, mMatrix, mPaint)

        mMatrixChar.setScale(0.5f, 0.5f)
        mMatrixChar.postTranslate(mPointFChar.x, mPointFChar.y)
        canvas.drawBitmap(mBitmapChar, mMatrixChar, mPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mSceneSize.x, mSceneSize.y)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mBitmap1.recycle()
        mBitmap2.recycle()
        mBitmapCharOrigin.recycle()
        mBitmapChar.recycle()
    }

}