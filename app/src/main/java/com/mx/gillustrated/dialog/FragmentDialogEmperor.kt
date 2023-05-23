package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.EmperorData.FeiLevel
import com.mx.gillustrated.component.EmperorData.FeiziStep
import com.mx.gillustrated.databinding.FragmentDialogEmperorBinding
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.io.File
import java.util.*
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.P)
class FragmentDialogEmperor constructor(private val mId:String)  : DialogFragment() {

    companion object{
        fun newInstance(id:String): FragmentDialogEmperor {
            return FragmentDialogEmperor(id)
        }
    }

    private var _binding: FragmentDialogEmperorBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        _binding = FragmentDialogEmperorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListener(){

        binding.btnClose.setOnClickListener {
            this.dismiss()
        }
        binding.btnReward.setOnClickListener {
            mPerson.feiziFavor += 1000
            showName()
            makeContent("\u8C22\u4E3B\u516C.")
            setLevelSpinner()
        }
        binding.btnPunish.setOnClickListener {
            mPerson.feiziFavor = max(0, mPerson.feiziFavor - 1000)
            makeContent("\u59BE\u77E5\u9519.")
            showName()
            setLevelSpinner()
        }
        binding.btnBye.setOnClickListener {
            makeContent(mEnd.shuffled()[0])
            binding.tvContent.postDelayed({
                this.dismiss()
            }, 1000)
        }
        binding.btnMl.setOnClickListener {
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
    }

    fun init(){
        val context = activity as CultivationActivity
        mPerson = context.getPersonData(mId)!!
        showName()
        initLevelSpinner()
        makeContent(mStart.shuffled()[0])
        try {
            val imageDir = requireActivity().getExternalFilesDir(
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + NameUtil.Gender.Female) ?: return
            var file = File(imageDir.path, "${mPerson.profile}.png")
            if (!file.exists()) {
                file = File(imageDir.path, "${mPerson.profile}.jpg")
            }
            if (!file.exists()) {
                file = File(imageDir.path, "1002.jpg")
            }
            if (file.exists()) {
                val bmp = ImageDecoder.createSource(requireContext().contentResolver, Uri.fromFile(file))
                binding.ivProfile.setImageBitmap(ImageDecoder.decodeBitmap(bmp))
            } else
                binding.ivProfile.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun makeContent(text:String){
        if(binding.tvContent.text.toString() == "")
            binding.tvContent.text = CultivationHelper.showing(text)
        else
            binding.tvContent.text = binding.tvContent.text.toString() +  "\n" + CultivationHelper.showing(text)
        binding.svContent.post {
            binding.svContent.smoothScrollTo(0, 1000)
        }

    }

    fun showName(){
        binding.tvName.text = CultivationHelper.showing("${FeiLevel[mPerson.feiziLevel]}·${mPerson.name}(${mPerson.feiziFavor})")
    }

    private fun getNameSimple():String{
        return CultivationHelper.showing("${mPerson.lastName}${FeiLevel[mPerson.feiziLevel]}")
    }

    private fun initLevelSpinner(){
        mLevelList.addAll(FeiLevel.filterIndexed { index, _ -> FeiziStep[index] <= mPerson.feiziFavor  }
                .map { CultivationHelper.showing(it) })
        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, mLevelList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLevel.adapter = adapter
        binding.spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.selectedItem.toString()
                mPerson.feiziLevel = FeiLevel.map { CultivationHelper.showing(it)  }.indexOf(selected)
                showName()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.spinnerLevel.setSelection(mLevelList.indexOf(CultivationHelper.showing(FeiLevel[mPerson.feiziLevel])))
    }

    private fun setLevelSpinner(){
        mLevelList.clear()
        mLevelList.addAll(FeiLevel.filterIndexed { index, _ -> FeiziStep[index] <= mPerson.feiziFavor  }
                .map { CultivationHelper.showing(it) })
        (binding.spinnerLevel.adapter as BaseAdapter).notifyDataSetChanged()
        binding.spinnerLevel.setSelection(mLevelList.indexOf(CultivationHelper.showing(FeiLevel[mPerson.feiziLevel])))
    }
}