package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationClanListAdapter
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogClanListBinding
import com.mx.gillustrated.vo.cultivation.Clan
import java.lang.ref.WeakReference

@SuppressLint("SetTextI18n")
class FragmentDialogClanList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogClanList {
            return FragmentDialogClanList()
        }

        class TimeHandler constructor(val context: FragmentDialogClanList): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogClanList> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }

    private var _binding: FragmentDialogClanListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mContext: CultivationActivity
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogClanListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        binding.lvClan.layoutManager = LinearLayoutManager(mContext)
        binding.lvClan.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        binding.lvClan.adapter = CultivationClanListAdapter(object : CultivationClanListAdapter.Callback{
            override fun onItemClick(item: Clan) {
                if(mContext.mPersons.filterValues { p-> p.clanId == item.id }.isEmpty()){
                    Toast.makeText(mContext, "size 0", Toast.LENGTH_SHORT).show()
                    return
                }
                val ft = mContext.supportFragmentManager.beginTransaction()
                val newFragment = FragmentDialogClan.newInstance()
                newFragment.isCancelable = false
                val bundle = Bundle()
                bundle.putString("id",item.id)
                newFragment.arguments = bundle
                newFragment.show(ft, "dialog_clan_info")
            }
        })

        initListener()
        updateView()
        registerTimeLooper()
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

    fun updateView(){
        mContext.mClans.forEach {
            val list = mContext.mPersons.filterValues { p-> p.clanId == it.key }.map { m -> m.value }
            it.value.totalXiuwei = list.sumOf { s-> s.lifeTurn.toDouble() }.toLong()
            it.value.totalPerson = "${list.size}-${list.count { it.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
        }
        val list = mContext.mClans.map { it.value }.toMutableList()
        list.sortByDescending { it.totalXiuwei }
        (binding.lvClan.adapter as CultivationClanListAdapter).submitList(list)
        binding.tvTotal.text = list.size.toString()
    }
}