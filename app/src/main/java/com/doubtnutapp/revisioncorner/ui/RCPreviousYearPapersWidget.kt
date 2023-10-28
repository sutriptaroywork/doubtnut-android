package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.InsertItemsOfATabKey
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.InsertChildrenAtNode
import com.doubtnutapp.base.LibraryWidgetClick
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.LayoutPadding
import com.doubtnutapp.databinding.ItemRcPrioviousYearPaperBinding
import com.doubtnutapp.databinding.ItemRcTestBinding
import com.doubtnutapp.databinding.WidgetParentTabBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.newlibrary.ui.LibraryFragment
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import com.uxcam.UXCam
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 21/4/22.
 */

class RCPreviousYearPapersWidget(context: Context) :
    BaseBindingWidget<RCPreviousYearPapersWidget.WidgetHolder, RCPreviousYearPapersWidget.Model, ItemRcPrioviousYearPaperBinding>(
        context
    ) {

    companion object {
        private const val TAG = "RCTestPapersWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: Data = model.data
        val binding = holder.binding

        binding.apply {
            tvTitle.text = data.heading
            tvSubTitle.text = data.subheading
            ivPlaylist.loadImage(data.imageUrl)
            setOnClickListener { deeplinkAction.performAction(context, data.deeplink) }
        }
        return holder
    }

    class WidgetHolder(
        binding: ItemRcPrioviousYearPaperBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<ItemRcPrioviousYearPaperBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val heading: String?,
        @SerializedName("description") val subheading: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()

    override fun getViewBinding() =
        ItemRcPrioviousYearPaperBinding.inflate(LayoutInflater.from(context), this, true)

}
