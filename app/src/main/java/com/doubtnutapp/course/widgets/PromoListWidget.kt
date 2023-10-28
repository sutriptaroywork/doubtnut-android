package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.RecyclerViewUtils.addRvRequestDisallowInterceptTouchEventListener
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.PromoListWidgetModel
import com.doubtnutapp.data.remote.models.PromoWidgetData
import com.doubtnutapp.data.remote.models.PromoWidgetItem
import com.doubtnutapp.databinding.ItemPromoBinding
import com.doubtnutapp.databinding.WidgetPromoListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.BannerActionUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PromoListWidget(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PromoListWidget.WidgetHolder,
    PromoListWidgetModel, WidgetPromoListBinding>(context, attrs, defStyle) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    override fun getViewBinding(): WidgetPromoListBinding {
        return WidgetPromoListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    private var autoScrollJob: Job? = null
    private var promoWidgetItem: PromoWidgetData? = null

    var isAutoScrollWidgetInteractionDone = false

    override fun bindWidget(holder: WidgetHolder, model: PromoListWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: PromoWidgetData = model.data
        promoWidgetItem = data

        val binding = holder.binding

        if (model.layoutConfig == null) {
            if (data.margin == true) {
                setMargins(WidgetLayoutConfig(12, 12, 12, 12))
            } else {
                setMargins(WidgetLayoutConfig(0, 0, 0, 0))
            }
        }

        val params =
            binding.rvItems.layoutParams as ConstraintLayout.LayoutParams
        if (data.ratio.isNullOrBlank()) {
            params.dimensionRatio = "6:1"
        } else {
            params.dimensionRatio = data.ratio
        }

        if (data.items.isNullOrEmpty() || data.items.size == 1) {
            binding.circleIndicator.hide()
        } else {
            binding.circleIndicator.show()
        }

        binding.root.layoutParams?.apply {
            width = model.data.width?.dpToPx() ?: ViewGroup.LayoutParams.MATCH_PARENT
            height = model.data.height?.dpToPx() ?: ViewGroup.LayoutParams.WRAP_CONTENT
        }

        (context as? AppCompatActivity)?.let { appCompatActivity ->

            val snapHelper = PagerSnapHelper()

            binding.rvItems.onFlingListener = null
            binding.rvItems.removeItemDecorations()
            snapHelper.attachToRecyclerView(null)

            snapHelper.attachToRecyclerView(binding.rvItems)

            binding.rvItems.addOnItemTouchListener(object :
                    RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            isAutoScrollWidgetInteractionDone = true
                            autoScrollJob?.cancel()
                        }
                        return super.onInterceptTouchEvent(rv, e)
                    }
                })

            binding.rvItems.layoutParams = params
            val actionActivity = model.action?.actionActivity.orDefaultValue()
            binding.rvItems.addRvRequestDisallowInterceptTouchEventListener()
            binding.rvItems.adapter = Adapter(
                items = data.items,
                actionActivity = actionActivity,
                actionPerformer = actionPerformer,
                deeplinkAction = deeplinkAction,
                analyticsPublisher = analyticsPublisher,
                parentPosition = model.data.parentPosition ?: holder.bindingAdapterPosition,
                source = source,
                extraParams = model.extraParams
            )
            binding.circleIndicator.attachToRecyclerView(binding.rvItems)
            binding.rvItems.smoothScrollToPosition(model.data.selectedPagePosition)

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            binding.rvItems,
                            autoScrollTimeInSec,
                            model.data.items.lastIndex
                        )
                    }
                }
            }
            if (model.data.title.isNullOrEmpty()) {
                binding.tvTitle.visibility = View.GONE
            } else {
                binding.tvTitle.visibility = View.VISIBLE
                binding.tvTitle.text = model.data.title.orEmpty()
                binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
            }
        }
        return holder
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
            promoWidgetItem?.selectedPagePosition = scrollToPosition
        }
        autoRotate(recyclerView, delayInSec, lastIndex)
    }

    inner class WidgetHolder(binding: WidgetPromoListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPromoListBinding>(binding, widget) {

        override fun onViewDetachedFromWindow() {
            super.onViewDetachedFromWindow()
            autoScrollJob?.cancel()
        }
    }

    companion object {
        const val TAG = "PromoListWidget"
    }

    class Adapter(
        val items: List<PromoWidgetItem>,
        val actionActivity: String,
        val actionPerformer: ActionPerformer? = null,
        val deeplinkAction: DeeplinkAction,
        val analyticsPublisher: AnalyticsPublisher,
        val parentPosition: Int,
        val source: String?,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemPromoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val promoWidgetItem = items.getOrNull(position) ?: return
            val context = binding.root.context

            val eventName = EventConstants.PROMO_BANNER_VIEW

            binding.ivBanner.layoutParams?.apply {
                width = promoWidgetItem.width?.dpToPx() ?: ViewGroup.LayoutParams.MATCH_PARENT
                height = promoWidgetItem.height?.dpToPx() ?: ViewGroup.LayoutParams.MATCH_PARENT
            }

            binding.ivBanner.scaleType =
                ImageView.ScaleType.valueOf(promoWidgetItem.scaleType ?: "FIT_XY")

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    eventName,
                    hashMapOf<String, Any>(
                        EventConstants.EVENT_NAME_ID to promoWidgetItem.id.orEmpty(),
                        EventConstants.ITEM_PARENT_POSITION to parentPosition,
                        EventConstants.ITEM_POSITION to position,
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.SOURCE to source.orEmpty(),
                    ).apply {
                        putAll(extraParams.orEmpty())
                    }
                )
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            binding.ivBanner.loadImage(promoWidgetItem.imageUrl, null)
            binding.ivBanner.setOnClickListener {
                val actionData = promoWidgetItem.action?.actionData
                if (actionData != null && promoWidgetItem.action.actionActivity != null) {
                    BannerActionUtils.performAction(
                        context,
                        promoWidgetItem.action.actionActivity!!,
                        actionData
                    )
                } else {
                    deeplinkAction.performAction(context, promoWidgetItem.deeplink)
                }

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PROMO_BANNER_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.EVENT_NAME_ID to promoWidgetItem.id.orEmpty(),
                            EventConstants.ITEM_PARENT_POSITION to parentPosition,
                            EventConstants.ITEM_POSITION to position,
                            EventConstants.WIDGET to TAG,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(extraParams.orEmpty())
                        }
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemPromoBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
