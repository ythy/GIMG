package com.mx.gillustrated.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.view.ViewGroup
import android.widget.TextView


class TextViewBox(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mCallback:Callback? = null
    private var maxWidth:Int = 0
    private var mGap:Int = 10

    fun setFixedWidth(width:Int){
        maxWidth = width
    }

    fun setCallback(callback: Callback){
        mCallback = callback
    }

    fun setGap(gap: Int){
        mGap = gap
    }


    fun setDataProvider(data:List<String>, color:List<String>?){

        this.removeAllViews()
        var parent = createParent()
        this.addView(parent)
        data.forEachIndexed { index, s ->
            val text = createTextView(s, index, index == 0, color?.get(index))
            parent.addView(text)
            parent.measure(0,0)
            if(parent.measuredWidth >= maxWidth){
                parent.removeView(text)
                parent = createParent(false)
                this.addView(parent)
                val newText = createTextView(s, index, true, color?.get(index))
                parent.addView(newText)
            }
        }
    }

    private fun createParent(top:Boolean = true):LinearLayout{
        val linearLayout = LinearLayout(context)
        val param = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if(!top){
            param.topMargin = 5
        }
        linearLayout.orientation = HORIZONTAL
        linearLayout.layoutParams = param
        return linearLayout
    }

    private fun createTextView(text:String, index:Int, isStart:Boolean, color:String?):TextView{
        val textView = TextView(context)
        val param = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.layoutParams = param
        textView.text = text
        if(!isStart)
            param.marginStart = mGap
        if(color != null)
            textView.setTextColor(Color.parseColor(color))

        textView.setOnClickListener {
            mCallback?.onClick(index)
        }
        return textView
    }

    interface Callback{
        fun onClick(index:Int)
    }
}