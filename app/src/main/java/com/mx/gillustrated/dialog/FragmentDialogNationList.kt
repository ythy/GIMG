package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationNationAdapter
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
    lateinit var mContext: CultivationActivity
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true
    private var mNationList = listOf<Nation>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogNationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        updateView()
        registerTimeLooper()
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
        binding.lvNation.setOnItemClickListener { _, _, position, _ ->
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogNation.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", mNationList[position].id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_nation_info")
        }
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
        mNationList = mContext.mNations.map { c->
            val nation = c.value.copy()
            nation.nationPersonList = ConcurrentHashMap(mContext.mPersons.filter { it.value.nationId == nation.id })
            nation.totalTurn =  nation.nationPersonList.map { it.value }.sumOf { it.lifeTurn }
            nation
        }.sortedByDescending { it.totalTurn }
        binding.lvNation.adapter = CultivationNationAdapter(requireContext(), mNationList)
        binding.tvTotal.text = mNationList.sumOf { it.nationPersonList.size }.toString()
    }

}