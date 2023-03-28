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

    @BindView(R.id.sv_content)
    lateinit var mScroll: ScrollView


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        this.dismiss()
    }

    var count = 0
    private var mLevelList = mutableListOf<String>()

    private val mStart =  mutableListOf("\u89C1\u8FC7\u4E3B\u516C.")
    private val mEnd =  mutableListOf("\u4E3B\u516C\u6162\u8D70.")
    private val mDetail =  mutableListOf("\u82B1\u524D\u6708\u4E0B\u5F71\u6210\u53CC", "\u60C5\u6EE1\u4E09\u6C5F\u610F\u76CE\u7136",
            "\u6708\u6EE1\u897F\u697C\u68A6\u6B63\u957F", "\u82B1\u5F71\u5A46\u5A11\u4EBA\u6B32\u9189",
            "\u69B4\u82B1\u5982\u706B\u6620\u7A97\u53F0", "\u79CB\u98CE\u4E0D\u89E3\u79BB\u4EBA\u8272",
            "\u5C71\u8272\u6E56\u5149\u5206\u5916\u5A07", "\u7EFF\u53F6\u7EA2\u82B1\u76F8\u6620\u886C",
            "\u7EA2\u85D5\u9999\u6B8B\u7389\u7C1F\u79CB")

    private lateinit var mPerson: Person

    @OnClick(R.id.btn_reward)
    fun onRewardHandler(){
        mPerson.feiziFavor += 1000
        showName()
        makeContent("\u8C22\u4E3B\u516C.")
        setLevelSpinner()
    }

    @OnClick(R.id.btn_punish)
    fun onPunishHandler(){
        mPerson.feiziFavor = Math.max(0, mPerson.feiziFavor - 1000)
        makeContent("\u59BE\u77E5\u9519.")
        showName()
        setLevelSpinner()
    }

    @OnClick(R.id.btn_bye)
    fun onByeHandler(){
        makeContent(mEnd.shuffled()[0])
        mContent.postDelayed({
            this.dismiss()
        }, 1000)
    }


    @OnClick(R.id.btn_ml)
    fun onMLHandler(){
        if (count < 5){
            val random = Random().nextInt(5)
            mPerson.feiziFavor += 10 * (random + 1)
            val showing = mDetail.shuffled()[0]
            makeContent("$showing, ${getNameSimple()}\u5BA0\u7231+${10 * (random + 1)}")
            mDetail.removeIf { it == showing }
            showName()
            setLevelSpinner()
            count++
        }else{
            makeContent("\u4E3B\u516C\u8FD8\u6709\u4E8B\u5417?")
        }
    }

    private fun makeContent(text:String){
        if(mContent.text.toString() == "")
            mContent.text = CultivationHelper.showing(text)
        else
            mContent.text = mContent.text.toString() +  "\n" + CultivationHelper.showing(text)
        mScroll.post {
            mScroll.smoothScrollTo(0, 1000)
        }

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
        makeContent(mStart.shuffled()[0])
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