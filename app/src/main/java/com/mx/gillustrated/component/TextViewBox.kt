package com.mx.gillustrated.component


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mx.gillustrated.R


class TextViewBox(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val containerLength = mutableListOf(0)
    val mCallback:Callback? = null
    var maxWidth:Int = 0


//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        //setMeasuredDimension(600, heightMeasureSpec)
//    }

    fun setFixedWidth(width:Int){
        maxWidth = width
        Log.d("CCCCCCCCC", "$maxWidth"  )
    }

    fun setDataProvider(data:List<String>, callback:Callback? ){
        this.removeAllViews()
        val parent = createParent()
        this.addView(parent)
        data.forEachIndexed { index, s ->
            parent.addView(createTextView(s, index))
        }
        parent.measure(0,0)
        this.measure(600,200)
        Log.d("AAAAAAAB", "${parent.measuredWidth} $maxWidth"  )
    }

    private fun createParent():LinearLayout{
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL
        linearLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return linearLayout
    }

    private fun createTextView(text:String, index:Int):TextView{
        val textView = TextView(context)
        val param = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        param.marginEnd = 10
        textView.layoutParams = param
        textView.text = text

        textView.setOnClickListener {
            mCallback?.onClick(index)
        }
        return textView
    }

    interface Callback{
        fun onClick(index:Int)
    }
}