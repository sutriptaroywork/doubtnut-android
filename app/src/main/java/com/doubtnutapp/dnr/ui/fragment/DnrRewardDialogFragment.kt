package com.doubtnutapp.dnr.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnutapp.R
import com.doubtnutapp.databinding.DialogDnrRewardBinding
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnut.core.utils.viewModelProvider
import dagger.Lazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class DnrRewardDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, DialogDnrRewardBinding>() {

    companion object {
        const val TAG = "DnrRewardDialogFragment"
        private const val ARG_DNR_REWARD = "dnr_reward"
        fun newInstance(dnrReward: DnrReward): DnrRewardDialogFragment =
            DnrRewardDialogFragment().apply {
                arguments = bundleOf(
                    ARG_DNR_REWARD to dnrReward
                )
            }
    }

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    private val dnrReward: DnrReward? by lazy { arguments?.getParcelable(ARG_DNR_REWARD) }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDnrRewardBinding =
        DialogDnrRewardBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        mBinding?.apply {
            val reward = dnrReward ?: return@apply

            // Increment the counter to count the no of dialog shown
            lifecycleScope.launchWhenStarted {
                val key = intPreferencesKey("dnr_" + reward.type + "_dialog")
                val count =
                    defaultDataStore.get().getNoOfTimesDnrRewardDialogShown(reward.type).firstOrNull()
                        ?: 0
                defaultDataStore.get().set(key = key, value = count + 1)
            }

            ivRewardFor.isVisible = reward.image.isNotNullAndNotEmpty()
            ivRewardFor.loadImage(reward.image)
            tvTitle.text = reward.dialogTitle
            reward.duration?.let {
                dismissWithDelay(it)
            }
        }
    }

    private fun dismissWithDelay(delay: Long) {
        lifecycleScope.launchWhenStarted {
            delay(delay)
            dismiss()
        }
    }
}
