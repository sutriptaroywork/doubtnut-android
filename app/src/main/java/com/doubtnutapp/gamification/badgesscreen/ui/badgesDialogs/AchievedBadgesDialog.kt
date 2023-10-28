package com.doubtnutapp.gamification.badgesscreen.ui.badgesDialogs

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OpenPage
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DialogAchivedBadgeItemBinding
import com.doubtnutapp.gamification.badgesscreen.model.BadgeProgress
import com.doubtnutapp.gamification.badgesscreen.ui.BadgesActivity
import com.doubtnutapp.gamification.badgesscreen.ui.viewmodel.BadgesViewModel
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.sharing.GAMIFICATION_CHANNEL
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils

/**
 * Screen displayed from Profile > Points > My Badges > Badge
 */
class AchievedBadgesDialog :
    BaseBindingDialogFragment<BadgesViewModel, DialogAchivedBadgeItemBinding>() {

    private var badgeId = ""
    private var description = ""
    private var imageUrl = ""
    private var featureType = ""
    private var sharingMessage = ""
    private var progressPercent: Int = 0
    private var actionPage: String = ""

    companion object {
        const val TAG = "AchievedBadgesDialog"
        fun newInstance(badgeId: String, nudeDescription: String, imageUrl: String, featureType: String, sharingUrl: String, actionPage: String): AchievedBadgesDialog =
                AchievedBadgesDialog().apply {
                    val bundle = Bundle()
                    bundle.putString(Constants.BADGE_ID, badgeId)
                    bundle.putString(Constants.NUDE_DESCRIPTION, nudeDescription)
                    bundle.putString(Constants.IMAGE_URL, imageUrl)
                    bundle.putString(Constants.FEATURE_TYPE, featureType)
                    bundle.putString(Constants.SHARING_MESSAGE, sharingUrl)
                    bundle.putString(Constants.ACTION_PAGE, actionPage)
                    arguments = bundle
                }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogAchivedBadgeItemBinding {
        return DialogAchivedBadgeItemBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): BadgesViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        extractIntentParams()
        isCancelable = false
        configureDialogActionsButtons(view)

        viewModel.getUserBadgeProgress(badgeId)
        updateUi()
    }

    private fun extractIntentParams() {
        arguments?.let {
            badgeId = it.getString(Constants.BADGE_ID, arguments?.getString(Constants.BADGE_ID))
            description = it.getString(
                Constants.NUDE_DESCRIPTION,
                arguments?.getString(Constants.NUDE_DESCRIPTION)
            )
            imageUrl = it.getString(Constants.IMAGE_URL, arguments?.getString(Constants.IMAGE_URL))
            featureType =
                it.getString(Constants.FEATURE_TYPE, arguments?.getString(Constants.FEATURE_TYPE))
            sharingMessage = it.getString(
                Constants.SHARING_MESSAGE,
                arguments?.getString(Constants.SHARING_MESSAGE)
            )
            actionPage =
                it.getString(Constants.ACTION_PAGE, arguments?.getString(Constants.ACTION_PAGE))
        }
    }

    private fun updateUi() {
        mBinding ?: return
        Glide.with(requireActivity()).load(imageUrl).into(mBinding?.dialogBadgeImage ?: return)
        mBinding?.badgeImageOverlay?.visibility =
            if (progressPercent == 100) View.GONE else View.VISIBLE
        mBinding?.dialogBadgeDescription?.text = description
        mBinding?.badgeProgressCountTextView?.text =
            getString(R.string.percent_text, progressPercent, "%")
        updateProgress(progressPercent)
        if (progressPercent == 100) {
            mBinding?.ctaButton?.text = getString(R.string.text_share_with_frnds)
            mBinding?.ctaButton?.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.whatsapp_login_color)
        } else {
            mBinding?.ctaButton?.text = getString(R.string.earn_this_badge)
            mBinding?.ctaButton?.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.redTomato)
        }
    }

    private fun configureDialogActionsButtons(view: View) {
        view.findViewById<Button>(R.id.ctaButton).setOnClickListener {
            if (this.progressPercent == 100) {
                (activity as BadgesActivity).performAction(
                    ShareOnWhatApp(
                        GAMIFICATION_CHANNEL,
                        featureType,
                        imageUrl,
                        null,
                        sharingMessage = sharingMessage,
                        questionId = ""

                    )
                )

            } else {
                (activity as BadgesActivity).performAction(OpenPage(actionPage))
            }
            dialog?.dismiss()
        }

        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.userBadgesProgressLiveData.observeK(
            viewLifecycleOwner,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressState
        )
    }

    private fun onSuccess(badgeProgress: BadgeProgress) {
        mBinding ?: return
        description = badgeProgress.description
        if (progressPercent == 100) {
            imageUrl = badgeProgress.imageUrl
        }
        if (badgeProgress.requirements != null && badgeProgress.requirements.isNotEmpty()) {
            this.progressPercent = badgeProgress.requirements[0].fullfilledPercent
        }
        updateUi()
    }

    private fun unAuthorizeUserError() {
        mBinding ?: return
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        mBinding ?: return
        apiErrorToast(e)
    }


    private fun ioExceptionHandler() {
        mBinding ?: return
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressState(state: Boolean) {
        mBinding ?: return
    }

    fun updateProgress(progressValue: Int) {
        val animation = ObjectAnimator.ofInt(mBinding?.badgeProgressbar, "progress", progressValue)
        animation.duration = 850
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    override fun onStart() {
        super.onStart()
        activity ?: return

        dialog?.window?.setLayout(
            requireActivity().getScreenWidth() - 32.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
