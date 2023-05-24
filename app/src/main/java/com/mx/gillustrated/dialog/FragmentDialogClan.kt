package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogClanBinding
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference


@SuppressLint("SetTextI18n")
class FragmentDialogClan : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogClan {
            return FragmentDialogClan()
        }
        class TimeHandler constructor(val context: FragmentDialogClan): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogClan> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }

    private var _binding: FragmentDialogClanBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mId:String
    lateinit var mContext:CultivationActivity


    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mPersonList = mutableListOf<Person>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogClanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        mId = requireArguments().getString("id", "")
        mContext = activity as CultivationActivity
        val clan = mContext.mClans[mId]
        if(clan != null){
            binding.lvPerson.adapter = CultivationPersonListAdapter(requireContext(), mPersonList, showStar = true, showSpecEquipment = true)
            updateName()
            updateCrest()
            updateView()
            registerTimeLooper()
        }
    }

    private fun initListener(){

        binding.btnClose.setOnClickListener {
            onCloseHandler()
        }
        binding.lvPerson.setOnItemClickListener { _, _, position, _ ->
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPerson.newInstance()
            newFragment.isCancelable = false

            val bundle = Bundle()
            bundle.putString("id", mPersonList[position].id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_person_info")
        }
        binding.tvXiuwei.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogRank.newInstance(4, mId)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_rank_info")
        }
        binding.btnAdd.setOnClickListener {
            val name = binding.etAbdicate.text.toString()
            val person = mContext.mPersons.map { it.value }.find { it.name == name || PinyinUtil.convert(it.name) == name }
            if (person != null){
                CultivationHelper.addPersonToClan(person,  mContext.mClans[mId]!!, mContext.mClans, mContext.mPersons)
                binding.etAbdicate.setText("")
                Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnChange.setOnClickListener {
            mContext.mClans[mId]?.name =  binding.etName.text.toString()
            mContext.mClans[mId]?.nickName = binding.etNickName.text.toString()
            updateName()
        }
        binding.etCrest.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = s.toString()
                if(current != ""){
                    mContext.mClans[mId]?.crest = current.toInt()
                    updateCrest()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onCloseHandler(){
        mThreadRunnable = false
        this.dismiss()
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

    private fun updateName(){
        val clan = mContext.mClans[mId]!!
        binding.tvName.text = CultivationHelper.showing("${clan.nickName}${if(clan.name == clan.nickName) "" else "-${clan.name}"}")
        binding.etName.setText(CultivationHelper.showing(clan.name))
        binding.etNickName.setText(CultivationHelper.showing(clan.nickName))
        binding.ivCrestName.text = CultivationHelper.showing(clan.nickName)
        binding.etCrest.setText(clan.crest.toString())
    }

    private fun updateCrest(){
        val profileFrame = CultivationHelper.getClanCrest(mContext.mClans[mId]?.crest ?: 0)
        if(profileFrame.first != -1){
            binding.llCrest.background = AppCompatResources.getDrawable(requireContext(),profileFrame.first)
            binding.llCrest.backgroundTintList = if(profileFrame.second != -1) ColorStateList.valueOf(profileFrame.second) else null
        }else{
            binding.llCrest.background = null
            binding.llCrest.backgroundTintList = null
        }
    }

    private fun updateView(){
        val clan = mContext.mClans[mId]
        if(clan == null){
            onCloseHandler()
            return
        }
        val personList = clan.clanPersonList.map { it.value }
        if(personList.isEmpty()){
            onCloseHandler()
            return
        }
        binding.tvTotal.text = "${personList.size}-${personList.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        binding.tvZhu.text = if(clan.zhu?.name == null ) "" else CultivationHelper.showing(clan.zhu!!.name)
        binding.tvXiuwei.text = "${clan.battleWinner}-${clan.xiuweiBattle}↑"
        mPersonList.clear()
        mPersonList.addAll(personList.sortedBy { it.ancestorLevel })
        (binding.lvPerson.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvPerson.invalidateViews()

    }


}