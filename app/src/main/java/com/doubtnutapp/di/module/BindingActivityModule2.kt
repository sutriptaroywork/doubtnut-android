package com.doubtnutapp.di.module

import com.doubtnut.core.di.scope.PerActivity
import com.doubtnut.referral.di.ReferralActivityV2Module
import com.doubtnut.referral.ui.ReferralActivityV2
import com.doubtnutapp.bottomsheetholder.BottomSheetHolderActivity
import com.doubtnutapp.dummy.DummyModule
import com.doubtnutapp.icons.di.IconsActivityModule
import com.doubtnutapp.icons.ui.IconsActivity
import com.doubtnutapp.login.di.LoginActivityModule
import com.doubtnutapp.login.di.LoginActivityProviderModule
import com.doubtnutapp.login.ui.activity.FailedGuestLoginActivity
import com.doubtnutapp.matchquestion.ui.activity.FullImageViewActivity
import com.doubtnutapp.ui.browser.WebViewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingActivityModule2 {

    @ContributesAndroidInjector(modules = [IconsActivityModule::class])
    @PerActivity
    internal abstract fun contributeIconsActivity(): IconsActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeWebViewActivity(): WebViewActivity

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerActivity
    internal abstract fun contributeFailedGuestLoginActivity(): FailedGuestLoginActivity

    @ContributesAndroidInjector(modules = [ReferralActivityV2Module::class])
    @PerActivity
    internal abstract fun contributeReferralActivityV2(): ReferralActivityV2

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeFullImageViewActivity(): FullImageViewActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeBottomSheetHolderActivity(): BottomSheetHolderActivity
}
