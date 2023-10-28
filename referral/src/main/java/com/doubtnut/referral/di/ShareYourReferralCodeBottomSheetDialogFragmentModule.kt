package com.doubtnut.referral.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.referral.ui.ShareYourReferralCodeBottomSheetDialogFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ShareYourReferralCodeBottomSheetDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShareYourReferralCodeBottomSheetDialogFragmentVM::class)
    abstract fun bindViewModel(viewModel: ShareYourReferralCodeBottomSheetDialogFragmentVM): ViewModel

}