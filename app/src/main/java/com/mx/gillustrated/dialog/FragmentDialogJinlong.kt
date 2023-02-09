package com.mx.gillustrated.dialog

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.JingLongData
import com.mx.gillustrated.component.JingLongData.Talk
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.io.File
import java.util.*

class FragmentDialogJinlong constructor(private val mId:String)  : DialogFragment() {

    companion object{
        fun newInstance(id:String): FragmentDialogJinlong {
            return FragmentDialogJinlong(id)
        }
    }

    @BindView(R.id.iv_profile)
    lateinit var mImage:ImageView

    @BindView(R.id.tv_name)
    lateinit var mName:TextView

    @BindView(R.id.tv_content)
    lateinit var mContent:TextView

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        this.dismiss()
    }

    var count = 0
    private val mStart = Talk.filterIndexed { index, _ -> listOf(3,4,6,9,16,17,18,28,29,56,57,60,62,63.150,152,153).contains(index) || index in 30..36
            || index in 42..50 || index in 85..90 || index in 98..107 || index in 113..119 || index in 130..147 }
    private val mData = mutableListOf(
            mutableListOf(),
            Talk.filterIndexed { index, _ -> listOf(58,59,64,65,91,92,120,121,148,149,151,154,155).contains(index) || index in 21..27  || index in 72..80 ||
                    index in 82..84 },
            Talk.filterIndexed { index, _ -> listOf(5,7,8).contains(index) ||  index in 37..41 || index in 51..55 || index in 66..70
                    ||  index in 93..97 || index in 108..112 || index in 122..127  }
    )
    private val mEnding = mutableListOf("\u610F\u72B9\u672A\u5C3D...", "\u547B\u541F...", "\u762B\u8F6F...")

    @OnClick(R.id.btn_reward)
    fun onRewardHandler(){
        mPerson.feiziXiuwei += 10
        mName.text = CultivationHelper.showing("${mPerson.name}-${mPerson.feiziXiuwei}")
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(convertTalk(Talk[2]))
    }

    @OnClick(R.id.btn_punish)
    fun onPunishHandler(){
        mPerson.feiziXiuwei = Math.max(0, mPerson.feiziXiuwei - 10)
        mName.text = CultivationHelper.showing("${mPerson.name}-${mPerson.feiziXiuwei}")
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(convertTalk(Talk.filterIndexed { index, _ -> index in 12..14 }.shuffled()[0]))
    }


    @OnClick(R.id.btn_ml)
    fun onMLHandler(){
        if (count == 3)
            return
        if (count == 0){
            val mlText = "\u4E0E${mPerson.name}\u82B1\u524D\u6708\u4E0B..."
            mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(mlText)
            count++
            return
        }
        val content = mData[count].shuffled()[0]
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(convertTalk(content))
        count++
        //结束奖励
        if (count == 3){
            val random = Random().nextInt(3)
            mPerson.feiziXiuwei += 10 * (random + 1)
            mName.text = CultivationHelper.showing("${mPerson.name}-${mPerson.feiziXiuwei}")
            val bonusText = "${mPerson.name}${mEnding[random]}, \u6B22\u6109+${10 * (random + 1)}"
            mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(bonusText)
        }
    }

    fun convertTalk(input:String):String{
        return  input.replace("QIE", "\u81E3\u59BE")
                .replace("CHEN", "\u81E3")
                .replace("HUANG", "\u7687\u4E0A")
                .replace("BIXIA", "\u965B\u4E0B")
    }

    lateinit var mPerson: Person

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val v = inflater.inflate(R.layout.fragment_dialog_jinlong, container, false)
        ButterKnife.bind(this, v)
        init()
        return v
    }

    fun init(){
        val context = activity as CultivationActivity
        mPerson = context.getPersonData(mId)!!
        mName.text = CultivationHelper.showing("${mPerson.name}-${mPerson.feiziXiuwei}")
        val content = mStart.shuffled()[0]
        mContent.text = CultivationHelper.showing(convertTalk(content))
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + NameUtil.Gender.Female)
            var file = File(imageDir.path, "${mPerson.profile}.png")
            if (!file.exists()) {
                file = File(imageDir.path, "${mPerson.profile}.jpg")
            }
            if (file.exists()) {
                val bmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, Uri.fromFile(file))
                mImage.setImageBitmap(bmp)
            } else
                mImage.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}