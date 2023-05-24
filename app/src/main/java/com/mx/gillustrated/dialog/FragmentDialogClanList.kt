package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationClanListAdapter
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

//    @BindView(R.id.lv_clan)
//    lateinit var mListView: ListView
//
//    @BindView(R.id.tv_total)
//    lateinit var mTotalText: TextView




    private var _binding: FragmentDialogClanListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var mContext: CultivationActivity
    private var mClanListData: MutableList<Clan> = mutableListOf()
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
        binding.lvClan.adapter = CultivationClanListAdapter(requireContext(), mClanListData)
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
        binding.lvClan.setOnItemClickListener { _, _, position, _ ->
            if(mClanListData[position].clanPersonList.map { it.value }.isEmpty()){
                Toast.makeText(mContext, "size 0", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }
            val ft = mContext.supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogClan.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", mClanListData[position].id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_clan_info")
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
        mClanListData.clear()
        mContext.mClans.forEach {
            it.value.totalXiuwei = it.value.clanPersonList.map { c->c.value }.sumOf { s-> s.lifeTurn.toDouble() }.toLong()
        }
        mClanListData.addAll(mContext.mClans.map { it.value })
        mClanListData.sortByDescending { it.totalXiuwei }
        (binding.lvClan.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvClan.invalidateViews()
        binding.tvTotal.text = mClanListData.size.toString()
    }
}