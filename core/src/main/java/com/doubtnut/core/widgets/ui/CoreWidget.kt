package com.doubtnut.core.widgets.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.forEach
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.DividerConfig
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.interfaces.WidgetFunctions
import javax.inject.Inject

abstract class CoreWidget<VH : CoreWidgetVH, WM : WidgetEntityModel<*, *>>
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), WidgetFunctions<VH, WM> {

    lateinit var widgetViewHolder: VH
    var widgetEntityModel: WidgetEntityModel<*, *>? = null

    /** Need to keep a different name from the child Analytics publishers.
     * Maybe this can be used instead of declaring analyticsPublisher in every widget subtype
     */
    @Inject
    lateinit var mAnalyticsPublisher: IAnalyticsPublisher

    var actionPerformer: ActionPerformer? = null

    var trackingViewId: String?
        set(value) {
            widgetViewHolder.trackingViewId = value
        }
        get() = widgetViewHolder.trackingViewId

    var trackingViewParams: HashMap<String, Any>?
        set(value) {
            widgetViewHolder.trackingViewParams = value
        }
        get() = widgetViewHolder.trackingViewParams

    init {
        setupViewHolder()
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun bindWidget(holder: VH, model: WM): VH {
        widgetEntityModel = model
        var viewHolder: VH? = holder

        if (viewHolder == null) {
            viewHolder = getViewHolder()
        }

        setDivider(model.dividerConfig)
        setMargins(model.layoutConfig, model.dividerConfig)

        return viewHolder
    }

    protected open fun setDivider(dividerConfig: DividerConfig?) {
        dividerConfig ?: return
        if (childCount != 0 && getChildAt(0) is View && getChildAt(0)?.tag == TAG_DIVIDER) {
            return
        }

        orientation = VERTICAL

        val divider = View(context).apply {
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, (dividerConfig.height ?: 4).dpToPx())
            tag = TAG_DIVIDER
        }
        divider.setMargins(
            left = dividerConfig.marginLeft?.dpToPx() ?: 0,
            top = dividerConfig.marginTop?.dpToPx() ?: 0,
            right = dividerConfig.marginRight?.dpToPx() ?: 0,
            bottom = dividerConfig.marginBottom?.dpToPx() ?: 0
        )
        divider.applyBackgroundColor(dividerConfig.backgroundColor ?: "#e4ecf1")
        addView(divider, 0)
    }

    protected open fun setMargins(
        widgetLayoutConfig: WidgetLayoutConfig?,
        dividerConfig: DividerConfig? = null
    ) {
        var marginTop = DEFAULT_MARGIN_TOP
        var marginLeft = 0
        var marginRight = 0
        var marginBottom: Int = DEFAULT_MARGIN_BOTTOM
        if (widgetLayoutConfig != null) {
            if (widgetLayoutConfig.marginTop >= 0) marginTop = widgetLayoutConfig.marginTop
            if (widgetLayoutConfig.marginLeft >= 0) marginLeft = widgetLayoutConfig.marginLeft
            if (widgetLayoutConfig.marginRight >= 0) marginRight = widgetLayoutConfig.marginRight
            if (widgetLayoutConfig.marginBottom >= 0) marginBottom = widgetLayoutConfig.marginBottom
        }

        // Prevent adding margin to divider.
        val view =
            if (dividerConfig?.skipMargin == true
                && childCount > 1
                && getChildAt(1) is View
                && getChildAt(0)?.tag == TAG_DIVIDER
            ) {
                getChildAt(1) ?: this
            } else {
                this
            }

        val params = view.layoutParams as MarginLayoutParams
        params.topMargin = ViewUtils.convertDpToPixel(marginTop.toFloat()).toInt()
        params.leftMargin = ViewUtils.convertDpToPixel(marginLeft.toFloat()).toInt()
        params.rightMargin = ViewUtils.convertDpToPixel(marginRight.toFloat()).toInt()
        params.bottomMargin = ViewUtils.convertDpToPixel(marginBottom.toFloat()).toInt()
    }

    fun getViewHolder(): VH {
        return this.widgetViewHolder
    }

    @Deprecated("Function will be removed in future as not required now.")
    protected abstract fun getView(): View



    protected abstract fun setupViewHolder()

    open fun performAction(action: Any) {
        this.actionPerformer?.performAction(action)
    }

    /**
     * Identify legitimate clicks on child views in the widget's [ViewGroup]
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        mTapDetector.onTouchEvent(ev)
        return false
    }

    private val mTapDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                checkViewGroupClicks(e, this@CoreWidget)
                return true
            }
        })

    private fun checkViewGroupClicks(ev: MotionEvent, viewGroup: ViewGroup): Boolean {
        viewGroup.forEach { view ->
            // check if this view would have handled a click
            if (verifyViewClick(ev, view)) {
                return true
            }
            // if ViewGroup, check all child views if they are going to handle a click
            if (view is ViewGroup) {
                checkViewGroupClicks(ev, view)
            }
        }
        return false
    }

    private fun verifyViewClick(ev: MotionEvent, view: View): Boolean {
        val hitRect = Rect()
        view.getHitRect(hitRect)
        // find the view for which this click was performed and then check if that view
        // had some clickListeners attached (which means they will handle some action)
        if (hitRect.contains(ev.x.toInt(), ev.y.toInt()) && view.hasOnClickListeners()) {
            // the child is going to do some action on this click, put some common logic here
            val params = hashMapOf<String, Any>()
            params.putAll(widgetEntityModel?.extraParams?.toMutableMap() ?: hashMapOf())
            params[CoreEventConstants.WIDGET_TYPE] = widgetEntityModel?.type.orEmpty()
            mAnalyticsPublisher.publishEvent(
                CoreAnalyticsEvent(
                    CoreEventConstants.EVENT_NAME_COMMON_WIDGET_CLICK,
                    params,
                    ignoreBranch = false
                )
            )
            return true
        }
        return false
    }

    fun publishEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        mAnalyticsPublisher.publishEvent(
            CoreAnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    class WidgetHolder(itemView: View) : CoreWidgetVH(itemView)

    companion object {
        protected const val DEFAULT_MARGIN_BOTTOM = 8
        protected const val DEFAULT_MARGIN_TOP = 16

        private const val TAG_DIVIDER = "tag_divider"
    }
}

