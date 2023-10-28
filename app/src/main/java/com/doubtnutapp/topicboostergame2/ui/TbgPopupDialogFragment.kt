package com.doubtnutapp.topicboostergame2.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.setNavigationResult
import com.doubtnutapp.databinding.FragmentTopicBoosterGameInviteDialogBinding
import com.doubtnutapp.hide
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

/**
 * Created by devansh on 2/3/21.
 */

class TbgPopupDialogFragment : DialogFragment(R.layout.fragment_topic_booster_game_invite_dialog) {

    companion object {
        fun newInstance(args: Bundle): TbgPopupDialogFragment {
            return TbgPopupDialogFragment().apply {
                arguments = args
            }
        }
        const val TAG = "TbgPopupDialogFragment"
    }

    private val binding by viewBinding(FragmentTopicBoosterGameInviteDialogBinding::bind)
    private val args by navArgs<TbgPopupDialogFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val data = args.popupDialogData

        with(binding) {
            tvDescription.text = data.description

            if (!data.button1.isNullOrEmpty()) {
                button1.apply {
                    text = data.button1
                    setOnClickListener {
                        setNavigationResult(data.button1, data.button1.orEmpty())
                        dismiss()
                    }
                }
            } else {
                button1.hide()
            }

            if (!data.button2.isNullOrEmpty()) {
                button2.apply {
                    text = data.button2
                    setOnClickListener {
                        setNavigationResult(data.button2, data.button2.orEmpty())
                        dismiss()
                    }
                }
            } else {
                button2.hide()
            }

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }
}