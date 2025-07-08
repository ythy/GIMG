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
import com.mx.gillustrated.adapter.CultivationNationAdapter
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentDialogNationListBinding
import com.mx.gillustrated.vo.cultivation.Nation
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("SetTextI18n")
class FragmentDialogNationList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogNationList {
            return FragmentDialogNationList()
        }

        class TimeHandler constructor(val context: FragmentDialogNationList): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogNationList> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }


    private var _binding: FragmentDialogNationListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var mContext: GameBaseActivity
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogNationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as GameBaseActivity
        init()
        updateView()
        registerTimeLooper()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init(){
        binding.btnClose.setOnClickListener {
            mThreadRunnable = false
            this.dismiss()
        }
        binding.lvNation.layoutManager = LinearLayoutManager(mContext)
        binding.lvNation.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        binding.lvNation.adapter = CultivationNationAdapter(object : CultivationNationAdapter.Callback{
            override fun onItemClick(item: Nation) {
                val ft = mContext.supportFragmentManager.beginTransaction()
                val newFragment = FragmentDialogNation.newInstance()
                newFragment.isCancelable = false
                val bundle = Bundle()
                bundle.putString("id", item.id)
                newFragment.arguments = bundle
                newFragment.show(ft, "dialog_nation_info")
            }
        })
    }

    private fun registerTimeLooper(){
        Thread{
            while (true){
                Thread.sleep(4000)
                if(mThreadRunnable){
                    val message = Message.obtain()
                    message.what = 1
                    mTimeHandler.sendMessage(message)
                }
            }
        }.start()
    }

    private fun updateView(){
        val list = mContext.mNations.map { c->
            val nation = c.value.copy()
            val list =  mContext.mPersons.filter { it.value.nationId == nation.id }
            nation.totalPerson = "${list.size}-${ list.count { it.value.lifeTurn >= CultivationSetting.TEMP_SP_JIE_TURN }}"
            nation.totalTurn =  list.map { it.value }.sumOf { it.lifeTurn }
            nation
        }.sortedByDescending { it.totalTurn }
        (binding.lvNation.adapter as CultivationNationAdapter).submitList(list)
        binding.tvTotal.text = list.size.toString()
    }

}