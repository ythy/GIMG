package com.mx.gillustrated.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mx.gillustrated.activity.GameBaseActivity
import com.mx.gillustrated.adapter.CultivationFollowerAdapter
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.databinding.FragmentVpFolowerBinding
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Follower
import com.mx.gillustrated.vo.cultivation.FollowerConfig
import com.mx.gillustrated.vo.cultivation.Person


class FragmentFollower: Fragment() {

    private val mConfigFollower = mConfig.follower
    private var _binding: FragmentVpFolowerBinding? = null
    private val binding get() = _binding!!
    lateinit var mContext: GameBaseActivity
    lateinit var mPerson: Person
    private val mFollowers: MutableList<Follower> = mutableListOf()
    lateinit var mCurrentSelected: FollowerConfig

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVpFolowerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as GameBaseActivity
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        val id = requireArguments().getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        initSpinner()
        binding.lvFollower.adapter = CultivationFollowerAdapter(requireContext(), mFollowers, object : CultivationFollowerAdapter.FollowerAdapterCallback {
            override fun onDeleteHandler(follower: Follower) {
                mPerson.followerList.removeIf {
                    it.id == follower.id && it.uniqueName == follower.uniqueName
                }
                updateList()
            }
        })
        binding.btnSave.setOnClickListener {
            val name = NameUtil.getChineseName(null, mCurrentSelected.gender)
            mPerson.followerList.add(Follower(mCurrentSelected.id, name.first + name.second))
            updateList()
        }
        updateList()
    }

    fun updateList(){
        mFollowers.clear()
        mFollowers.addAll(mPerson.followerList.sortedBy { it.detail.rarity })

        mPerson.equipmentList.forEach {
            it.detail.follower.forEach { id->
                mFollowers.add(Follower(id))
            }
        }
        mPerson.label.forEach {
            val label = mConfig.label.find { f-> f.id == it }!!.copy()
            label.follower.forEach { id->
                mFollowers.add(Follower(id))
            }
        }
        mPerson.tipsList.forEach { tips->
            tips.detail.follower.forEach { id->
                mFollowers.add(Follower(id))
            }
        }
        (binding.lvFollower.adapter as BaseAdapter).notifyDataSetChanged()
        binding.lvFollower.invalidateViews()
    }

    private fun initSpinner(){
        val list = mConfigFollower.filter { it.type == 0 }.sortedBy { it.rarity }
        mCurrentSelected = list[0]
        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFollower.adapter = adapter
        binding.spinnerFollower.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val follower = parent.selectedItem as FollowerConfig
                mCurrentSelected = follower
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

}