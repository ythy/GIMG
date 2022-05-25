package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import butterknife.*
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.component.CultivationHelper.CommonColors
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.PersonEvent
import java.io.File
import java.lang.ref.WeakReference
import java.sql.Time
import java.sql.Timestamp
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
        val success = mContext.addPersonLifetime(mId)
        if(success){
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.btn_close)
    fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
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
            }
        }
    }

    @OnClick(R.id.btn_revive)
    fun onReviveHandler(){
        if(mBtnRevive.text == "Kill"){
            val success = mContext.killPerson(mId)
            if(success){
                Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
            }
        }else{
            mContext.revivePerson(mId)
            mThreadRunnable = true
        }
    }

    @BindView(R.id.btn_revive)
    lateinit var mBtnRevive:Button

    @BindView(R.id.sch_fav)
    lateinit var mSwitchFav:Switch

    @OnCheckedChanged(R.id.sch_fav)
    fun onFavSwitch(checked:Boolean){
        mPerson.isFav = checked
    }

    lateinit var mPerson:Person
    lateinit var mId:String
    lateinit var mContext:CultivationActivity
    lateinit var mDialogView:DialogView
    var mPinyinMode:Boolean = false

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mEventDataString = mutableListOf<String>()
    private var mEventData = mutableListOf<PersonEvent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialog_persion, container, false)
        ButterKnife.bind(this, v)
        mDialogView = DialogView(v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init(){
        mId = this.arguments!!.getString("id", "")
        mContext = activity as CultivationActivity
        val person = mContext.getPersonDetail(mId)
        if(person == null){
            onCloseHandler()
            return
        }
        mPerson = person
        mPinyinMode = mContext.pinyinMode
        mDialogView.events.adapter = ArrayAdapter(this.context!!,
                android.R.layout.simple_list_item_1, android.R.id.text1, mEventDataString)
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

    private fun setProfile(){
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

    private fun setTianfu(){
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

    private fun updateView(){
        val person = mContext.getPersonDetail(mId)
        if(person == null){
            onCloseHandler()
            return
        }
        mPerson = person
        if(mPerson.isDead){
            mThreadRunnable = false
            mBtnRevive.text = "Revive"
        }else{
            mBtnRevive.text = "Kill"
        }
        mSwitchFav.isChecked = mPerson.isFav
        mDialogView.name.text = if(mPinyinMode) "${mPerson.pinyinName}(${mPerson.gender})-${mPerson.ancestorLevel}" else "${mPerson.name}(${mPerson.gender.props})-${mPerson.ancestorLevel}"
        setFamily()
        mDialogView.alliance.text = mPerson.allianceName
        mDialogView.age.text = "${mPerson.age}/${mPerson.lifetime}"
        mDialogView.neigong.text = mPerson.maxXiuWei.toString()
        mDialogView.clan.text = mContext.mClans.find { it.persons.contains(mPerson.id) }?.name ?: ""
        mDialogView.jingjie.text = mPerson.jinJieName
        mDialogView.jingjie.setTextColor(Color.parseColor(CommonColors[mPerson.jinJieColor]))
        mDialogView.xiuwei.text = "${mPerson.xiuXei}/${mPerson.jinJieMax}"
        mDialogView.xiuweiAdd.text = ((mPerson.lingGenType.qiBasic + mPerson.extraXiuwei + mPerson.allianceXiuwei) * ((mPerson.extraXuiweiMulti + 100).toDouble() / 100 )).toInt().toString() + "(${mPerson.allianceXiuwei})"
        val currentJinJie = mContext.getJingJie(mPerson.jingJieId)
        var bonus = 0
        if(currentJinJie.bonus > 0 && mPerson.lingGenType.jinBonus.isNotEmpty()){
            bonus = mPerson.lingGenType.jinBonus[currentJinJie.bonus - 1]
        }
        mDialogView.success.text = "${mPerson.jingJieSuccess + mPerson.extraTupo + mPerson.allianceSuccess + bonus}"
        mDialogView.lingGen.text = mPerson.lingGenName
        mDialogView.lingGen.setTextColor(Color.parseColor(CommonColors[mPerson.lingGenType.color]))
        val eventChanged = mEventData.size != mPerson.events.size
        mPerson.events.forEach {
            if(mEventData.find { e-> e.nid == it.nid} == null){
                mEventData.add(it)
                mEventDataString.add(0, it.content)
            }
        }
        if(eventChanged){
            (mDialogView.events.adapter as BaseAdapter).notifyDataSetChanged()
            mDialogView.events.invalidateViews()
        }

    }

    private fun setFamily(){

        mDialogView.partner.text = if(mPerson.partnerName != null) "<${getContent(mPerson.partnerName)}>"  else ""
        mDialogView.partner.visibility = if(mPerson.partnerName != null) View.VISIBLE else View.GONE
        mDialogView.be.visibility = if(mPerson.partnerName != null) View.VISIBLE else View.GONE

        val dadName = if(mPerson.parentName != null) "[${mPerson.parentName!!.first}" else null
        val mumName = if(mPerson.parentName != null) "${mPerson.parentName!!.second}]" else null
        mDialogView.parentDad.text = getContent(dadName)
        mDialogView.parentMum.text = getContent(mumName)
        mDialogView.parentDad.visibility = if(mPerson.parentName != null) View.VISIBLE else View.GONE
        mDialogView.parentMum.visibility = if(mPerson.parentName != null) View.VISIBLE else View.GONE

        val children = mPerson.children.filter { mContext.getOnlinePersonDetail(it) != null }.map { mContext.getOnlinePersonDetail(it) }
        mDialogView.children.removeAllViews()
        if(children.isNotEmpty()){
            children.forEach {
                val person = it!!
                val textView = TextView(this.context)
                textView.text =  if(mPinyinMode) person.pinyinName else person.name
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
        return if(mPinyinMode)
            PinyinUtil.convert(name)
        else
            name
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
        lateinit var children:LinearLayout

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

        @BindView(R.id.lv_events)
        lateinit var events:ListView

        @BindView(R.id.ll_tianfu)
        lateinit var tianfu:LinearLayout

        @BindView(R.id.tv_xiuwei_add)
        lateinit var xiuweiAdd:TextView

        @BindView(R.id.tv_neigong)
        lateinit var neigong:TextView

        @BindView(R.id.tv_clan)
        lateinit var clan:TextView

        @BindView(R.id.btn_be)
        lateinit var be:ImageButton

        init {
            ButterKnife.bind(this, view)
        }
    }
}