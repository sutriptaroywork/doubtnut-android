package com.doubtnutapp.topicboostergame2.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentTopicBoosterGameMessageDialogBinding
import com.doubtnutapp.topicboostergame2.ui.adapter.DialogMessageAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class TbgMessageDialogFragment : DialogFragment(R.layout.fragment_topic_booster_game_message_dialog) {

    companion object {
        const val MESSAGE = "message"
    }

    private val binding by viewBinding(FragmentTopicBoosterGameMessageDialogBinding::bind)
    private val args by navArgs<TbgMessageDialogFragmentArgs>()

    private val messageAdapter by lazy { DialogMessageAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        with(binding){
            messageContainer.setOnClickListener {
                dialog?.cancel()
            }
            rvMessages.adapter = messageAdapter
            messageAdapter.updateList(args.messages.toList())

            ivClose.setOnClickListener {
                dialog?.cancel()
            }
        }
    }
}