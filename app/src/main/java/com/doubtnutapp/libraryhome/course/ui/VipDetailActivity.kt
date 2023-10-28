package com.doubtnutapp.libraryhome.course.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.sticker.BaseActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class VipDetailActivity : BaseActivity(), HasAndroidInjector {

    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, VipDetailActivity::class.java)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.bunker)
        setContentView(R.layout.activity_vip_detail)
        startVmcDetailFragment()
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.COURSE_WHY_VIP_PAGE_OPEN))
    }

    private fun startVmcDetailFragment() {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager!!.beginTransaction()
        fragmentTransaction.add(R.id.fragmentContainer, VipClassesDetailFragment.newInstance())
        fragmentTransaction.commitAllowingStateLoss()
    }

}