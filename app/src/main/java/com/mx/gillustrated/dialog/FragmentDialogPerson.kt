package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
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
import com.j256.ormlite.stmt.query.In
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.PersonPagerAdapter
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.AutoWrapLinearLayout
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.fragment.FragmentEquipment
import com.mx.gillustrated.fragment.FragmentPersonEvent
import com.mx.gillustrated.fragment.FragmentPersonInfo
import com.mx.gillustrated.fragment.FragmentTeJi
import com.mx.gillustrated.util.CommonUtil
import com.mx.gillustrated.util.PinyinUtil
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

    @OnClick(R.id.btn_life)
    fun onLifetimeHandler(){
        val success = mContext.addPersonLifetime(mPerson.id)
        if(success){
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
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

    @OnClick(R.id.btn_revive)
    fun onReviveHandler(){
        if(mBtnRevive.text == "Kill"){
            mContext.killPerson(mPerson.id)
        }else{
            mContext.revivePerson(mPerson.id)
            mThreadRunnable = true
        }
    }

    @BindView(R.id.btn_revive)
    lateinit var mBtnRevive:Button

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

    //RelativeLayout 需要
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
        val person = mContext.getOnlinePersonDetail(id) ?: mContext.getOfflinePersonDetail(id)
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
        mFragments.clear()
        mFragments.add(equip)
        mFragments.add(info)
        mFragments.add(teji)
        mFragments.add(his)
        mViewPager.adapter = PersonPagerAdapter(childFragmentManager, mFragments)
        mViewPager.currentItem = 0
    }

    private fun updateViewPager(){
        if(mViewPager.currentItem == 3){
            val fragment:FragmentPersonEvent = mFragments[3] as FragmentPersonEvent
            fragment.updateEvent()
        }
    }

    fun setProfile(){
        val person = mPerson
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
        mDialogView.tianfu.removeAllViews()
        val tianFus = mPerson.tianfus
        if(tianFus.isNotEmpty()){
            tianFus.forEach {
                val data = it
                val textView = TextView(this.context)
                textView.text = data.name
                textView.setTextColor(Color.parseColor(CommonColors[data.rarity]))
                textView.setOnClickListener {
                    var text = ""
                    when {
                        data.type == 1 -> text = "基础修为"
                        data.type == 2 -> text = "修为加速"
                        data.type == 3 -> text = "Life"
                        data.type == 4 -> text = "突破"
                        data.type == 5 -> text = "福源"
                    }
                    Toast.makeText(this.context, "${text}增加${data.bonus}", Toast.LENGTH_SHORT).show()
                }
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.marginEnd = 20
                textView.layoutParams = layoutParams
                mDialogView.tianfu.addView(textView)
            }

        }
    }

    fun updateView(){
        if(mContext.getOnlinePersonDetail(mPerson.id) == null){
            mThreadRunnable = false
            mBtnRevive.text = "Revive"
        }else{
            mBtnRevive.text = "Kill"
        }
        mSwitchFav.isChecked = mPerson.isFav
        val lifeTurn = if(mPerson.lifeTurn == 0) "" else ".${mPerson.lifeTurn}"
        mDialogView.name.text ="${CultivationHelper.showing(mPerson.name)}$lifeTurn(${CultivationHelper.showing(mPerson.gender.props)})-${mPerson.ancestorLevel}"
        setFamily()
        mDialogView.alliance.text = CultivationHelper.showing(mPerson.allianceName)
        mDialogView.age.text = "${mPerson.age}/${mPerson.lifetime}"
        mDialogView.neigong.text = mPerson.maxXiuWei.toString()
        mDialogView.props.text =  getProperty()
        mDialogView.clan.text = CultivationHelper.showing(mContext.mClans[mPerson.ancestorId]?.name ?: "")
        mDialogView.jingjie.text = CultivationHelper.showing(mPerson.jinJieName)
        mDialogView.jingjie.setTextColor(Color.parseColor(CommonColors[mPerson.jinJieColor]))
        mDialogView.xiuwei.text = "${mPerson.xiuXei}/${mPerson.jinJieMax}"
        mDialogView.xiuweiAdd.text =  "${CultivationHelper.getXiuweiGrow(mPerson, mContext.mAlliance)}" + "(${mPerson.allianceXiuwei})"
        mDialogView.success.text = "${CultivationHelper.getTotalSuccess(mPerson)}"
        mDialogView.lingGen.text = CultivationHelper.showing(mPerson.lingGenName)
        mDialogView.lingGen.setTextColor(Color.parseColor(CommonColors[mPerson.lingGenType.color]))

        updateViewPager()
    }

    private fun getProperty():String{
        val result = CultivationHelper.getProperty(mPerson)
        return "${result[0]}/${result[1]} ${result[2]}-${result[3]}-${result[4]} (${mPerson.teji.size})"
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
        lateinit var tianfu:LinearLayout

        @BindView(R.id.tv_xiuwei_add)
        lateinit var xiuweiAdd:TextView

        @BindView(R.id.tv_neigong)
        lateinit var neigong:TextView

        @BindView(R.id.tv_clan)
        lateinit var clan:TextView

        @BindView(R.id.tv_props)
        lateinit var props:TextView

        @BindView(R.id.btn_be)
        lateinit var be:ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }

    interface IViewpageCallback{
        fun update(type:Int)
    }
}