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
    private var mLevelList = mutableListOf<String>()
    private val mStart = Talk.filterIndexed { index, _ -> listOf(3,4,6,9,10,16,17,18,28,29,56,57,60,62,63.150,152,153).contains(index) || index in 30..36
            || index in 42..50 || index in 85..90 || index in 98..107 || index in 113..119 || index in 130..147 }
    private val mData = mutableListOf(
            mutableListOf(),
            Talk.filterIndexed { index, _ -> listOf(58,59,64,65,91,92,120,121,148,149,151,154,155).contains(index) || index in 21..27  || index in 72..80 ||
                    index in 82..84 },
            Talk.filterIndexed { index, _ -> listOf(5,7,8).contains(index) ||  index in 37..41 || index in 51..55 || index in 66..70
                    ||  index in 93..97 || index in 108..112 || index in 122..127  }
    )
    private val mEnding1 = mutableListOf("\u7F9E......", "\u4F9D\u504E......", "\u7687\u4E0A~",
            "\u7687\u4E0A\u8BA8\u538C~",
            "\u965B\u4E0B\u597D\u574F~\u7F9E\u7F9E\u7B54\u7B54......")


    private val mEnding2 = (0..9).mapIndexed { index, _ ->
        when(index){
            in 0..1 -> "……………… \u4E00\u70B7\u9999\u8FC7\u53BB\u4E86"
            in 2..3 -> "……………… \u4E00\u4E2A\u65F6\u8FB0\u8FC7\u53BB\u4E86"
            in 4..5 -> "\u7687\u4E0A\u8EAB\u4F53\u771F\u597D ……(\u8138\u7EA2)"
            else -> ""
        }
    }
    private lateinit var mPerson: Person

    @OnClick(R.id.btn_reward)
    fun onRewardHandler(){
        mPerson.feiziFavor += 1000
        showName()
        makeContent(convertTalk(Talk[2]))
        setLevelSpinner()
    }

    @OnClick(R.id.btn_punish)
    fun onPunishHandler(){
        mPerson.feiziFavor = Math.max(0, mPerson.feiziFavor - 1000)
        makeContent(convertTalk(Talk.filterIndexed { index, _ -> index in 12..14 }.shuffled()[0]))
        showName()
        setLevelSpinner()
    }

    @OnClick(R.id.btn_bye)
    fun onByeHandler(){
        makeContent(convertTalk(mData[2].shuffled()[0]))
        mContent.postDelayed({
            this.dismiss()
        }, 1000)
    }


    @OnClick(R.id.btn_ml)
    fun onMLHandler(){
        if (count < 2){
            makeContent(convertTalk(mData[1].shuffled()[0]))
            val random = Random().nextInt(5)
            mPerson.feiziFavor += 10 * (random + 1)
            val ending = if (count == 0) mEnding1[random] else mEnding2[random]
            makeContent("\u4E0E${getName()}\u82B1\u524D\u6708\u4E0B, $ending \u5BA0\u7231+${10 * (random + 1)}")
            showName()
            setLevelSpinner()
            count++
        }else{
            makeContent("${getNameSimple()}\u762B\u8F6F\u5728\u4E86\u5730\u4E0A...")
        }
    }

    fun makeContent(text:String){
        mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(text)
    }

    fun convertTalk(input:String):String{
        return  input.replace("QIE", "\u81E3\u59BE")
                .replace("CHEN", "\u81E3")
                .replace("HUANG", "\u7687\u4E0A")
                .replace("BIXIA", "\u965B\u4E0B")
    }

    fun showName(){
        mName.text = CultivationHelper.showing("${FeiLevel[mPerson.feiziLevel]}·${mPerson.name}(${mPerson.feiziFavor})")
    }

    fun getName():String{
        return CultivationHelper.showing("${FeiLevel[mPerson.feiziLevel]}·${mPerson.fullName}")
    }

    fun getNameSimple():String{
        return CultivationHelper.showing("${mPerson.lastName}${FeiLevel[mPerson.feiziLevel]}")
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
        makeContent(convertTalk(mStart.shuffled()[0]))
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + NameUtil.Gender.Female)
            var file = File(imageDir.path, "${mPerson.profile}.png")
            if (!file.exists()) {
                file = File(imageDir.path, "${mPerson.profile}.jpg")
            }
            if (!file.exists()) {
                file = File(imageDir.path, "1002.jpg")
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