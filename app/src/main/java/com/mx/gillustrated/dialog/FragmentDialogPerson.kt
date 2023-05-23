package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.PersonPagerAdapter
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.EmperorData
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.databinding.FragmentDialogPersionBinding
import com.mx.gillustrated.fragment.*
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Label
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.TianFu
import java.io.File
import java.lang.ref.WeakReference
import androidx.appcompat.content.res.AppCompatResources
import kotlin.math.min


@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("SetTextI18n")
class FragmentDialogPerson : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogPerson {
            return FragmentDialogPerson()
        }
        class TimeHandler constructor(val context: FragmentDialogPerson): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogPerson> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }

    private var _binding: FragmentDialogPersionBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mPerson:Person
    lateinit var mContext:CultivationActivity
    private val mFragments:MutableList<Fragment> = mutableListOf()

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var showSS:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogPersionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initListener()
    }

    private fun initListener(){

        binding.btnClose.setOnClickListener {
            onCloseHandler()
        }
        binding.tvLingGen.setOnClickListener {
            val prop = mPerson.extraProperty.joinToString()
            Toast.makeText(mContext, prop, Toast.LENGTH_SHORT).show()
        }
        binding.tvPartner.setOnClickListener {
            val partner = mContext.getOnlinePersonDetail(mPerson.partner)
            if(partner != null){
                openPersonDetail(partner.id)
                onCloseHandler()
            }else{
                Toast.makeText(mContext, mPerson.partnerName ?: "", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvParentDad.setOnClickListener {
            val person = mContext.getOnlinePersonDetail(mPerson.parent?.first)
            if(person != null){
                openPersonDetail(person.id)
                onCloseHandler()
            }else{
                Toast.makeText(mContext, mPerson.parentName?.first ?: "", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvParentMum.setOnClickListener {
            val person = mContext.getOnlinePersonDetail(mPerson.parent?.second)
            if(person != null){
                openPersonDetail(person.id)
                onCloseHandler()
            }else{
                Toast.makeText(mContext, mPerson.parentName?.second ?: "", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvWinner.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogRank.newInstance(6, mPerson.id)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_rank_info")
        }
        binding.ivProfile.setOnClickListener {
            if (mPerson.profile == 0)
                return@setOnClickListener
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogImage.newInstance(mPerson.profile.toString(), mPerson.gender)
            newFragment.isCancelable = true
            newFragment.show(ft, "dialog_image")
        }
        binding.schFav.setOnCheckedChangeListener { _, isChecked ->
            mPerson.isFav = isChecked
        }
    }
//   // RelativeLayout 需要
//    override fun onResume() {
//        super.onResume()
//        if(dialog?.window != null){
//            val params = dialog?.window?.attributes!!
//            params.width = WindowManager.LayoutParams.MATCH_PARENT
//            params.height = WindowManager.LayoutParams.MATCH_PARENT
//            dialog?.window?.attributes = params
//        }
//    }

    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    fun init(){
        mContext = activity as CultivationActivity
        val id = requireArguments().getString("id", "")
        val person = mContext.getPersonData(id)
        if(person == null){
            onCloseHandler()
            return
        }
        val skin = CultivationHelper.getSkinObject(person.skin)
        if (skin != null){
            if (!skin.animated){
                binding.llParentMeasure.background = ColorDrawable(Color.TRANSPARENT)
                dialog?.window?.setBackgroundDrawableResource(CultivationHelper.getResouresId(resources, skin.resource))
            }else{
                val animate = AppCompatResources.getDrawable(requireContext(),CultivationHelper.getResouresId(resources, skin.resource)) as AnimationDrawable
                //mContext.getDrawable(CultivationHelper.getResouresId(resources, skin.resource)) as AnimationDrawable
                dialog?.window?.setBackgroundDrawable(animate)
                binding.llParentMeasure.background = ColorDrawable(Color.TRANSPARENT)
                animate.start()
            }
        }
        mPerson = person
        setViewPager()
        setTianfu()
        setProfile()
        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread{
            while (true){
                Thread.sleep(2000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }.start()
    }

    private val innerCallback = object:IViewpageCallback{
        override fun update(type: Int, params:String) {
            when (type){
                1 -> {
                    setTianfu()
                    updateView()
                }
                2 -> setProfile()
                3 -> updateView()
                4 -> {
                    showSS = params == "Y"
                    setProfile()
                }
            }
        }

    }

    private fun setViewPager(){
        val bundle = Bundle()
        bundle.putString("id", mPerson.id)
        val equip = FragmentEquipment()
        equip.arguments = bundle
        val his = FragmentPersonEvent(innerCallback)
        his.arguments = bundle
        val info = FragmentPersonInfo(innerCallback)
        info.arguments = bundle
        val teji = FragmentTeJi()
        teji.arguments = bundle
        val follower = FragmentFollower()
        follower.arguments = bundle

        mFragments.clear()
        mFragments.add(equip)
        mFragments.add(info)
        mFragments.add(teji)
        mFragments.add(follower)
        mFragments.add(his)
        val title = mutableListOf("Eq.", "In.", "Tj.", "Fw.", "Ev.")
        binding.vpPerson.adapter = PersonPagerAdapter(childFragmentManager, mFragments, title)
        binding.vpPerson.currentItem = 0
        binding.tabLayout.setupWithViewPager(binding.vpPerson)
    }

    private fun updateViewPager(){
        if(binding.vpPerson.currentItem == 4){
            val fragment:FragmentPersonEvent = mFragments[4] as FragmentPersonEvent
            fragment.updateEvent()
        }
    }

    fun setProfile(){
        val profileFrame = CultivationHelper.getProfileFrame(mPerson, mContext.mClans)
        if(profileFrame.first != -1){
            binding.llProfile.background = AppCompatResources.getDrawable(requireContext(), profileFrame.first)
            binding.llProfile.backgroundTintList = if(profileFrame.second != -1) ColorStateList.valueOf(profileFrame.second) else null
        }else{
            binding.llProfile.background = null
            binding.llProfile.backgroundTintList = null
        }

        var profile = mPerson.profile
        if(!showSS && mPerson.gender == NameUtil.Gender.Female && mPerson.profile in 1701..1799){
            profile = 0
        }
        try {
            val imageDir = requireActivity().getExternalFilesDir(
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + mPerson.gender) ?: return
            var file = File(imageDir.path, "$profile.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$profile.jpg")
            }
            if (file.exists()) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, Uri.fromFile(file))
                if(profileFrame.third != -1){
                    val decoder = ImageDecoder.decodeDrawable(source) { decoder, _, _ ->
                        val path = Path().apply {
                            fillType = Path.FillType.INVERSE_EVEN_ODD
                        }
                        val paint = Paint().apply {
                            isAntiAlias = true
                            color = Color.TRANSPARENT
                            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                        }
                        decoder.setPostProcessor { canvas ->
                            val length = min(canvas.width.toFloat(),canvas.height.toFloat())
                            val direction = Path.Direction.CW
                            path.addRoundRect(0f, 0f, length, length, length / 2, length / 2, direction)
                            canvas.drawPath(path, paint)
                            PixelFormat.TRANSLUCENT
                        }
                    }
                    binding.ivProfile.setImageDrawable(decoder)
                    binding.ivProfile.setPadding(profileFrame.third,profileFrame.third,profileFrame.third,profileFrame.third)
                }
                else
                    binding.ivProfile.setImageBitmap(ImageDecoder.decodeBitmap(source))
            } else
                binding.ivProfile.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTianfu(){
        //mDialogView.tianfu.removeAllViews()
        val tianFus = mPerson.tianfuList
        if(tianFus.isNotEmpty()){

            binding.llParentMeasure.measure(0,0)
            binding.llTianfu.setConfig(TextViewBox.TextViewBoxConfig(binding.llParentMeasure.measuredWidth - 100))
            binding.llTianfu.setCallback(object : TextViewBox.Callback {
                override fun onClick(index: Int) {
                    val data = tianFus[index]
                    val text = when (data.type) {
                        1 -> "${data.name} \u57fa\u7840\u4fee\u4e3a"
                        2 -> "${data.name} \u4fee\u4e3a\u52a0\u901f"
                        3 -> "${data.name} Life"
                        4 -> "${data.name} \u7a81\u7834"
                        5 -> "${data.name} \u798f\u6e90"
                        else -> ""
                    }
                    Toast.makeText(context, "${text}增加${data.bonus}", Toast.LENGTH_SHORT).show()
                }
            })
            binding.llTianfu.setDataProvider(
                    tianFus.map { getTianFuName(it) },
                    tianFus.map { CommonColors[it.rarity] })

        }

        val labels = mPerson.label
        if(labels.isNotEmpty()){
            binding.llParentMeasure.measure(0,0)
            binding.llLabel.setConfig(TextViewBox.TextViewBoxConfig(binding.llParentMeasure.measuredWidth - 100))
            binding.llLabel.setDataProvider(
                    labels.map { getLabelName(CultivationHelper.mConfig.label.find { l -> l.id == it }!!.copy())},
                    labels.map { CommonColors[CultivationHelper.mConfig.label.find { l -> l.id == it }!!.copy().rarity] })
            binding.llLabel.setCallback(object : TextViewBox.Callback {
                override fun onClick(index: Int) {
                    val data = CultivationHelper.mConfig.label.find { it.id == labels[index] }!!
                    Toast.makeText(context, data.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun getTianFuName(tianFu: TianFu):String{
        if (CultivationHelper.pinyinMode)
            return tianFu.name.replace(Regex("\\d"), "")
        when {
            tianFu.name.contains("Power") -> return "\u6839\u9AA8"
            tianFu.name.contains("Clever") -> return "\u5929\u8D44"
            tianFu.name.contains("Life") -> return "\u547D\u6570"
            tianFu.name.contains("Top") -> return "\u609F\u6027"
            tianFu.name.contains("Fu") -> return "\u798F\u6E90"
        }
        return ""
    }

    private fun getLabelName(label:Label):String{
        return CultivationHelper.showing(label.name)
    }

    private fun getName():String{
        return if (mPerson.feiziFavor > 0){
            "${EmperorData.FeiLevel[mPerson.feiziLevel]}·${mPerson.name}"
        }else{
            mPerson.name
        }
    }

    fun updateView(){
        if(mContext.getOnlinePersonDetail(mPerson.id) == null){
            mThreadRunnable = false
        }
        binding.schFav.isChecked = mPerson.isFav
        binding.tvName.text ="${CultivationHelper.showing(getName())}${CultivationHelper.showLifeTurn(mPerson)}${CultivationHelper.showAncestorLevel(mPerson)}"
        setFamily()
        binding.tvAlliance.text = CultivationHelper.showing(mPerson.allianceName)
        binding.tvAge.text = "${getGender()}${CultivationHelper.talentValue(mPerson)}${if(CultivationHelper.isTalent(mPerson)) "⭐" else ""}/${CultivationHelper.showAgeRemained(mPerson)}"
        binding.tvCareer.text = mPerson.careerList.joinToString()
        binding.tvProps.text = getProperty()
        binding.tvWinner.text = "${mPerson.battleWinner}-${mPerson.battlexiuwei}↑"
        binding.tvClan.text = CultivationHelper.showing(mContext.mClans[mPerson.ancestorId]?.nickName ?: "")
        binding.tvJingjie.text = CultivationHelper.showing(mPerson.jinJieName)
        binding.tvJingjie.setTextColor(Color.parseColor(CommonColors[mPerson.jinJieColor]))
        binding.tvXiuweiAdd.text =  "${CultivationHelper.getXiuweiGrow(mPerson)}"
        binding.tvSuccess.text = "↑${CultivationHelper.getTotalSuccess(mPerson)}"
        binding.tvXiuweiAdd.setTextColor(Color.parseColor(CommonColors[1]))
        binding.tvSuccess.setTextColor(Color.parseColor(CommonColors[1]))
        binding.tvLingGen.text = CultivationHelper.showing(mPerson.lingGenName)
        binding.tvLingGen.setTextColor(Color.parseColor(CommonColors[mPerson.lingGenDetail.color]))

        updateViewPager()
    }

    private fun getGender():String{
        return mPerson.gender.props
    }

    private fun getProperty():String{
        val result = CultivationHelper.getProperty(mPerson)
        return "${result[0]}/${result[1]} ${result[2]}-${result[3]}-${result[4]}"
    }

    private fun setFamily(){
        setRelationName(binding.tvPartner, mPerson.partnerName, mContext.getOnlinePersonDetail(mPerson.partner), binding.tvSymbol3, binding.tvSymbol4)
        setRelationName(binding.tvParentDad, mPerson.parentName?.first, mContext.getOnlinePersonDetail(mPerson.parent?.first),  binding.tvSymbol1, null)
        setRelationName(binding.tvParentMum, mPerson.parentName?.second, mContext.getOnlinePersonDetail(mPerson.parent?.second), binding.tvSymbol2, null)

        val children = mPerson.children.mapNotNull { mContext.getOnlinePersonDetail(it) }
        binding.llChildren.removeAllViews()
        if(children.isNotEmpty()){
            children.forEach {
                val person = it
                val textView = TextView(this.context)
                textView.text =  CultivationHelper.showing(person.name)
                textView.setTextColor(Color.parseColor(CommonColors[person.lingGenDetail.color]))
                textView.setOnClickListener { _->
                    openPersonDetail(it.id)
                    onCloseHandler()
                }
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginEnd = 5
                textView.layoutParams = layoutParams
                binding.llChildren.addView(textView)
            }
        }
        binding.llChildren.visibility = if(children.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setRelationName(text:TextView, name:String?, person: Person?, symbol1:TextView?, symbol2:TextView?){
        if(name != null){
            text.visibility = View.VISIBLE
            symbol1?.visibility = View.VISIBLE
            symbol2?.visibility = View.VISIBLE
            if(person == null){
                text.text = CultivationSetting.TEMP_DEAD_SYMBOL
            }else{
                text.text = getContent(name)
                text.setTextColor(Color.parseColor(CommonColors[person.lingGenDetail.color]))
            }
        }else{
            text.visibility = View.GONE
            symbol1?.visibility = View.GONE
            symbol2?.visibility = View.GONE
            text.text = ""
        }
    }


    private fun getContent(name:String?):String{
        if(name == null)
            return ""
        return CultivationHelper.showing(name)
    }

    private fun openPersonDetail(id:String){
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = newInstance()
        newFragment.isCancelable = false
        val bundle = Bundle()
        bundle.putString("id", id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }



    interface IViewpageCallback{
        fun update(type:Int, params:String = "")
    }
}