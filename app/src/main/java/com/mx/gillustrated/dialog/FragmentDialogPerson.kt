package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.PersonPagerAdapter
import com.mx.gillustrated.component.CultivationSetting.CommonColors
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.component.TextViewBox
import com.mx.gillustrated.fragment.*
import com.mx.gillustrated.vo.cultivation.Person
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

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

    @OnClick(R.id.btn_be)
    fun onBeClickHandler(){
        val partner = mContext.getOnlinePersonDetail(mPerson.partner)
        if(partner == null){
            val tag = mDialogView.be.tag
            val calendar = Calendar.getInstance()
            val timestamp = calendar.timeInMillis
            if(tag == null || timestamp - tag.toString().toLong() > 3000){
                Toast.makeText(mContext, "再次点击", Toast.LENGTH_SHORT).show()
                mDialogView.be.tag = timestamp
            }else{
                mPerson.partner = null
                mPerson.partnerName = null
                mDialogView.be.tag = null
                setFamily()
            }
        }
    }

    @BindView(R.id.sch_fav)
    lateinit var mSwitchFav:Switch

    @BindView(R.id.vp_person)
    lateinit var mViewPager: ViewPager

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

    val innerCallback = object:IViewpageCallback{
        override fun update(type: Int) {
            when (type){
                1 -> {
                    setTianfu()
                    updateView()
                }
                2 -> setProfile()
                3 -> updateView()
            }
        }

    }

    private fun setViewPager(){
        val bundle = Bundle()
        bundle.putString("id", mPerson.id)
        val equip = FragmentEquipment()
        equip.arguments = bundle
        val his = FragmentPersonEvent()
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
        mViewPager.adapter = PersonPagerAdapter(childFragmentManager, mFragments)
        mViewPager.currentItem = 0
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
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + person.gender)
            var file = File(imageDir.path, person.profile.toString() + ".png")
            if (!file.exists()) {
                file = File(imageDir.path, person.profile.toString() + ".jpg")
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
                    tianFus.map { it.name },
                    tianFus.map { CommonColors[it.rarity] })

        }
    }

    fun updateView(){
        if(mContext.getOnlinePersonDetail(mPerson.id) == null){
            mThreadRunnable = false
        }
        mSwitchFav.isChecked = mPerson.isFav
        val lifeTurn = if(mPerson.lifeTurn == 0) "" else ".${mPerson.lifeTurn}"
        mDialogView.name.text ="${CultivationHelper.showing(mPerson.name)}$lifeTurn-${mPerson.ancestorLevel}"
        setFamily()
        mDialogView.alliance.text = CultivationHelper.showing(mPerson.allianceName)
        mDialogView.age.text = "${CultivationHelper.showing(mPerson.gender.props)}/${mPerson.lifetime - mPerson.age}"
        mDialogView.career.text = mPerson.careerList.map {
            val obj = CultivationHelper.mConfig.career.find { c-> c.id == it.first }!!.copy()
            obj.level = it.second
            obj
        }.joinToString()
        mDialogView.props.text =  getProperty()
        mDialogView.clan.text = CultivationHelper.showing(mContext.mClans[mPerson.ancestorId]?.name ?: "")
        mDialogView.jingjie.text = CultivationHelper.showing(mPerson.jinJieName)
        mDialogView.jingjie.setTextColor(Color.parseColor(CommonColors[mPerson.jinJieColor]))
        mDialogView.xiuwei.text = "${mPerson.xiuXei}/${mPerson.jinJieMax}"
        mDialogView.xiuweiAdd.text =  "${CultivationHelper.getXiuweiGrow(mPerson, mContext.mAlliance)}"
        mDialogView.success.text = "${CultivationHelper.getTotalSuccess(mPerson)}"
        mDialogView.lingGen.text = CultivationHelper.showing(mPerson.lingGenName)
        mDialogView.lingGen.setTextColor(Color.parseColor(CommonColors[mPerson.lingGenType.color]))

        updateViewPager()
    }

    private fun getProperty():String{
        val result = CultivationHelper.getProperty(mPerson)
        return "${result[0]}/${result[1]} ${result[2]}-${result[3]}-${result[4]}"
    }

    private fun setFamily(){
        mDialogView.partner.text = if(mPerson.partnerName != null) "<${getContent(mPerson.partnerName)}>"  else ""
        mDialogView.partner.visibility = if(mPerson.partnerName != null) View.VISIBLE else View.GONE
        val partner = mContext.getOnlinePersonDetail(mPerson.partner)
        mDialogView.be.visibility = if(mPerson.partner != null && partner == null ) View.VISIBLE else View.GONE

        val dadName = if(mPerson.parentName != null) "[${mPerson.parentName!!.first}" else null
        val mumName = if(mPerson.parentName != null) "${mPerson.parentName!!.second}]" else null
        mDialogView.parentDad.text = getContent(dadName)
        mDialogView.parentMum.text = getContent(mumName)
        mDialogView.parentDad.visibility = if(mPerson.parentName != null) View.VISIBLE else View.GONE
        mDialogView.parentMum.visibility = if(mPerson.parentName != null) View.VISIBLE else View.GONE

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

        @BindView(R.id.tv_xiuwei)
        lateinit var xiuwei:TextView

        @BindView(R.id.tv_success)
        lateinit var success:TextView

        @BindView(R.id.tv_lingGen)
        lateinit var lingGen:TextView

        @BindView(R.id.ll_tianfu)
        lateinit var tianfu:TextViewBox

        @BindView(R.id.tv_xiuwei_add)
        lateinit var xiuweiAdd:TextView

        @BindView(R.id.tv_neigong)
        lateinit var career:TextView

        @BindView(R.id.tv_clan)
        lateinit var clan:TextView

        @BindView(R.id.tv_props)
        lateinit var props:TextView

        @BindView(R.id.btn_be)
        lateinit var be:ImageButton

        @BindView(R.id.ll_parent_measure)
        lateinit var measures:LinearLayout

        @BindView(R.id.ll_profile)
        lateinit var profileBorder:LinearLayout


        init {
            ButterKnife.bind(this, view)
        }
    }

    interface IViewpageCallback{
        fun update(type:Int)
    }
}