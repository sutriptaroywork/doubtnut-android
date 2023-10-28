package com.doubtnut.core.widgets.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.*
import com.google.gson.annotations.SerializedName

/**
 * Building block for Widget System used in the Application.
 */

@Keep
open class WidgetEntityModel<WD : WidgetData?, WA : WidgetAction?>(
    @SerializedName("type") var _type: String? = null,
    @SerializedName("data") var _data: WD? = null,

    @SerializedName("action") var action: WA? = null,

    @SerializedName("widget_type") var _widgetType: String? = "",
    @SerializedName("widget_data") var _widgetData: WD? = null,

    @SerializedName("layout_config") var layoutConfig: WidgetLayoutConfig? = null,
    @SerializedName("divider_config") var dividerConfig: DividerConfig? = null,

    @SerializedName("extra_params") var extraParams: HashMap<String, Any>? = null,
    @SerializedName("onboarding_enabled") var isOnboardingEnabled: Boolean? = null,

    /**
     * This key is used to mark the view to be stick on header during scroll
     * refer [StickyHeadersLinearLayoutManager]
     */
    @SerializedName("is_sticky") var isSticky: Boolean? = null,
    @SerializedName("tracking_view_id") var trackingViewId: String? = null,

    /**
     * This key is associated with grouping the tabs
     * See [ParentAutoplayWidget] for the usage
     */
    @SerializedName("group_id") var groupId: String? = null,

    @SerializedName("id") var id: String? = null,
) {

    val type: String
        get() = _type ?: _widgetType!!

    val data: WD
        get() = _data ?: _widgetData!!

    override fun toString(): String {
        return "WidgetEntityModel(_type=$_type, _data=$_data, action=$action, _widgetType=$_widgetType, _widgetData=$_widgetData, layoutConfig=$layoutConfig, extraParams=$extraParams, isOnboardingEnabled=$isOnboardingEnabled, isSticky=$isSticky, type='$type', data=$data)"
    }
}