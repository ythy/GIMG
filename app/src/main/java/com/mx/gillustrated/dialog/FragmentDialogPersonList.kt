package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogPersionListBinding
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

//type 0 all; 1 never dead; 2 spec career
//type 3 persons has spec tips
//type 4 persons has amulet
//type 5 persons has label
//type 6 persons set skin
@SuppressLint("SetTextI18n")
class  FragmentDialogPersonList constructor(private val mType:Int)  : DialogFragment() {

    companion object{

        fun newInstance(type:Int = 0): FragmentDialogPersonList {
            return FragmentDialogPersonList(type)
        }

        class TimeHandler constructor(val context: FragmentDialogPersonList): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogPersonList> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateList()
                }
            }
        }
    }

    private var _binding: FragmentDialogPersionListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    lateinit var mContext:CultivationActivity
    private var mThreadRunnable:Boolean = true
    private var mSort = "T"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogPersionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init(){
        binding.lvPerson.layoutManager = LinearLayoutManager(requireContext())
        binding.lvPerson.itemAnimator = null
        binding.lvPerson.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        binding.lvPerson.adapter = CultivationPersonListAdapter(showStar = false, showSpecEquipment = false,
                object : CultivationPersonListAdapter.Callback{
                    override fun onItemClick(item: Person) {
                        val ft = mContext.supportFragmentManager.beginTransaction()
                        // Create and show the dialog.
                        val newFragment = FragmentDialogPerson.newInstance()
                        newFragment.isCancelable = false
                        val bundle = Bundle()
                        bundle.putString("id", item.id)
                        newFragment.arguments = bundle
                        newFragment.show(ft, "dialog_person_info")
                    }
                })
        updateList()
        initListener()
        registerTimeLooper()
    }

    private fun sortBtnHandler(btn:Button){
        binding.btnSortT.setTextColor(mContext.getColor(R.color.color_blue))
        binding.btnSortX.setTextColor(mContext.getColor(R.color.color_blue))
        binding.btnSortB.setTextColor(mContext.getColor(R.color.color_blue))
        btn.setTextColor(Color.parseColor("white"))
        mSort = btn.tag.toString()
        setOnlineList()
    }

    private fun initListener(){
        binding.btnSortT.setOnClickListener {
            sortBtnHandler(it as Button)
        }
        binding.btnSortX.setOnClickListener {
            sortBtnHandler(it as Button)
        }
        binding.btnSortB.setOnClickListener {
            sortBtnHandler(it as Button)
        }
        binding.btnClose.setOnClickListener {
            mThreadRunnable = false
            this.dismiss()
        }
        binding.btnBe.setOnClickListener {
            mContext.bePerson()
        }
        binding.btnClear.setOnClickListener {
            mContext.mDeadPersons.clear()
        }
        binding.btnSwitch.setOnClickListener {
            if (mType > 0)
                return@setOnClickListener
            val btn = it as Button
            val tag = btn.tag
            if(tag == "ON"){
                btn.text = "offline"
                btn.tag = "OFF"
                binding.btnBe.visibility = View.VISIBLE
                binding.btnClear.visibility = View.VISIBLE
                setOfflineList()
            }else{
                btn.text = "online"
                btn.tag = "ON"
                binding.btnBe.visibility = View.GONE
                binding.btnClear.visibility = View.GONE
                setOnlineList()
            }
        }
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

    private fun updateList(){
        if(binding.btnSwitch.tag == "ON"){
            setOnlineList()
        }else{
            setOfflineList()
        }
    }

    private fun setOfflineList(){
        val list = mContext.mDeadPersons.map { it.value }.toMutableList()
        list.sortByDescending { it.lifeTurn * 1000000 + it.jinJieMax}
        (binding.lvPerson.adapter as CultivationPersonListAdapter).submitList(list)
        binding.tvTotal.text = list.size.toString()
    }

    private fun setOnlineList(){
        val persons = when (mType) {
            1 -> mContext.mPersons.map { it.value }.filter { CultivationHelper.isNeverDead(it) }
            2 -> mContext.mPersons.map { it.value }.filter { p-> (p.careerList.maxByOrNull { m-> m.detail.rarity }?.detail?.rarity ?: 0) >= 8 }
            3 -> mContext.mPersons.map { it.value }.filter { p->
                p.tipsList.find { t-> t.detail.type > 2 } != null
            }
            4 -> mContext.mPersons.map { it.value }.filter { p->
               p.equipmentList.find { e-> e.detail.type == 5 } != null
            }
            5 -> mContext.mPersons.map { it.value }.filter { p ->
                p.label.mapNotNull { m -> CultivationHelper.mConfig.label.find { f -> f.id == m } }
                        .any { it.rarity >= 9 }
            }
            6 -> mContext.mPersons.map { it.value }.filter { p -> p.skin != ""}
            else -> mContext.mPersons.map { it.value }
        }
        val filterString = binding.etName.text.toString()
        val list =  if(filterString == "" )
                        persons.toMutableList()
                    else
                        persons.filter {
                            it.name.startsWith(filterString, true) || PinyinUtil.convert(it.name).startsWith(filterString, true)
                        }.toMutableList()

        if(mType == 2)
            mSort = "C"
        if(mType == 4)
            mSort = "E"
        if(mType == 5)
            mSort = "L"

        when (mSort) {
            "T" -> list.sortWith(compareByDescending<Person> {it.lifeTurn}
                    .thenByDescending { it.jingJieId }
                    .thenByDescending { it.xiuXei } )
            "X" -> list.sortWith(compareByDescending<Person> { CultivationHelper.getXiuweiGrow(it) }
                    .thenByDescending { it.lifeTurn })
            "B" -> list.sortWith(compareByDescending<Person> {it.battleWinner}
                    .thenByDescending { it.lifeTurn })
            "E" -> list.sortWith(compareByDescending { it.equipmentList.filter {
                        e-> e.amuletSerialNo > 0
                    }.sumOf { s-> s.detail.rarity }})
            "C" -> list.sortByDescending{
                val max = it.careerList.maxByOrNull { m-> m.detail.rarity }
                (max?.detail?.rarity ?: 0) * 1000 + (max?.level ?: 0)
            }
            "L" -> list.sortByDescending{ p->
                p.label.mapNotNull { m -> CultivationHelper.mConfig.label.find { f-> f.id == m } }.sumOf {
                    it.weight
                }
            }

        }
        (binding.lvPerson.adapter as CultivationPersonListAdapter).submitList(list)
        binding.tvTotal.text = "${list.size}-${list.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
    }
}