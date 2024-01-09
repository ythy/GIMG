package com.mx.gillustrated.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.adapter.SpinnerCommonAdapter
import com.mx.gillustrated.component.CultivationHelper
import com.mx.gillustrated.component.CultivationSetting
import com.mx.gillustrated.databinding.FragmentVpPersonBinding
import com.mx.gillustrated.dialog.FragmentDialogPerson
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.util.PinyinUtil
import com.mx.gillustrated.vo.SpinnerInfo
import com.mx.gillustrated.vo.cultivation.Person
import com.mx.gillustrated.vo.cultivation.Skin

@SuppressLint("SetTextI18n")
class FragmentPersonInfo(private val mCallback: FragmentDialogPerson.IViewpageCallback)  : Fragment(){

    lateinit var mContext: CultivationActivity
    private var _binding: FragmentVpPersonBinding? = null
    private val binding get() = _binding!!
    private lateinit var mPerson: Person
    private val mSkinList:MutableList<Skin> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVpPersonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = activity as CultivationActivity
        init()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initListener(){
        binding.btnSaveSkin.setOnClickListener {
            val index = binding.spinnerSkin.selectedItemPosition
            mPerson.skin = "M${mSkinList[index].id}"
            CultivationHelper.updatePersonExtraProperty(mPerson)
            Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show()
        }
        binding.btnKill.setOnClickListener{
            mContext.killPerson(mPerson.id)
            mCallback.update(3)
        }
        binding.btnClan.setOnClickListener {
            if(mPerson.specIdentity > 0)
                return@setOnClickListener
            CultivationHelper.abdicateInClan(mPerson, mContext.mClans, mContext.mPersons)
            Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
            mCallback.update(3)
            updateView()
        }
        binding.btnLife.setOnClickListener {
            val success = mContext.addPersonLifetime(mPerson.id)
            if(success){
                Toast.makeText(this.context, "成功", Toast.LENGTH_SHORT).show()
                updateView()
            }
        }
        binding.btnAssign.setOnClickListener {
            val partner = mContext.mPersons.map { it.value }.find { it.name == binding.etName.text.toString() || PinyinUtil.convert(it.name) == binding.etName.text.toString() }
            if(partner != null){
                mPerson.partner = partner.id
                mPerson.partnerName = partner.name
                if(partner.partner == null){
                    partner.partner = mPerson.id
                    partner.partnerName = mPerson.name
                }
                CultivationHelper.addPersonEvent(partner, "与${mPerson.name}\u7ed3\u4f34")
                CultivationHelper.addPersonEvent(mPerson, "与${partner.name}\u7ed3\u4f34")
                CultivationHelper.writeHistory("${CultivationHelper.getPersonBasicString(partner)} 与 ${CultivationHelper.getPersonBasicString(mPerson)} \u7ed3\u4f34了")
                updateView()
            }
        }
        binding.btnProfile.setOnClickListener {
            mPerson.profile = binding.etProfile.text.toString().toInt()
            mCallback.update(2)
            Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
        }
        binding.btnProps.setOnClickListener {
            val alliance = mContext.mAlliance[mPerson.allianceId]
            CultivationHelper.updatePersonInborn(mPerson, binding.etTianfu.text.toString().toInt(), binding.etLinggen.text.toString().toInt(), alliance)
            mCallback.update(1)
            updateView()
            Toast.makeText(context, "重置成功", Toast.LENGTH_SHORT).show()
        }
        binding.btnName.setOnClickListener {
            val first = binding.etNameFirst.text.toString()
            val second  = binding.etNameLast.text.toString()
            mPerson.name = first + second
            mPerson.lastName = first
            changedNameLoopHandler(mPerson)
            if (mContext.mClans[mPerson.ancestorId] != null && mPerson.ancestorId == mPerson.id){
                val clan = mContext.mClans[mPerson.ancestorId!!]!!
                clan.name = first
                clan.clanPersonList.forEach { (_: String, u: Person) ->
                    val ming = u.name.substring(u.lastName.length)
                    u.name = first + ming
                    u.lastName = first
                    changedNameLoopHandler(u)
                }
            }
            mCallback.update(3)
            Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
        }
        binding.btnBe.setOnClickListener {
            val partner = mContext.getOnlinePersonDetail(mPerson.partner)
            mPerson.partner = null
            mPerson.partnerName = null
            if(partner?.partner == mPerson.id){
                partner.partner = null
                partner.partnerName = null
            }
            updateView()
            Toast.makeText(context, "BE成功", Toast.LENGTH_SHORT).show()
        }
        binding.schSingled.setOnCheckedChangeListener { _, isChecked ->
            mPerson.singled = isChecked
        }
        binding.schDink.setOnCheckedChangeListener { _, isChecked ->
            mPerson.dink = isChecked
        }
        binding.schNeverDead.setOnCheckedChangeListener { _, isChecked ->
            mPerson.neverDead = isChecked
        }
    }

    private fun changedNameLoopHandler(person: Person){
        mContext.mPersons.forEach { (_: String, u: Person) ->
            if(u.partner == person.id){
                u.partnerName = person.name
            }
            if(u.parent?.first == person.id){
                u.parentName = Pair(person.name, u.parentName!!.second)
            }
            if(u.parent?.second == person.id){
                u.parentName = Pair(u.parentName!!.first, person.name)
            }
        }
    }

    fun init(){
        val id = requireArguments().getString("id", "")
        mPerson = mContext.getPersonData(id)!!
        initSkinSpinner()
        binding.schSingled.isChecked = mPerson.singled
        binding.schDink.isChecked = mPerson.dink
        binding.schNeverDead.isChecked =  mPerson.neverDead
        binding.etProfile.setText(mPerson.profile.toString())
        if(mPerson.specIdentity > 0){
            val persons =  CultivationSetting.getAllSpecPersons()
            val spec = persons.find { it.identity == mPerson.specIdentity }
            binding.etTianfu.setText(spec?.tianfuWeight.toString())
            binding.etLinggen.setText(spec?.linggenWeight.toString())
        }
        updateView()
    }

    private fun updateView(){
        if (mPerson.gender == NameUtil.Gender.Female && mPerson.partner == null){
            binding.btnAssign.visibility = View.VISIBLE
            binding.etName.isEnabled = true
        }else{
            binding.btnAssign.visibility = View.GONE
            binding.etName.isEnabled = false
        }
        binding.etName.setText(CultivationHelper.showing(mPerson.partnerName ?: ""))

        if (mPerson.partner == null){
            binding.btnBe.visibility = View.GONE
        }else{
            binding.btnBe.visibility = View.VISIBLE
        }

        binding.etNameFirst.setText(CultivationHelper.showing(mPerson.lastName))
        binding.etNameLast.setText(CultivationHelper.showing(mPerson.name.substring(mPerson.lastName.length)))

        binding.tvAge.text = CultivationHelper.showAge(mPerson)
        binding.tvAncestor.text = "${mPerson.ancestorOrignId}/${mPerson.ancestorOrignLevel}-${mPerson.ancestorId}/${mPerson.ancestorLevel}"
    }

    private fun initSkinSpinner(){
        mSkinList.clear()
        mSkinList.addAll(CultivationHelper.getSkinList(mPerson))
        mSkinList.add(0, Skin("", "默认"))
        mSkinList.sortBy { it.rarity }
        var index = 0
        if (mSkinList.size > 1 && mPerson.skin != ""){
            mSkinList.forEachIndexed { i, skin ->
                if (skin.id == CultivationHelper.getSkinObject(mPerson.skin)?.id ) {
                    index = i
                }
            }
        }
        val adapter = SpinnerCommonAdapter(mContext, mSkinList.map {
            val info = SpinnerInfo()
            info.name =  "<font color=\"${CultivationSetting.CommonColors[it.rarity]}\">${it.name}</font>(${it.description.replace("?", CultivationSetting.TEMP_SKIN_BATTLE_MIN.toString(), false)})-(${it.property.take(6).joinToString()})"
            info
        }, false, 12)
        binding.spinnerSkin.adapter = adapter
        binding.spinnerSkin.setSelection(index)
    }
}