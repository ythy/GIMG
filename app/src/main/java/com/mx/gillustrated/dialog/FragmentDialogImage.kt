package com.mx.gillustrated.dialog

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.mx.gillustrated.common.MConfig
import com.mx.gillustrated.databinding.FragmentDialogImageBinding
import com.mx.gillustrated.util.NameUtil
import java.io.File

class FragmentDialogImage constructor(private val mId:String, private val mGender: NameUtil.Gender )  : DialogFragment() {

    companion object{
        fun newInstance(id:String, gender: NameUtil.Gender = NameUtil.Gender.Default): FragmentDialogImage {
            return FragmentDialogImage(id, gender)
        }
    }

    private var _binding: FragmentDialogImageBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        _binding = FragmentDialogImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun init(){
        try {
            val imageDir = requireActivity().getExternalFilesDir(
                    MConfig.SD_CULTIVATION_HEADER_PATH + "/" + mGender) ?: return
            var file = File(imageDir.path, "$mId.png")
            if (!file.exists()) {
                file = File(imageDir.path, "$mId.jpg")
            }
            if (file.exists()) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, Uri.fromFile(file))
                binding.ivProfile.setImageBitmap(ImageDecoder.decodeBitmap(source))
            } else
                binding.ivProfile.setImageBitmap(null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}