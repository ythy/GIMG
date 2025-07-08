package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.gillustrated.activity.GameBaseActivity
import com.mx.gillustrated.adapter.CultivationPersonListAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogNationBinding
import com.mx.gillustrated.vo.cultivation.Nation
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference


@SuppressLint("SetTextI18n")
class FragmentDialogNation : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogNation {
            return FragmentDialogNation()
        }
        class TimeHandler constructor(val context: FragmentDialogNation): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogNation> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }
    }

    private var _binding: FragmentDialogNationBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mNation: Nation
    lateinit var mContext:GameBaseActivity
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogNationBinding.inflate(inflater, container, false)
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

    private fun initListener(){
        binding.btnClose.setOnClickListener {
            mThreadRunnable = false
            this.dismiss()
        }
        binding.tvXiuwei.setOnClickListener {
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogRank.newInstance(5, mNation.id)
            newFragment.isCancelable = false
            newFragment.show(ft, "dialog_rank_info")
        }
    }

    fun init(){
        val nationId = requireArguments().getString("id", "")
        mContext = activity as GameBaseActivity
        mNation = mContext.mNations[nationId]!!
        binding.tvName.text = CultivationHelper.showing(mNation.name)
        binding.lvPerson.layoutManager = LinearLayoutManager(mContext)
        binding.lvPerson.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        binding.lvPerson.adapter = CultivationPersonListAdapter(showStar = true, showSpecEquipment = false,
                object : CultivationPersonListAdapter.Callback{
                    override fun onItemClick(item: Person) {
                        showPersonInfo(item.id)
                    }
                })

        updateView()
        registerTimeLooper()
    }

    private fun registerTimeLooper(){
        Thread{
            while (true){
                Thread.sleep(10000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }.start()
    }

    private fun updateView(){
        val list = mContext.mPersons.filter { it.value.nationId == mNation.id }.map { it.value }.
                    sortedWith(compareByDescending<Person>{ it.lifeTurn }.thenByDescending { it.jingJieId })
        (binding.lvPerson.adapter as CultivationPersonListAdapter).submitList(list)
        binding.tvTotal.text = "${list.size}-${list.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
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