package com.mx.gillustrated.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout


class AutoWrapLinearLayout : LinearLayout {
    private var mWidth: Int = 0//AutoWrapLinearLayout控件的宽
    private var mHeight: Int = 0//AutoWrapLinearLayout控件的高

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        mWidth = r - l//宽度是跟随父容器而定的

        //自身控件的padding
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom

        //控件自身可以被用来显示自控件的宽度
        val mDisplayWidth = mWidth - paddingLeft - paddingRight

        //自控件的margin
        var cellMarginLeft = 0
        var cellMarginRight = 0
        var cellMarginTop = 0
        var cellMarginBottom = 0

        var x = 0//子控件的默认左上角坐标x
        var y = 0//子控件的默认左上角坐标y

        var totalWidth = 0//计算每一行随着自控件的增加而变化的宽度
        val count = childCount

        var cellWidth = 0//子控件的宽，包含padding
        var cellHeight = 0//自控件的高，包含padding

        var left = 0//子控件左上角的横坐标
        var top = 0//子控件左上角的纵坐标

        var lp: LinearLayout.LayoutParams

        for (j in 0 until count) {
            val childView = getChildAt(j)
            //获取子控件child的宽高
            cellWidth = childView.measuredWidth//测量自控件内容的宽度(这个时候padding有被计算进内)
            cellHeight = childView.measuredHeight//测量自控件内容的高度(这个时候padding有被计算进内)

            lp = childView.layoutParams as LinearLayout.LayoutParams
            cellMarginLeft = lp.leftMargin
            cellMarginRight = lp.rightMargin
            cellMarginTop = lp.topMargin
            cellMarginBottom = lp.bottomMargin

            //动态计算子控件在一行里面占据的宽度
            //预判，先加上下一个要展示的子控件，计算这一行够不够放
            totalWidth += cellMarginLeft + cellWidth + cellMarginRight

            if (totalWidth >= mDisplayWidth) {//不够放的时候需要换行
                y += cellMarginTop + cellHeight + cellMarginBottom//新增一行
                totalWidth = cellMarginLeft + cellWidth + cellMarginRight//这时候这一行的宽度为这个子控件的宽度
                x = 0//重置
            }

            //计算顶点横坐标
            left = x + cellMarginLeft + paddingLeft

            //计算顶点纵坐标
            top = y + cellMarginTop + paddingTop

            //绘制自控件
            childView.layout(left, top, left + cellWidth, top + cellHeight)

            //计算下一个横坐标
            x += cellMarginLeft + cellWidth + cellMarginRight

        }
        mHeight = paddingTop + y + cellMarginTop + cellHeight + cellMarginBottom + paddingBottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val cellWidthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)//让自控件自己按需撑开

        val cellHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)//让自控件自己按需撑开

        val count = childCount
        for (i in 0 until count) {
            val childView = getChildAt(i)
            try {
                childView.measure(cellWidthSpec, cellHeightSpec)
            } catch (e: NullPointerException) {
                //这个异常不影响展示
            }

        }

        setMeasuredDimension(View.resolveSize(mWidth, widthMeasureSpec), View.resolveSize(mHeight, heightMeasureSpec))

    }

    companion object {
        internal val TAG = "AutoWrapLinearLayout"
    }

}