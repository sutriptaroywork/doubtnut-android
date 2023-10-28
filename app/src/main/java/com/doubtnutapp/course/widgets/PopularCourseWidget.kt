package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ClickOnWidgetAction
import com.doubtnutapp.databinding.PopularCourseWidgetItemBinding
import com.doubtnutapp.databinding.WidgetPopularCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.PopularCourseWidgetData
import com.doubtnutapp.domain.videoPage.entities.PopularCourseWidgetItem
import com.doubtnutapp.home.recyclerdecorator.HorizontalSpaceItemDecoration
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Akshat Jindal 12/4/21
 */

class PopularCourseWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PopularCourseWidget.WidgetHolder, PopularCourseWidgetModel,
        WidgetPopularCourseBinding>(context, attrs, defStyle) {

    companion object {
        const val TAG = "PopularCourseWidget"

        var isClicked = false
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var autoScrollJob: Job? = null
    private var model: PopularCourseWidgetModel? = null

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    var isAutoScrollWidgetInteractionDone = false

    override fun getViewBinding(): WidgetPopularCourseBinding {
        return WidgetPopularCourseBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    private fun isMPVP() = source == VideoPageActivity.TAG || source == SimilarVideoFragment.TAG

    override fun bindWidget(holder: WidgetHolder, model: PopularCourseWidgetModel): WidgetHolder {

        super.bindWidget(holder, model)

        val binding = holder.binding

        val data: PopularCourseWidgetData = model.data
        this.model = model

        val eventName = when {
            isMPVP() -> {
                EventConstants.MPVP_COURSE_BANNER_SHOWN
            }
            else -> {
                EventConstants.POPULAR_COURSE_VIEW
            }
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name = eventName,
                params = hashMapOf<String, Any>(
                    EventConstants.WIDGET to TAG,
                    EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.SOURCE to source.orEmpty(),
                )
                    .apply {
                        putAll(model.extraParams ?: hashMapOf())
                        put(EventConstants.FLAG_ID, model.data.flagrId.orEmpty())
                        put(EventConstants.VARIANT_ID, model.data.variantId.orEmpty())
                    }
            )
        )

        if (!data.backgroundColor.isNullOrEmpty()) {
            binding.parentLayout.setBackgroundColor(Color.parseColor(data.backgroundColor))
        }

        if (model.data.addExtraSpacing == true) {
            binding.tvTitle.updateMargins(top = 20.dpToPx())
        } else {
            binding.tvTitle.updateMargins(top = 5.dpToPx())
        }

        binding.tvTitle.text = data.title.orEmpty()
        binding.tvTitle.setVisibleState(!data.title.isNullOrEmpty())

        binding.recyclerView.adapter = Adapter(
            items = data.items.orEmpty(),
            deeplinkAction = deeplinkAction,
            analyticsPublisher = analyticsPublisher,
            extraParams = model.extraParams ?: HashMap(),
            title = model.data.title,
            source = source,
            isMpvp = isMPVP(),
            flagrId = model.data.flagrId,
            variantId = model.data.variantId,
            actionPerformer = actionPerformer,
            callImpressionApi = model.data.callImpressionApi
        )

        binding.btnMore.text = model.data.moreText
        binding.btnMore.isVisible = model.data.moreText.isNullOrEmpty().not()
        binding.btnMore.setOnClickListener {
            deeplinkAction.performAction(context, model.data.moreDeepLink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = "${TAG}_${EventConstants.MORE_CLICKED}",
                    params = hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.SOURCE to source.orEmpty(),
                    )
                        .apply {
                            putAll(model.extraParams ?: hashMapOf())
                            put(EventConstants.FLAG_ID, model.data.flagrId.orEmpty())
                            put(EventConstants.VARIANT_ID, model.data.variantId.orEmpty())
                        }
                )
            )
        }

        val snapHelper = PagerSnapHelper()

        binding.recyclerView.onFlingListener = null
        binding.recyclerView.removeItemDecorations()
        snapHelper.attachToRecyclerView(null)

        snapHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addItemDecoration(
            HorizontalSpaceItemDecoration(8.dpToPx())
        )

        (context as? AppCompatActivity)?.let { appCompatActivity ->
            binding.recyclerView.addOnItemTouchListener(object :
                RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isAutoScrollWidgetInteractionDone = true
                        autoScrollJob?.cancel()
                    }
                    return super.onInterceptTouchEvent(rv, e)
                }
            })

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            binding.recyclerView,
                            autoScrollTimeInSec,
                            model.data.items?.lastIndex ?: return@launchWhenResumed
                        )
                    }
                }
            }
        }
        binding.recyclerView.smoothScrollToPosition(model.data.selectedPagePosition)
        return holder
    }

    fun onPageSelected(position: Int) {
        model?.data?.selectedPagePosition = position
        if (isMPVP()) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.MPVP_COURSE_BANNER_USER_SWIPE,
                    params = hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.SOURCE to source.orEmpty(),
                    )
                        .apply {
                            put(
                                EventConstants.FLAG_ID,
                                model?.data?.flagrId.orEmpty()
                            )
                            put(
                                EventConstants.VARIANT_ID,
                                model?.data?.variantId.orEmpty()
                            )
                            put(EventConstants.STUDENT_ID, UserUtil.getStudentId())
                            putAll(model?.extraParams.orEmpty())
                        }
                )
            )
        }
    }

    private suspend fun autoRotate(
        recyclerView: RecyclerView,
        delayInSec: Long,
        lastIndex: Int
    ) {
        delay(TimeUnit.SECONDS.toMillis(delayInSec))
        if (isAutoScrollWidgetInteractionDone) {
            isAutoScrollWidgetInteractionDone = false
        } else {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val scrollToPosition =
                if (visibleItemPosition == lastIndex) 0 else visibleItemPosition + 1
            recyclerView.smoothScrollToPosition(scrollToPosition)
            onPageSelected(scrollToPosition)
        }
        autoRotate(recyclerView, delayInSec, lastIndex)
    }

    inner class WidgetHolder(binding: WidgetPopularCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPopularCourseBinding>(binding, widget) {

        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
            autoScrollJob?.cancel()
        }
    }

    class Adapter(
        private val items: List<PopularCourseWidgetItem>,
        private val deeplinkAction: DeeplinkAction,
        private val analyticsPublisher: AnalyticsPublisher,
        private val extraParams: HashMap<String, Any>,
        private val title: String?,
        private val source: String?,
        private val isMpvp: Boolean,
        private val flagrId: String?,
        private val variantId: String?,
        private val actionPerformer: ActionPerformer? = null,
        private val callImpressionApi: Boolean? = false
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        var mLastClickTime: Long = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                PopularCourseWidgetItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            val context = binding.root.context

            Utils.setWidthBasedOnPercentage(
                context,
                binding.root,
                "1.1",
                R.dimen.spacing_5
            )

            if (data.bannerType == 0) {
                binding.layoutOther.visibility = GONE
                binding.layoutPre.visibility = VISIBLE
                binding.ivBackgroundPre.loadImage(data.imageUrl.orEmpty())
                binding.ivBackgroundPre.setOnClickListener {
                    handleBannerClick(context, data)
                }
            } else {
                binding.layoutOther.visibility = VISIBLE
                binding.layoutPre.visibility = GONE
                binding.ivBackground.loadImage(data.imageUrl.orEmpty())
                binding.ivBackground.setOnClickListener {
                    handleBannerClick(context, data)
                }
            }

            when (data.bannerType) {
                0 -> {
                    // pre purchase
                    if (!data.price.isNullOrEmpty()) {
                        binding.tvPricePre.setVisibleState(true)
                        binding.tvPricePre.text = data.price.orEmpty()
                        binding.tvPricePre.setTextColor(Color.parseColor(data.priceColor.orEmpty()))
                        binding.tvPricePre.setOnClickListener {
                            handleButtonClick(context, data)
                        }
                    } else {
                        binding.tvPricePre.setVisibleState(false)
                    }

                    if (!data.crossedPrice.isNullOrEmpty()) {
                        binding.tvPriceCrossedPre.setVisibleState(true)
                        binding.tvPriceCrossedPre.text = data.crossedPrice.orEmpty()
                        binding.tvPriceCrossedPre.setTextColor(Color.parseColor(data.crossedPriceColor.orEmpty()))
                        binding.tvPriceCrossedPre.setOnClickListener {
                            handleButtonClick(context, data)
                        }
                    } else {
                        binding.tvPriceCrossedPre.setVisibleState(false)
                    }

                    if (data.crossedPrice.toString().trim().isEmpty()) {
                        binding.crossViewPre.setVisibleState(false)
                    } else {
                        binding.crossViewPre.setVisibleState(true)
                        binding.crossViewPre.setBackgroundColor(Color.parseColor(data.crossColor.orEmpty()))
                    }

                    if (!data.buttonText.isNullOrEmpty()) {
                        binding.btnCourseNotifyPre.setVisibleState(true)
                        binding.btnCourseNotifyPre.background = Utils.getShape(
                            colorString = data.buttonBackgroundColor.orEmpty(),
                            strokeColor = "00FFFFFF",
                            cornerRadius = 8f,
                            strokeWidth = 0
                        )
                        binding.btnCourseNotifyPre.text = data.buttonText.orEmpty()
                        binding.btnCourseNotifyPre.setTextColor(Color.parseColor(data.buttonTextColor.orEmpty()))
                        binding.btnCourseNotifyPre.setOnClickListener {
                            handleButtonClick(context, data)
                        }
                    } else {
                        binding.btnCourseNotifyPre.setVisibleState(false)
                    }

                }
                1 -> {
                    // trial

                    binding.apply {
                        tvStart.setVisibleState(false)
                        tvPrice.setVisibleState(false)
                        tvPriceCrossed.setVisibleState(false)
                        crossView.setVisibleState(false)
                        tvSyllabus.setVisibleState(false)
                        btnCourseNotify.setVisibleState(false)

                        tvTrial.setVisibleState(true)
                        ivTrial.setVisibleState(true)
                        btnCourseNotify2.setVisibleState(true)
                    }

                    binding.tvTrial.text = data.bannerText.orEmpty()
                    binding.ivTrial.loadImageEtx(data.trialImageUrl.orEmpty())

                    binding.tvTrial.setOnClickListener {
                        handleButtonClick(context, data)
                    }

                    binding.ivTrial.setOnClickListener {
                        handleButtonClick(context, data)
                    }

                    binding.btnCourseNotify2.text = data.buttonText.orEmpty()
                    binding.btnCourseNotify2.setTextColor(Color.parseColor(data.buttonTextColor.orEmpty()))
                    binding.btnCourseNotify2.setBackgroundColor(Color.parseColor(data.buttonBackgroundColor.orEmpty()))
                    binding.btnCourseNotify2.setOnClickListener {
                        handleButtonClick(context, data)
                    }
                }
                2 -> {
                    // post purchase

                    binding.apply {
                        tvStart.setVisibleState(false)
                        tvPrice.setVisibleState(false)
                        tvPriceCrossed.setVisibleState(false)
                        crossView.setVisibleState(false)
                        tvSyllabus.setVisibleState(false)

                        tvTrial.setVisibleState(true)
                        ivTrial.setVisibleState(false)
                        btnCourseNotify2.setVisibleState(true)
                    }

                    binding.tvTrial.text = data.bannerText.orEmpty()

                    binding.tvTrial.setOnClickListener {
                        handleButtonClick(context, data)
                    }

                    binding.btnCourseNotify2.text = data.buttonText.orEmpty()
                    binding.btnCourseNotify2.setTextColor(Color.parseColor(data.buttonTextColor.orEmpty()))
                    binding.btnCourseNotify2.setBackgroundColor(Color.parseColor(data.buttonBackgroundColor.orEmpty()))
                    binding.btnCourseNotify2.setOnClickListener {
                        handleButtonClick(context, data)
                    }
                }
                else -> {
                    binding.apply {
                        tvStart.setVisibleState(false)
                        tvPrice.setVisibleState(false)
                        tvPriceCrossed.setVisibleState(false)
                        crossView.setVisibleState(false)
                        tvSyllabus.setVisibleState(false)

                        tvTrial.setVisibleState(false)
                        ivTrial.setVisibleState(false)
                        btnCourseNotify2.setVisibleState(false)
                    }
                }
            }
        }

        override fun getItemCount(): Int = items.size

        private fun handleBannerClick(context: Context, data: PopularCourseWidgetItem) {
            isClicked = true

            if (System.currentTimeMillis() - mLastClickTime < 1000) return
            mLastClickTime = System.currentTimeMillis()

            if (isMpvp) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.MPVP_COURSE_BANNER_TAPPED,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.SOURCE to source.orEmpty(),
                        )
                            .apply {
                                putAll(extraParams)
                                putAll(data.extraParams ?: hashMapOf())
                            }
                    )
                )

                data.deeplinkBanner
                    ?.takeIf { it.isEmpty().not() }
                    ?.contains("course_details_bottom_sheet")
                    ?.not()?.let {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.MPVP_COURSE_BOTTOMSHEET_NOT_DISPLAYED,
                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                                    EventConstants.CLICKED_BUTTON_NAME to data.buttonText.toString(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                    EventConstants.SOURCE to source.orEmpty(),
                                )
                                    .apply {
                                        putAll(extraParams)
                                        putAll(data.extraParams ?: hashMapOf())
                                    }
                            )
                        )
                    }
            }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.POPULAR_COURSE_BANNER_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG,
                        EventConstants.WIDGET_TITLE to title.orEmpty(),
                        EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.SOURCE to source.orEmpty(),
                    )
                        .apply {
                            putAll(extraParams)
                            putAll(data.extraParams ?: hashMapOf())
                        }
                )
            )

            val bundle = Bundle().apply {
                putString(EventConstants.FLAG_ID, flagrId)
                putString(EventConstants.VARIANT_ID, variantId)
                putString(EventConstants.SOURCE, source)
            }
            deeplinkAction.performAction(context, data.deeplinkBanner.orEmpty(), bundle)
            if (callImpressionApi == true) {
                actionPerformer?.performAction(ClickOnWidgetAction())
            }
        }

        private fun handleButtonClick(context: Context, data: PopularCourseWidgetItem) {
            isClicked = true

            if (System.currentTimeMillis() - mLastClickTime < 1000) return
            mLastClickTime = System.currentTimeMillis()

            if (isMpvp) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.MPVP_COURSE_BANNER_TAPPED,
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                            EventConstants.CLICKED_BUTTON_NAME to data.buttonText.toString(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.SOURCE to source.orEmpty(),
                        )
                            .apply {
                                putAll(extraParams)
                                putAll(data.extraParams ?: hashMapOf())
                            }
                    )
                )

                data.deeplinkButton
                    ?.takeIf { it.isEmpty().not() }
                    ?.contains("course_details_bottom_sheet")
                    ?.not()?.let {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.MPVP_COURSE_BOTTOMSHEET_NOT_DISPLAYED,
                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to TAG,
                                    EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                                    EventConstants.CLICKED_BUTTON_NAME to data.buttonText.toString(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                    EventConstants.SOURCE to source.orEmpty(),
                                )
                                    .apply {
                                        putAll(extraParams)
                                        putAll(data.extraParams ?: hashMapOf())
                                    }
                            )
                        )
                    }
            }

            val event = AnalyticsEvent(
                EventConstants.POPULAR_COURSE_BUTTON_CLICK,
                hashMapOf<String, Any>(
                    EventConstants.WIDGET to TAG,
                    EventConstants.WIDGET_TITLE to title.orEmpty(),
                    EventConstants.ASSORTMENT_ID to data.assortmentId.toString(),
                    EventConstants.CLICKED_BUTTON_NAME to data.buttonText.toString(),
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.CTA_TEXT to data.buttonText.orEmpty(),
                    EventConstants.SOURCE to source.orEmpty(),
                ).apply {
                    putAll(extraParams)
                    putAll(data.extraParams ?: hashMapOf())
                }, ignoreMoengage = false
            )
            analyticsPublisher.publishEvent(event)

            val countToSendEvent: Int = Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.POPULAR_COURSE_BUTTON_CLICK
            )
            val eventCopy = event.copy()
            repeat((0 until countToSendEvent).count()) {
                analyticsPublisher.publishBranchIoEvent(eventCopy)
            }

            val bundle = Bundle().apply {
                putString(EventConstants.FLAG_ID, flagrId)
                putString(EventConstants.VARIANT_ID, variantId)
                putString(EventConstants.SOURCE, source)
            }
            deeplinkAction.performAction(context, data.deeplinkButton.orEmpty(), bundle)
        }

        class ViewHolder(val binding: PopularCourseWidgetItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}

class PopularCourseWidgetModel : WidgetEntityModel<PopularCourseWidgetData, WidgetAction>()
