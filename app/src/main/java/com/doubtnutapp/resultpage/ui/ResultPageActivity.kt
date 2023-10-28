package com.doubtnutapp.resultpage.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.remote.Resource
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityResultPageBinding
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class ResultPageActivity : CoreBindingActivity<ResultPageVM, ActivityResultPageBinding>() {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    private var page: String? = null
    private var type: String? = null
    private var source: String? = null

    companion object {
        const val TAG = "RESULT_PAGE"
        private const val BOTTOM_DATA_EVENT_TAG = "explore_courses_widget"
        private const val EVENT_TAG = "result_page"
        const val PAGE = "page"
        const val TYPE = "type"
        const val SOURCE = "source"

        fun getStartIntent(
            context: Context,
            page: String,
            type: String,
            source: String
        ) =
            Intent(context, ResultPageActivity::class.java).apply {
                putExtra(PAGE, page)
                putExtra(TYPE, type)
                putExtra(SOURCE, source)
            }
    }

    private var viewTrackingBus: ViewTrackingBus? = null

    private val widgetLayoutAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = this@ResultPageActivity,
            source = TAG
        )
    }

    override fun provideViewBinding() = ActivityResultPageBinding.inflate(layoutInflater)

    override fun providePageName() = TAG

    override fun provideViewModel(): ResultPageVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    override fun setupView(savedInstanceState: Bundle?) {
        page = intent.getStringExtra(PAGE).orEmpty()
        type = intent.getStringExtra(TYPE).orEmpty()
        source = intent.getStringExtra(SOURCE).orEmpty()

        Log.e(TAG, type.toString())
        Log.e(TAG, source.toString())
        registerViewTracking()

        viewModel.getResultPageData(page, type, source)

        viewModel.pageData.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = VISIBLE
                }
                is Resource.Success -> {
                    val data = it.data
                    binding.apply {
                        progressBar.visibility = GONE
                        tvToolbarTitle.text = data?.title.orEmpty()
                        tvToolbarTitle.applyTextSize(data?.titleTextSize)
                        tvToolbarTitle.applyTextColor(data?.titleTextColor)

                        (rvWidgets.adapter as? IWidgetLayoutAdapter)?.setWidgets(
                            data?.widgets.orEmpty()
                        )

                        if (data?.bottomData != null) {
                            tvBottom.visibility = VISIBLE
                            tvBottom.text = data.bottomData.title
                            tvBottom.applyTextSize(data.bottomData.titleTextSize)
                            tvBottom.applyTextColor(data.bottomData.titleTextColor)
                            tvBottom.applyBackgroundColor(data.bottomData.bgColor)
                            tvBottom.setOnClickListener {
                                deeplinkAction.performAction(
                                    this@ResultPageActivity,
                                    data.bottomData.deeplink
                                )
                                analyticsPublisher.publishEvent(
                                    AnalyticsEvent(
                                        "${BOTTOM_DATA_EVENT_TAG}_${CoreEventConstants.CLICKED}",
                                        hashMapOf(
                                            CoreEventConstants.PARENT_SCREEN_NAME to TAG
                                        ), ignoreFacebook = true
                                    )
                                )
                            }
                        } else {
                            tvBottom.visibility = GONE
                        }
                    }

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                            hashMapOf(), ignoreFacebook = true
                        )
                    )

                }
                is Resource.Error -> {
                    binding.progressBar.visibility = GONE
                    Log.e(TAG, it.message.toString())
                    this.toast("Some Error Occurred")
                }
            }
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

    }

    private fun registerViewTracking() {
        viewTrackingBus = ViewTrackingBus({
            viewModel.trackView(it)
        }, {})
        widgetLayoutAdapter.registerViewTracking(viewTrackingBus!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewTrackingBus?.unsubscribe()
    }

}