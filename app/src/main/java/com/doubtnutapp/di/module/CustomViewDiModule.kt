package com.doubtnutapp.di.module

import com.doubtnut.referral.widgets.*
import com.doubtnut.scholarship.widget.*
import com.doubtnutapp.referral.ReferralVideoWidget
import com.doubtnutapp.widgets.TimerWidget
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Module used to bind custom Views.
 * Reference: https://github.com/google/dagger/issues/720#issuecomment-581868740
 */
@Module
internal abstract class CustomViewDiModule {

    //region Referral Module
    @ContributesAndroidInjector
    abstract fun bindReferralStepsWidget(): ReferralStepsWidget

    @ContributesAndroidInjector
    abstract fun bindReferralLevelWidget(): ReferralLevelWidget

    @ContributesAndroidInjector
    abstract fun bindImageTextWidget(): ImageTextWidget

    @ContributesAndroidInjector
    abstract fun bindReferralWinnerEarnWidget(): ReferralWinnerEarnWidget

    @ContributesAndroidInjector
    abstract fun bindReferralWinnerCongratulationsWidget(): ReferralWinnerCongratulationsWidget

    @ContributesAndroidInjector
    abstract fun bindReferralWinnerEarnV2Widget(): ReferralWinnerEarnWidgetV2

    @ContributesAndroidInjector
    abstract fun bindReferralVideoWidget(): ReferralVideoWidget

    @ContributesAndroidInjector
    abstract fun bindReferralClaimWidget(): ReferralClaimWidget

    @ContributesAndroidInjector
    abstract fun bindsReferAndEarnHeaderWidget(): ReferAndEarnHeaderWidget

    @ContributesAndroidInjector
    abstract fun bindsReferAndEarnStepsWidget(): ReferAndEarnStepsWidget

    @ContributesAndroidInjector
    abstract fun bindsReferralCodeWidget(): ReferralCodeWidget

    @ContributesAndroidInjector
    abstract fun bindsReferrredFriendsWidget(): ReferredFriendsWidget

    @ContributesAndroidInjector
    abstract fun bindsCouponAppliedWidget(): CouponAppliedWidget

    @ContributesAndroidInjector
    abstract fun bindsClipboardWidget(): ClipboardWidget
    //endregion

    //region Scholarship Module
    @ContributesAndroidInjector
    abstract fun bindsAwardedStudentsWidget(): AwardedStudentsWidget

    @ContributesAndroidInjector
    abstract fun bindsReferralWidget(): ReferralWidget

    @ContributesAndroidInjector
    abstract fun bindsPreviousTestResultsWidget(): PreviousTestResultsWidget

    @ContributesAndroidInjector
    abstract fun bindsPracticeTestWidget(): PracticeTestWidget

    @ContributesAndroidInjector
    abstract fun bindsScholarshipTabsWidget(): ScholarshipTabsWidget

    @ContributesAndroidInjector
    abstract fun bindsReportCardWidget(): ReportCardWidget

    @ContributesAndroidInjector
    abstract fun bindsRegisterTestWidget(): RegisterTestWidget

    @ContributesAndroidInjector
    abstract fun bindsWidgetProgress(): WidgetProgress

    @ContributesAndroidInjector
    abstract fun bindsScholarshipProgressCardWidget(): ScholarshipProgressCardWidget
    //endregion

    //region Common Module
    @ContributesAndroidInjector
    abstract fun bindsTimerWidget(): TimerWidget
    //endregion
}