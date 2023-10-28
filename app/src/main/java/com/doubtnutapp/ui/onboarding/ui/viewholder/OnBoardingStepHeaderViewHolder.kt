package com.doubtnutapp.ui.onboarding.ui.viewholder

import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnBoardingLanguageButtonPressed
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStep
import com.doubtnutapp.databinding.ItemOnboardingHeaderBinding
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import javax.inject.Inject

/**
 * Created by Sachin Saxena on
 * 12, Feb, 2020
 **/
class OnBoardingStepHeaderViewHolder(val binding: ItemOnboardingHeaderBinding) :
    BaseViewHolder<ApiOnBoardingStep>(binding.root) {

    @Inject
    lateinit var mUserPreference: UserPreference

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun bind(data: ApiOnBoardingStep) {
        if (data.image?.isNotEmpty() == true) {
            binding.userImage.show()
            binding.userImage.loadImage(data.image)
        } else {
            binding.userImage.hide()
        }

        if (!data.title.isNullOrEmpty()) {
            binding.userName.show()
            binding.userName.text = data.title
        } else {
            binding.userName.hide()
        }

        binding.buttonLanguage.apply {
            show()
            text = mUserPreference.getSelectedDisplayLanguage()
            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(OnBoardingLanguageButtonPressed)
            }
        }

        binding.welcomeMessage.text = data.message
    }
}