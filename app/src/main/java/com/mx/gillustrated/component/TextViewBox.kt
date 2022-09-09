package com.mx.gillustrated.component


import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.view.LayoutInflater
import com.mx.gillustrated.R


class TextViewBox(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

      init {
          val layoutInflater = LayoutInflater.from(context)
          layoutInflater.inflate(R.layout.component_textbox, this)

      }

    fun setDataProvider(data:List<String>, callback:Callback? ){

    }

    private fun createParent():LinearLayout?{
       val linearLayout = LinearLayout(context)
        //linearLayout.
        return null
    }

    interface Callback{
        fun onClick(index:Int)
    }
}