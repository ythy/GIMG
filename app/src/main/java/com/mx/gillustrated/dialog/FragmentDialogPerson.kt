package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.ContentInfoCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import butterknife.*
import com.google.android.material.tabs.TabLayout
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.PersonPagerAdapter
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.fragment.*
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Label
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.TianFu
import java.io.File
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("SetTextI18n")
class FragmentDialogPerson : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogPerson {
            return FragmentDialogPerson()
        }
        class TimeHandler constructor(val context: FragmentDialogPerson): Handler(){

            private val reference: WeakReference<FragmentDialogPerson> = WeakReference(context)

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg?.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }


    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
    }

    @OnClick(R.id.tv_lingGen)
    fun onLingGenClickHandler(){
        val prop = mPerson.extraProperty.joinToString()
        Toast.makeText(mContext, prop, Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.tv_partner)
    fun onPartnerClickHandler(){
        val partner = mContext.getOnlinePersonDetail(mPerson.partner) ?: return
        openPersonDetail(partner.id)
        onCloseHandler()
    }

    @OnClick(R.id.tv_parent_dad)
    fun onDadClickHandler(){
        val person = mContext.getOnlinePersonDetail(mPerson.parent?.first) ?: return
        openPersonDetail(person.id)
        onCloseHandler()
    }

    @OnClick(R.id.tv_parent_mum)
    fun onMumClickHandler(){
        val person = mContext.getOnlinePersonDetail(mPerson.parent?.second) ?: return
        openPersonDetail(person.id)
        onCloseHandler()
    }

    @OnClick(R.id.tv_winner)
    fun onWinnerClickHandler(){
        val ft = mContext.supportFragmentManager.beginTransaction()
        val newFragment = FragmentDialogRank.newInstance(6, mPerson.id)
        newFragment.isCancelable = false
        newFragment.show(ft, "dialog_rank_info")
    }

    @BindView(R.id.sch_fav)
    lateinit var mSwitchFav:Switch

    @BindView(R.id.vp_person)
    lateinit var mViewPager: ViewPager

    @BindView(R.id.tabLayout)
    lateinit var mTabLayout: TabLayout

    @OnCheckedChanged(R.id.sch_fav)
    fun onFavSwitch(checked:Boolean){
        mPerson.isFav = checked
    }

    lateinit var mPerson:Person
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView
    private val mFragments:MutableList<Fragment> = mutableListOf()

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var showSS:Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_persion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mDialogView = DialogView(view)
        init()
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

    fun init(){
        mContext = activity as CultivationActivity
        val id = this.arguments!!.getString("id", "")
        val person = mContext.getPersonData(id)
        if(person == null){
            onCloseHandler()
            return
        }
        mPerson = person
        setViewPager()
        setTianfu()
        setProfile()
        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread(Runnable {
            while (true){
                Thread.sleep(2000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }).start()
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
        mViewPager.adapter = PersonPagerAdapter(childFragmentManager, mFragments, title)
        mViewPager.currentItem = 0

        mTabLayout.setupWithViewPager(mViewPager)

    }

    private fun updateViewPager(){
        if(mViewPager.currentItem == 4){
            val fragment:FragmentPersonEvent = mFragments[4] as FragmentPersonEvent
            fragment.updateEvent()
        }
    }

    fun setProfile(){
        val person = mPerson
        if(person.nationPost > 0){
            mDialogView.profileBorder.background = mContext.getDrawable(R.drawable.profile_frame1)
            mDialogView.profileBorder.backgroundTintList = ColorStateList.valueOf(Color.parseColor(CultivationSetting.PostColors[person.nationPost - 1]))
            mDialogView.profileBorder.setPadding(8,8,8,8)
        }else{
            mDialogView.profileBorder.background = null
            mDialogView.profileBorder.backgroundTintList = null
            mDialogView.profileBorder.setPadding(0,0,0,0)
        }
        var profile = mPerson.profile
        if(!showSS && mPerson.gender == NameUtil.Gender.Female && mPerson.profile in 1701..1799){
            profile = 0
        }
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + person.gender)
            var file = File(imageDir.path, "$profile.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$profile.jpg")
            }
            if (file.exists()) {
                val bmp = MediaStore.Images.Media.getBitmap(mContext.contentResolver, Uri.fromFile(file))
                mDialogView.profile.setImageBitmap(bmp)
            } else
                mDialogView.profile.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTianfu(){
        //mDialogView.tianfu.removeAllViews()
        val tianFus = mPerson.tianfus
        if(tianFus.isNotEmpty()){

            mDialogView.measures.measure(0,0)
            mDialogView.tianfu.setConfig(TextViewBox.TextViewBoxConfig(mDialogView.measures.measuredWidth - 100))
            mDialogView.tianfu.setCallback(object : TextViewBox.Callback {
                override fun onClick(index: Int) {
                    val data = tianFus[index]
                    val text = when (data.type) {
                        1 -> "\u57fa\u7840\u4fee\u4e3a"
                        2 -> "\u4fee\u4e3a\u52a0\u901f"
                        3 -> "Life"
                        4 -> "\u7a81\u7834"
                        5 -> "\u798f\u6e90"
                        else -> ""
                    }
                    Toast.makeText(context, "${text}增加${data.bonus}", Toast.LENGTH_SHORT).show()
                }
            })
            mDialogView.tianfu.setDataProvider(
                    tianFus.map { getTianFuName(it) },
                    tianFus.map { CommonColors[it.rarity] })

        }

        val labels = mPerson.label
        if(labels.isNotEmpty()){
            mDialogView.measures.measure(0,0)
            mDialogView.label.setConfig(TextViewBox.TextViewBoxConfig(mDialogView.measures.measuredWidth - 100))
            mDialogView.label.setDataProvider(
                    labels.map { getLabelName(CultivationHelper.mConfig.label.find { l -> l.id == it }!!.copy())},
                    labels.map { CommonColors[CultivationHelper.mConfig.label.find { l -> l.id == it }!!.copy().rarity] })
            mDialogView.label.setCallback(object : TextViewBox.Callback {
                override fun onClick(index: Int) {
                    val data = CultivationHelper.mConfig.label.find { it.id == labels[index] }!!
                    Toast.makeText(context, data.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun getTianFuName(tianFu: TianFu):String{
        if (CultivationHelper.pinyinMode)
            return tianFu.name
        when {
            tianFu.name.contains("Power") -> return tianFu.name.replace("Power", "\u6839\u9AA8")
            tianFu.name.contains("Clever") -> return tianFu.name.replace("Clever", "\u5929\u8D44")
            tianFu.name.contains("Life") -> return tianFu.name.replace("Life", "\u547D\u6570")
            tianFu.name.contains("Top") -> return tianFu.name.replace("Top", "\u609F\u6027")
            tianFu.name.contains("Fu") -> return tianFu.name.replace("Fu", "\u798F\u6E90")
        }
        return ""
    }

    fun getLabelName(label:Label):String{
        val tejiString = if (label.teji.size > 0) "+" else  ""
        val followerString = if (label.follower.size > 0) "#" else  ""
        return CultivationHelper.showing(label.name + tejiString + followerString)
    }

    fun updateView(){
        if(mContext.getOnlinePersonDetail(mPerson.id) == null){
            mThreadRunnable = false
        }
        mSwitchFav.isChecked = mPerson.isFav
        mDialogView.name.text ="${CultivationHelper.showing(mPerson.name)}${CultivationHelper.showLifeTurn(mPerson)}${CultivationHelper.showAncestorLevel(mPerson)}"
        setFamily()
        mDialogView.alliance.text = CultivationHelper.showing(mPerson.allianceName)
        mDialogView.age.text = "${CultivationHelper.showing(mPerson.gender.props)}${CultivationHelper.talentValue(mPerson)}${if(CultivationHelper.isTalent(mPerson)) "⭐" else ""}/${CultivationHelper.showAgeRemained(mPerson)}"
        mDialogView.career.text = mPerson.careerDetailList.joinToString()
        mDialogView.props.text = getProperty()
        mDialogView.winner.text = "${mPerson.battleWinner}-${mPerson.battlexiuwei}↑"
        mDialogView.clan.text = CultivationHelper.showing(mContext.mClans[mPerson.ancestorId]?.nickName ?: "")
        mDialogView.jingjie.text = CultivationHelper.showing(mPerson.jinJieName)
        mDialogView.jingjie.setTextColor(Color.parseColor(CommonColors[mPerson.jinJieColor]))
        mDialogView.xiuweiAdd.text =  "${CultivationHelper.getXiuweiGrow(mPerson, mContext.mAlliance)}"
        mDialogView.success.text = "↑${CultivationHelper.getTotalSuccess(mPerson)}"
        mDialogView.lingGen.text = CultivationHelper.showing(mPerson.lingGenName)
        mDialogView.lingGen.setTextColor(Color.parseColor(CommonColors[mPerson.lingGenType.color]))

        updateViewPager()
    }

    private fun getProperty():String{
        val result = CultivationHelper.getProperty(mPerson)
        return "${result[0]}/${result[1]} ${result[2]}-${result[3]}-${result[4]}"
    }

    private fun setFamily(){
        setRelationName(mDialogView.partner, mPerson.partnerName, mContext.getOnlinePersonDetail(mPerson.partner), "<", ">")
        setRelationName(mDialogView.parentDad, mPerson.parentName?.first, mContext.getOnlinePersonDetail(mPerson.parent?.first), "[", "")
        setRelationName(mDialogView.parentMum, mPerson.parentName?.second, mContext.getOnlinePersonDetail(mPerson.parent?.second), "", "]")

        val children = mPerson.children.mapNotNull { mContext.getOnlinePersonDetail(it) }
        mDialogView.children.removeAllViews()
        if(children.isNotEmpty()){
            children.forEach {
                val person = it
                val textView = TextView(this.context)
                textView.text =  CultivationHelper.showing(person.name)
                textView.setOnClickListener { _->
                    openPersonDetail(it.id)
                    onCloseHandler()
                }
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginEnd = 5
                textView.layoutParams = layoutParams
                mDialogView.children.addView(textView)
            }
        }
        mDialogView.children.visibility = if(children.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setRelationName(text:TextView, name:String?, person: Person?, prefix:String = "", suffix:String = ""){
        if(name != null){
            text.visibility = View.VISIBLE
            text.text = "$prefix${getContent(name)}$suffix"
            if(person == null){
                text.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
                text.paint.isAntiAlias = true
            }
        }else{
            text.visibility = View.GONE
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
        // Create and show the dialog.
        val newFragment = newInstance()
        newFragment.isCancelable = false
        val bundle = Bundle()
        bundle.putString("id", id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

    class DialogView constructor(view: View){

        @BindView(R.id.tv_name)
        lateinit var name:TextView

        @BindView(R.id.iv_profile)
        lateinit var profile:ImageView

        @BindView(R.id.tv_partner)
        lateinit var partner:TextView

        @BindView(R.id.tv_parent_dad)
        lateinit var parentDad:TextView

        @BindView(R.id.tv_parent_mum)
        lateinit var parentMum:TextView

        @BindView(R.id.ll_children)
        lateinit var children: LinearLayout

        @BindView(R.id.tv_alliance)
        lateinit var alliance:TextView

        @BindView(R.id.tv_age)
        lateinit var age:TextView

        @BindView(R.id.tv_jingjie)
        lateinit var jingjie:TextView

        @BindView(R.id.tv_success)
        lateinit var success:TextView

        @BindView(R.id.tv_lingGen)
        lateinit var lingGen:TextView

        @BindView(R.id.ll_tianfu)
        lateinit var tianfu:TextViewBox

        @BindView(R.id.ll_label)
        lateinit var label:TextViewBox

        @BindView(R.id.tv_xiuwei_add)
        lateinit var xiuweiAdd:TextView

        @BindView(R.id.tv_career)
        lateinit var career:TextView

        @BindView(R.id.tv_clan)
        lateinit var clan:TextView

        @BindView(R.id.tv_winner)
        lateinit var winner:TextView

        @BindView(R.id.tv_props)
        lateinit var props:TextView

        @BindView(R.id.ll_parent_measure)
        lateinit var measures:LinearLayout

        @BindView(R.id.ll_profile)
        lateinit var profileBorder:LinearLayout


        init {
            ButterKnife.bind(this, view)
        }
    }

    interface IViewpageCallback{
        fun update(type:Int, params:String = "")
    }
}