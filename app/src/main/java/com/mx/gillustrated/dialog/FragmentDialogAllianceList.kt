package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.AllianceListAdapter
import com.mx.gillustrated.databinding.FragmentDialogAllianceListBinding
import com.mx.gillustrated.vo.cultivation.Alliance
import com.mx.gillustrated.vo.cultivation.Person
import java.lang.ref.WeakReference

@SuppressLint("SetTextI18n")
class FragmentDialogAllianceList  : DialogFragment() {

    companion object{

        fun newInstance(): FragmentDialogAllianceList {
            return FragmentDialogAllianceList()
        }

        class TimeHandler constructor(val context: FragmentDialogAllianceList): Handler(Looper.getMainLooper()){

            private val reference: WeakReference<FragmentDialogAllianceList> = WeakReference(context)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val dialog = reference.get()
                if(msg.what == 1 && dialog != null ){
                    dialog.updateView()
                }
            }
        }

    }

    private var _binding: FragmentDialogAllianceListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mContext: CultivationActivity
    var mAllianceListData: MutableList<Alliance> = mutableListOf()
    private val mTimeHandler: TimeHandler = TimeHandler(this)
    private var mThreadRunnable:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogAllianceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        binding.lvAlliance.adapter = AllianceListAdapter(requireContext(), mAllianceListData)
        initListener()
        updateView()
        registerTimeLooper()

    }

    private fun initListener(){

        binding.btnClose.setOnClickListener {
            mThreadRunnable = false
            this.dismiss()
        }
        binding.lvAlliance.setOnItemClickListener { _, _, position, _ ->
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogAlliance.newInstance()
            newFragment.isCancelable = false

            val bundle = Bundle()
            bundle.putString("id", mAllianceListData[position].id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_alliance_info")
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

    private fun updateView(){
        mAllianceListData.clear()
        mAllianceListData.addAll(mContext.mAlliance.map { it.value })
        mAllianceListData.forEach {
            it.totalXiuwei = it.personList.reduceValuesToLong(1000,
                    { p: Person -> p.lifeTurn.toLong() }, 0, { left, right -> left + right })
        }
        mAllianceListData.sortWith(compareByDescending<Alliance> {it.battleWinner}.thenByDescending { it.totalXiuwei })
        (binding.lvAlliance.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvAlliance.invalidateViews()
        binding.tvTotal.text = mAllianceListData.sumOf { it.personList.size }.toString()
    }

}