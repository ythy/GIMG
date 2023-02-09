package com.mx.gillustrated.dialog

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.JinLongData.FeiLevel
import com.mx.gillustrated.component.JinLongData.FeiziStep
import com.mx.gillustrated.component.JinLongData.Talk
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

    @BindView(R.id.spinner_level)
    lateinit var mSpinner: Spinner

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        this.dismiss()
    }

    var count = 0
    var mLevelList = mutableListOf<String>()
    private val mStart = Talk.filterIndexed { index, _ -> listOf(3,4,6,9,16,17,18,28,29,56,57,60,62,63.150,152,153).contains(index) || index in 30..36
            || index in 42..50 || index in 85..90 || index in 98..107 || index in 113..119 || index in 130..147 }
    private val mData = mutableListOf(
            mutableListOf(),
            Talk.filterIndexed { index, _ -> listOf(58,59,64,65,91,92,120,121,148,149,151,154,155).contains(index) || index in 21..27  || index in 72..80 ||
                    index in 82..84 },
            Talk.filterIndexed { index, _ -> listOf(5,7,8).contains(index) ||  index in 37..41 || index in 51..55 || index in 66..70
                    ||  index in 93..97 || index in 108..112 || index in 122..127  }
    )
    private val mEnding = mutableListOf("\u610F\u72B9\u672A\u5C3D...", "\u5A07\u541F...", "\u762B\u8F6F...")
    private lateinit var mPerson: Person

    @OnClick(R.id.btn_reward)
    fun onRewardHandler(){
        mPerson.feiziFavor += 100
        showName()
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(convertTalk(Talk[2]))
        setLevelSpinner()
    }

    @OnClick(R.id.btn_punish)
    fun onPunishHandler(){
        mPerson.feiziFavor = Math.max(0, mPerson.feiziFavor - 100)
        showName()
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(convertTalk(Talk.filterIndexed { index, _ -> index in 12..14 }.shuffled()[0]))
        setLevelSpinner()
    }


    @OnClick(R.id.btn_ml)
    fun onMLHandler(){
        if (count == 3)
            return
        if (count == 0){
            val mlText = "\u4E0E${getName()}\u82B1\u524D\u6708\u4E0B..."
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
            mPerson.feiziFavor += 10 * (random + 1)
            showName()
            val bonusText = "${getName()}${mEnding[random]}, \u6B22\u6109+${10 * (random + 1)}"
            mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(bonusText)
            setLevelSpinner()
        }
    }

    fun convertTalk(input:String):String{
        return  input.replace("QIE", "\u81E3\u59BE")
                .replace("CHEN", "\u81E3")
                .replace("HUANG", "\u7687\u4E0A")
                .replace("BIXIA", "\u965B\u4E0B")
    }

    fun showName(){
        mName.text = CultivationHelper.showing("${FeiLevel[mPerson.feiziLevel]}${mPerson.name}(${mPerson.feiziFavor})")
    }

    fun getName():String{
        return CultivationHelper.showing("${FeiLevel[mPerson.feiziLevel]}${mPerson.fullName}")
    }

    fun initLevelSpinner(){
        mLevelList.addAll(FeiLevel.filterIndexed { index, _ -> FeiziStep[index] <= mPerson.feiziFavor  }
                .map { CultivationHelper.showing(it) })
        val adapter = ArrayAdapter<String>(context!!,
                android.R.layout.simple_spinner_item, mLevelList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = adapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.selectedItem.toString()
                mPerson.feiziLevel = FeiLevel.map { CultivationHelper.showing(it)  }.indexOf(selected)
                showName()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        mSpinner.setSelection(mLevelList.indexOf(CultivationHelper.showing(FeiLevel[mPerson.feiziLevel])))
    }

    fun setLevelSpinner(){
        mLevelList.clear()
        mLevelList.addAll(FeiLevel.filterIndexed { index, _ -> FeiziStep[index] <= mPerson.feiziFavor  }
                .map { CultivationHelper.showing(it) })
        (mSpinner.adapter as BaseAdapter).notifyDataSetChanged()
        mSpinner.setSelection(mLevelList.indexOf(CultivationHelper.showing(FeiLevel[mPerson.feiziLevel])))
    }


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
        showName()
        initLevelSpinner()
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