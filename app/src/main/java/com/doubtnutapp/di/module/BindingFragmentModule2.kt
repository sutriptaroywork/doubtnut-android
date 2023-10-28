package com.doubtnutapp.di.module

import com.doubtnut.core.di.scope.PerFragment
import com.doubtnut.referral.di.ReferAndEarnModule
import com.doubtnut.referral.di.ReferralHomeFragmentModule
import com.doubtnut.referral.di.ShareYourReferralCodeBottomSheetDialogFragmentModule
import com.doubtnut.referral.ui.ReferAndEarnFAQFragment
import com.doubtnut.referral.ui.ReferAndEarnHomeFragment
import com.doubtnut.referral.ui.ReferralHomeFragment
import com.doubtnut.referral.ui.ShareYourReferralCodeBottomSheetDialogFragment
import com.doubtnutapp.freeclasses.bottomsheets.FilterListBottomSheetDialogFragment
import com.doubtnutapp.freeclasses.module.FilterListBottomSheetDialogFragmentModule
import com.doubtnutapp.icons.di.IconsHomeFragmentModule
import com.doubtnutapp.icons.ui.IconsHomeFragment
import com.doubtnutapp.shorts.ShortsCategoryBottomSheet
import com.doubtnutapp.shorts.di.ShortsCategoryBottomSheetModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingFragmentModule2 {

    @ContributesAndroidInjector(modules = [IconsHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeIconsHomeFragment(): IconsHomeFragment

    @ContributesAndroidInjector(modules = [FilterListBottomSheetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFilterListBSDialogFragment(): FilterListBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ReferralHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeReferralHomeFragment(): ReferralHomeFragment

    @ContributesAndroidInjector(modules = [ShareYourReferralCodeBottomSheetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeShareYourReferralCodeBottomSheetDialogFragment(): ShareYourReferralCodeBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ReferAndEarnModule::class])
    @PerFragment
    internal abstract fun contributeReferAndEarnHomeFragment():ReferAndEarnHomeFragment

    @ContributesAndroidInjector(modules = [ReferAndEarnModule::class])
    @PerFragment
    internal abstract fun contributeReferAndEarnFAQFragment():ReferAndEarnFAQFragment

    @ContributesAndroidInjector(modules = [ShortsCategoryBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeShortsCategoryBottomSheet(): ShortsCategoryBottomSheet
}
