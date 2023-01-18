package com.mx.gillustrated.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.CultivationFollowerAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationHelper.mConfig
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.Follower
import com.mx.gillustrated.vo.cultivation.FollowerConfig
import com.mx.gillustrated.vo.cultivation.Person

@RequiresApi(Build.VERSION_CODES.N)
class FragmentFollower: Fragment() {

    private val mConfigFollower = CultivationHelper.mConfig.follower

    @BindView(R.id.lv_follower)
    lateinit var mListView: ListView

    @BindView(R.id.spinner_follower)
    lateinit var mSpinner: Spinner

    @OnClick(R.id.btn_save)
    fun onSaveClick(){
        val name = NameUtil.getChineseName(null, mCurrentSelected.gender)
        mPerson.followerList.add(Follower(mCurrentSelected.id, name.first + name.second))
        updateList()
    }

    lateinit var mContext: CultivationActivity
    lateinit var mPerson: Person
    private val mFollowers: MutableList<Follower> = mutableListOf()
    lateinit var mCurrentSelected: FollowerConfig

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vp_folower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        mContext = activity as CultivationActivity
        init()
    }

    fun init(){
        val id = this.arguments!!.getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        initSpinner()
        mListView.adapter = CultivationFollowerAdapter(this.context!!, mFollowers, object : CultivationFollowerAdapter.FollowerAdapterCallback {
            override fun onDeleteHandler(follower: Follower) {
                mPerson.followerList.removeIf {
                    it.id == follower.id && it.uniqueName == follower.uniqueName
                }
                updateList()
            }
        })
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
        (mListView.adapter as BaseAdapter).notifyDataSetChanged()
        mListView.invalidateViews()
    }

    fun initSpinner(){
        val list = mConfigFollower.filter { it.type == 0 }.sortedBy { it.rarity }
        mCurrentSelected = list[0]
        val adapter = ArrayAdapter<FollowerConfig>(context!!,
                android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner.adapter = adapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val follower = parent.selectedItem as FollowerConfig
                mCurrentSelected = follower
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

}