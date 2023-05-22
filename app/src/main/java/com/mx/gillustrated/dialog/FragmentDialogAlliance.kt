package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogAllianceBinding
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

@SuppressLint("SetTextI18n")
class FragmentDialogAlliance : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogAlliance {
            return FragmentDialogAlliance()
        }
        class TimeHandler constructor(val context: FragmentDialogAlliance): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogAlliance> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }

    private var _binding: FragmentDialogAllianceBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mAlliance: Alliance
    private lateinit var mId:String
    lateinit var mContext:CultivationActivity

    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mPersonList = mutableListOf<Person>()
    private val nation = CultivationHelper.mConfig.nation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogAllianceBinding.inflate(inflater, container, false)
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
            mThreadRunnable = false
            this.dismiss()
        }
        binding.tvName.setOnClickListener {
            if (mAlliance.type != 5)
                return@setOnClickListener
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogEmperor.newInstance(mAlliance.personList.map { it.value }.shuffled()[0].id)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_emperor")
        }
        binding.btnInsert.setOnClickListener {
            val name = binding.etInsert.text.toString()
            val person = mContext.mPersons.map { it.value }.find { it.name == name || PinyinUtil.convert(it.name) == name }
            if (person != null){
                CultivationHelper.changedToFixedAlliance(person, mContext.mAlliance, mAlliance)
                binding.etInsert.setText("")
                Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvWinner.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogRank.newInstance(3, mAlliance.id)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_rank_info")
        }
        binding.tvXiuwei.setOnClickListener {
            val prop = mAlliance.property.joinToString()
            Toast.makeText(mContext, prop, Toast.LENGTH_SHORT).show()
        }
        binding.lvPerson.setOnItemClickListener { _, _, position, _ ->
            showPersonInfo(mPersonList[position].id)
        }

    }

    fun init(){
        mId = this.requireArguments().getString("id", "")
        mContext = activity as CultivationActivity
        mAlliance = mContext.mAlliance[mId]!!
        val abridgeName = if (mAlliance.abridgeName != "") "(${mAlliance.abridgeName})" else ""

        binding.tvName.text = CultivationHelper.showing("${nation.find { it.id == mAlliance.nation }?.name}-${mAlliance.name}$abridgeName")
        binding.tvLifetime.text = "life: ${mAlliance.lifetime}"
        binding.tvXiuwei.text = "xiuwei: ${mAlliance.xiuwei}(${mAlliance.xiuweiMulti})  ↑${mAlliance.success}"
        binding.lvPerson.adapter = CultivationPersonListAdapter(this.requireContext(), mPersonList, showStar = true, showSpecEquipment = true)
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

    private fun updateView(){
        if(mAlliance.zhuPerson == null){
            binding.tvZhu.text = ""
        }else{
            val zhuName = CultivationHelper.showing(mAlliance.zhuPerson!!.name)
            binding.tvZhu.text = zhuName
        }
        binding.tvWinner.text = "${mAlliance.battleWinner}-${mAlliance.xiuweiBattle}↑"


        mPersonList.clear()
        mPersonList.addAll(mAlliance.personList.map { it.value }.toMutableList())
        mPersonList.sortWith(compareByDescending<Person>{ it.lifeTurn }.thenByDescending { it.jingJieId })
        (binding.lvPerson.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvPerson.invalidateViews()

        binding.tvTotal.text = "${mPersonList.size}-${mPersonList.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
    }

    private fun showPersonInfo(id:String?){
        if(id == null)
            return
        val ft = mContext.supportFragmentManager.beginTransaction()
        // Create and show the dialog.
        val newFragment = FragmentDialogPerson.newInstance()
        newFragment.isCancelable = false

        val bundle = Bundle()
        bundle.putString("id", id)
        newFragment.arguments = bundle
        newFragment.show(ft, "dialog_person_info")
    }

}