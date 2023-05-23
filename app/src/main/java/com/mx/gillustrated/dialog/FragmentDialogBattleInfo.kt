package com.mx.gillustrated.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.adapter.CultivationBattleAdapter
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.component.CultivationBattleHelper
import com.mx.gillustrated.component.CultivationBattleHelper.BattleObject
import com.mx.gillustrated.component.CultivationHelper.showing
import com.mx.gillustrated.databinding.FragmentDialogBattleinfoBinding
import com.mx.gillustrated.util.NameUtil
import com.mx.gillustrated.vo.cultivation.BattleInfo
import com.mx.gillustrated.vo.cultivation.Person
import java.io.File


@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("SetTextI18n")
class FragmentDialogBattleInfo  : DialogFragment() {

    companion object{
        fun newInstance(): FragmentDialogBattleInfo {
            return FragmentDialogBattleInfo()
        }
    }

    private var _binding: FragmentDialogBattleinfoBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mBattle:BattleInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentDialogBattleinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = this.requireArguments().getString("id", "")
        mBattle = CultivationBattleHelper.mBattles[id]!!
        mBattle.details.forEach {
            it.winner = mBattle.winnerName
            it.looser = mBattle.looserName
        }
        binding.lvInBattle.adapter = CultivationBattleAdapter(this.requireContext(), mBattle.details)
        showTitle()
        initListener()
    }

    private fun initListener(){
        binding.btnClose.setOnClickListener {
            this.dismiss()
        }
        binding.ivProfile1.setOnClickListener {
            if(mBattle.attacker == null)
                return@setOnClickListener
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPerson.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", mBattle.attacker!!.id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_person_info")
        }
        binding.ivProfile2.setOnClickListener {
            if(mBattle.defender == null)
                return@setOnClickListener
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            val newFragment = FragmentDialogPerson.newInstance()
            newFragment.isCancelable = false
            val bundle = Bundle()
            bundle.putString("id", mBattle.defender!!.id)
            newFragment.arguments = bundle
            newFragment.show(ft, "dialog_person_info")
        }
    }

    private fun showTitle(){
        setProfileLeft(mBattle.attacker, mBattle.attackerValue)
        setProfileRight(mBattle.defender, mBattle.defenderValue)
    }

    private fun setProfileLeft(person: Person?, battleValue:BattleObject){
        val spannable = SpannableString(showing(battleValue.name)+"(${battleValue.follower.size})")
        if(battleValue.name == mBattle.winnerName){
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#108A5E")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }else{
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#BF7C92")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvName1.setText(spannable, TextView.BufferType.SPANNABLE)
        binding.tvHp1.text = "${battleValue.hpBasis}→${battleValue.hp}"
        binding.tvAttack1.text = "${battleValue.attackInit}→${battleValue.attack}"
        binding.tvDefence1.text = "${battleValue.defenceInit}→${battleValue.defence}"
        binding.tvSpeed1.text = "${battleValue.speedInit}→${battleValue.speed}"

        if(person == null)
            return
        setProfileImage(person, binding.ivProfile1 )
    }

    private fun setProfileRight(person: Person?, battleValue:BattleObject){
        val spannable = SpannableString(showing(battleValue.name)+"(${battleValue.follower.size})")
        if(battleValue.name == mBattle.winnerName){
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#108A5E")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }else{
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#BF7C92")), 0,  showing(battleValue.name).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvName2.setText(spannable, TextView.BufferType.SPANNABLE)
        binding.tvHp2.text = "${battleValue.hpBasis}→${battleValue.hp}"
        binding.tvAttack2.text = "${battleValue.attackInit}→${battleValue.attack}"
        binding.tvDefence2.text = "${battleValue.defenceInit}→${battleValue.defence}"
        binding.tvSpeed2.text = "${battleValue.speedInit}→${battleValue.speed}"

        if(person == null)
            return
        setProfileImage(person, binding.ivProfile2 )
    }

    private fun setProfileImage(person: Person, imageView: ImageView){
        var profile = person.profile
        if(person.gender == NameUtil.Gender.Female && person.profile in 1701..1799){
            profile = 0
        }
        try {
            val imageDir = requireActivity().getExternalFilesDir(
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + person.gender) ?: return
            var file = File(imageDir.path,  "$profile.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$profile.jpg")
            }
            if (file.exists()) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, Uri.fromFile(file))
                imageView.setImageBitmap(ImageDecoder.decodeBitmap(source))
            } else
                imageView.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}