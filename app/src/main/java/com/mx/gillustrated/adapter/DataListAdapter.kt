package com.mx.gillustrated.adapter

import java.io.File

import com.mx.gillustrated.R
import com.mx.gillustrated.activity.BaseActivity
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.ResourceController
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.vo.CardInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

class DataListAdapter constructor(private val mContext: MainActivity, private val list: List<CardInfo>) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(arg0: Int): Any {
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

    override fun getView(arg0: Int, convertViews: View?, arg2: ViewGroup): View {
        var convertView = convertViews
        lateinit var component: Component

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.adapter_mainlist, arg2, false)
            component = Component(convertView)
            convertView.tag = component

            val resourceController = ResourceController(mContext, list[arg0].gameId)
            if ("E1" == resourceController.number4) {
                component.ivExtraGap1.visibility = View.GONE
                component.tvExtra1.visibility = View.GONE
            }
            if ("E2" == resourceController.number5) {
                component.ivExtraGap2.visibility = View.GONE
                component.tvExtra2.visibility = View.GONE
            }
            if (!mContext.mSP.getBoolean(BaseActivity.SHARE_SHOW_COST_COLUMN + list[arg0].gameId, false)) {
                component.ivCostGap.visibility = View.GONE
                component.tvCost.visibility = View.GONE
            }
        } else {
            component = convertView.tag as Component
        }

        val hasHeader = "Y" == list[arg0].profile
        val showHeader = mContext.mSP.getBoolean(BaseActivity.SHARE_SHOW_HEADER_IMAGES + list[arg0].gameId, false)
        if (showHeader && hasHeader) {
            component.tvTotal.visibility = View.GONE
            component.ivHeader.visibility = View.VISIBLE
            try {
                val imageDir = File(Environment.getExternalStorageDirectory(), MConfig.SD_HEADER_PATH + "/" + list[arg0].gameId)
                val file = File(imageDir.path, list[arg0].id.toString() + "_h.png")
                if (file.exists()) {
                    val bmp = MediaStore.Images.Media.getBitmap(mContext.contentResolver, Uri.fromFile(file))
                    component.ivHeader.setImageBitmap(bmp)
                } else
                    component.ivHeader.setImageBitmap(null)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (showHeader) {
            component.tvTotal.visibility = View.GONE
            component.ivHeader.visibility = View.GONE

        } else {
            component.tvTotal.visibility = View.VISIBLE
            component.ivHeader.visibility = View.GONE
            if ("" != list[arg0].maxHP &&
                    "" != list[arg0].maxAttack &&
                    "" != list[arg0].maxDefense &&
                    "" != list[arg0].extraValue1 &&
                    "" != list[arg0].extraValue2 &&
                    CommonUtil.isNumeric2(list[arg0].maxHP) &&
                    CommonUtil.isNumeric2(list[arg0].maxAttack) &&
                    CommonUtil.isNumeric2(list[arg0].maxDefense) &&
                    CommonUtil.isNumeric2(list[arg0].extraValue1) &&
                    CommonUtil.isNumeric2(list[arg0].extraValue2)) {
                val total = Integer.parseInt(list[arg0].maxHP!!) +
                        Integer.parseInt(list[arg0].maxAttack!!) +
                        Integer.parseInt(list[arg0].maxDefense!!) +
                        Integer.parseInt(list[arg0].extraValue1!!) +
                        Integer.parseInt(list[arg0].extraValue2!!)
                component.tvTotal.text = total.toString()
            }


        }

        component.tvName.text = list[arg0].name
        component.tvFrontName.text = list[arg0].frontName
        val attr = list[arg0].attr
        component.tvAttr.text = attr
        component.tvCost.text = list[arg0].cost.toString()
        component.tvHP.text = list[arg0].maxHP.toString()
        component.tvAttack.text = list[arg0].maxAttack.toString()
        component.tvDefense.text = list[arg0].maxDefense.toString()
        if (list[arg0].extraValue1 != null)
            component.tvExtra1.text = list[arg0].extraValue1.toString()
        else
            component.tvExtra1.text = ""
        if (list[arg0].extraValue2 != null)
            component.tvExtra2.text = list[arg0].extraValue2.toString()
        else
            component.tvExtra2.text = ""

        component.tvNid.text = list[arg0].nid.toString()
        if (list[arg0].nid > 0)
            component.tvNid.visibility = View.VISIBLE
        else
            component.tvNid.visibility = View.GONE

        return convertView!!
    }


    internal class Component(view: View) {

        @BindView(R.id.ivHeader)
        lateinit var ivHeader: ImageView

        @BindView(R.id.tvFrontName)
        lateinit var tvFrontName: TextView

        @BindView(R.id.tvName)
        lateinit var tvName: TextView

        @BindView(R.id.tvAttr)
        lateinit var tvAttr: TextView

        @BindView(R.id.tvCost)
        lateinit var tvCost: TextView

        @BindView(R.id.tvHP)
        lateinit var tvHP: TextView

        @BindView(R.id.tvAttack)
        lateinit var tvAttack: TextView

        @BindView(R.id.tvDefense)
        lateinit var tvDefense: TextView

        @BindView(R.id.tvExtra1)
        lateinit var tvExtra1: TextView

        @BindView(R.id.tvExtra2)
        lateinit var tvExtra2: TextView

        @BindView(R.id.tvNid)
        lateinit var tvNid: TextView

        @BindView(R.id.tvTotal)
        lateinit var tvTotal: TextView

        @BindView(R.id.ivExtra1Gap)
        lateinit var ivExtraGap1: ImageView

        @BindView(R.id.ivExtra2Gap)
        lateinit var ivExtraGap2: ImageView

        @BindView(R.id.ivCostGap)
        lateinit var ivCostGap: ImageView

        init {
            ButterKnife.bind(this, view)
        }


    }
}
