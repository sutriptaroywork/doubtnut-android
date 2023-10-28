package com.doubtnutapp.studygroup.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentSgActionDialogBinding
import com.doubtnutapp.getScreenWidth
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.AndroidInjection
import kotlinx.android.parcel.Parcelize

class SgActionDialogFragment : DialogFragment() {

    companion object {

        const val TAG = "StudyGroupActionDialogFragment"

        private const val PARAM_KEY_UI_CONFIG = "PARAM_KEY_UI_CONFIG"

        fun newInstance(sgActionUiConfig: SgActionUiConfig) =
            SgActionDialogFragment().also {
                it.arguments = bundleOf(
                    PARAM_KEY_UI_CONFIG to sgActionUiConfig
                )
            }
    }

    private var sgActionUiConfig: SgActionUiConfig? = null

    private var sgActionListener: SgActionListener? = null

    private val binding by viewBinding(FragmentSgActionDialogBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(activity)
        super.onCreate(savedInstanceState)
        arguments?.let {
            sgActionUiConfig = it.getParcelable(PARAM_KEY_UI_CONFIG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_sg_action_dialog, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            (requireActivity().getScreenWidth() / 1.2).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        setUpUi()
    }

    private fun setUpUi() {
        sgActionUiConfig?.let {
            binding.tvTitle.text = it.title
            binding.tvSubtitle.text = it.subtitle
            binding.buttonYes.text = it.buttonYesTitle
            binding.buttonNo.text = it.buttonNoTitle
        }

        binding.buttonYes.setOnClickListener {
            sgActionListener?.onButtonYesClick()
        }

        binding.buttonNo.setOnClickListener {
            sgActionListener?.onButtonNoClick()
        }
    }

    fun setSgActionListener(sgActionListener: SgActionListener) {
        this.sgActionListener = sgActionListener
    }

    @Keep
    @Parcelize
    data class SgActionUiConfig(
        val title: String,
        val subtitle: String,
        val buttonYesTitle: String,
        val buttonNoTitle: String
    ) : Parcelable

    interface SgActionListener {
        fun onButtonYesClick()
        fun onButtonNoClick()
    }
}