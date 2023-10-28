package com.doubtnutapp.leaderboard.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.applyBackgroundTintList
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.addOnTabSelectedListener
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetLeaderboardTabBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.leaderboard.event.OnTabSelected
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class LeaderboardTabWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<LeaderboardTabWidget.WidgetHolder, LeaderboardTabModel,
        WidgetLeaderboardTabBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetLeaderboardTabBinding {
        return WidgetLeaderboardTabBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: LeaderboardTabModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()
        binding.tabLayout2.removeAllTabs()
        binding.tabLayout2.clearOnTabSelectedListeners()

        binding.tabLayout.isVisible = model.data.style != 1
        binding.tabLayout2.isVisible = model.data.style == 1

        val tabLayout = if (model.data.style == 1) {
            binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 48.dpToPx())
            binding.root.setPadding(0, 0, 0, 0)
            binding.root.background = ColorDrawable(ContextCompat.getColor(context, R.color.white))
            binding.tabLayout2
        } else {
            binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 38.dpToPx())
            binding.root.background =
                ContextCompat.getDrawable(context, R.drawable.bg_rounded_corner_white_fill_24dp)
            binding.root.setPadding(5, 5, 5, 5)
            binding.tabLayout
        }

        binding.root.applyBackgroundTintList(model.data.background, "#ececec")
        binding.root.elevation = model.data.elevation?.toFloatOrNull() ?: 0f

        model.data.items?.forEach { item ->
            tabLayout.addTab(tabLayout.newTab()
                .apply {
                    text = item.title
                    tag = item.id
                }
            )
        }

        var title = ""
        model.data.items?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                tabLayout.getTabAt(it)?.select()
                title = model.data.items?.getOrNull(it)?.title.orEmpty()
            }

        val source = if (model.data.assortmentId.isNullOrEmpty()) {
            EventConstants.TEST
        } else {
            EventConstants.COURSE
        }
        analyticsPublisher.publishEvent(
            hashMapOf<String, Any>(
                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                EventConstants.TEST_ID to model.data.testId.orEmpty(),
                EventConstants.TAB_NAME to title,
                EventConstants.SOURCE to source,
            ).let {
                it.putAll(model.extraParams.orEmpty())
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.VIEWED}",
                    it
                )

            }
        )


        tabLayout.addOnTabSelectedListener { tab ->
            DoubtnutApp.INSTANCE.bus()?.send(OnTabSelected(tab.tag?.toString()))
        }

        if (model.data.margin == true) {
            holder.itemView.setMargins(
                Utils.convertDpToPixel(12f).toInt(),
                Utils.convertDpToPixel(10f).toInt(),
                Utils.convertDpToPixel(12f).toInt(),
                0
            )
        }
        return holder
    }

    companion object {
        const val TAG = "LeaderboardTabWidget"
        const val EVENT_TAG = "leaderboard_tab_widget"
    }

    class WidgetHolder(binding: WidgetLeaderboardTabBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetLeaderboardTabBinding>(binding, widget)

}

@Parcelize
@Keep
class LeaderboardTabModel :
    WidgetEntityModel<LeaderboardTabData, WidgetAction>(), Parcelable

@Parcelize
@Keep
data class LeaderboardTabData(
    @SerializedName("margin") val margin: Boolean?,
    @SerializedName("background") val background: String?,
    @SerializedName("elevation") val elevation: String?,
    @SerializedName("style") val style: Int?,
    @SerializedName("items") val items: List<LeaderboardTabItem>?,
    @SerializedName("assortment_id") var assortmentId: String?,
    @SerializedName("test_id") var testId: String?,
) : WidgetData(), Parcelable

@Parcelize
@Keep
data class LeaderboardTabItem(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean?
) : WidgetData(), Parcelable
