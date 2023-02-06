package com.mx.gillustrated.dialog

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.mx.gillustrated.R
import com.mx.gillustrated.activity.CultivationActivity
import com.mx.gillustrated.activity.MainActivity
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.util.NameUtil
import java.io.File

class FragmentDialogImage constructor(private val mId:String, private val mGender: NameUtil.Gender )  : DialogFragment() {

    companion object{
        fun newInstance(id:String, gender: NameUtil.Gender = NameUtil.Gender.Default): FragmentDialogImage {
            return FragmentDialogImage(id, gender)
        }
    }


    @BindView(R.id.iv_profile)
    lateinit var mImage:ImageView



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val v = inflater.inflate(R.layout.fragment_dialog_image, container, false)
        ButterKnife.bind(this, v)
        init()
        return v
    }

    fun init(){
        try {
            val imageDir = File(Environment.getExternalStorageDirectory(),
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + mGender)
            var file = File(imageDir.path, "$mId.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$mId.jpg")
            }
            if (file.exists()) {
                val bmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, Uri.fromFile(file))
                mImage.setImageBitmap(bmp)
            } else
                mImage.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}