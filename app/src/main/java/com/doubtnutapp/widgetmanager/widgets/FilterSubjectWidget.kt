package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.base.OnboardingClicked
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.domain.homefeed.interactor.OnboardingData
import com.doubtnutapp.training.CustomToolTip
import com.doubtnutapp.training.OnboardingManager
import com.doubtnutapp.utils.BannerActionUtils.deeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.item_filter_chip_subject.view.*
import kotlinx.android.synthetic.main.widget_filter_tabs.view.*
import javax.inject.Inject

class FilterSubjectWidget(context: Context) : BaseWidget<FilterSubjectWidget.FilterTabsWidgetHolder,
        FilterSubjectWidget.FilterTabsWidgetModel>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getView(): View {
        return View.inflate(context, R.layout.widget_filter_tabs, this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FilterTabsWidgetHolder(getView())
    }

    override fun bindWidget(
        holder: FilterTabsWidgetHolder,
        model: FilterTabsWidgetModel
    ): FilterTabsWidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val data: FilterTabWidgetData = model.data
        widgetViewHolder.itemView.rvFilters.adapter = FilterTabsAdapter(
            model,
            holder.itemView.context,
            data.selectedSubject,
            actionPerformer,
            analyticsPublisher,
            model.extraParams ?: HashMap()
        )
        if (widgetViewHolder.itemView.rvFilters.itemDecorationCount == 0)
            widgetViewHolder.itemView.rvFilters.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        context!!
                    ).toInt()
                )
            )
        return holder
    }

    class FilterTabsAdapter(
        val model: FilterTabsWidgetModel,
        val context: Context,
        private var selectedSubject: String?,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        init {
            if (selectedSubject.isNullOrBlank()) {
                this.selectedSubject = model.data.items.getOrNull(0)?.filterId.orEmpty()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return FacultyListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_filter_chip_subject, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = model.data.items[position]
            if (model.isOnboardingEnabled == true) {
                showToolTip(position, context, holder.itemView.filterChip)
            }
            holder.itemView.filterChip.apply {
                tvFilter.text = data.displayName
                if (data.isSelected == true) {
                    background = Utils.getShape(
                        data.selectedColor ?: "#fff5f5",
                        data.filterColor,
                        40f
                    )
                    ivCross.visibility = VISIBLE
                } else {
                    background = Utils.getShape(
                        "#ffffff",
                        data.filterColor,
                        40f
                    )
                    tvFilter.setTextColor(Utils.parseColor(data.filterColor))
                    ivCross.visibility = GONE
                }
                setOnClickListener {
                    if (data.isSelected == true) {
                        actionPerformer?.performAction(
                            FilterSelectAction(
                                -1,
                                null, "subject"
                            )
                        )
                        return@setOnClickListener
                    }
                    selectedSubject = data.filterId
                    ivCross.visibility = GONE
                    notifyDataSetChanged()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(EventConstants.COURSE_FILTER_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.SOURCE to "subject",
                                EventConstants.EVENT_NAME_ID to selectedSubject.orEmpty(),
                                EventConstants.WIDGET to "FilterSubjectWidget"
                            ).apply {
                                putAll(extraParams)
                            })
                    )
                    actionPerformer?.performAction(
                        FilterSelectAction(
                            -1,
                            data.filterId, "subject"
                        )
                    )
                }
            }
        }

        private fun showToolTip(position: Int, context: Context, view: View) {
            val onboardingList: List<OnboardingData>? =
                DoubtnutApp.INSTANCE.onboardingList.orEmpty()
            if (onboardingList != null && onboardingList.isNotEmpty()) {
                val onboardingData: OnboardingData? = onboardingList[1]
                if (onboardingData != null && position == onboardingData.position ?: 2) {
                    OnboardingManager(
                        context as FragmentActivity,
                        onboardingData?.id
                            ?: 0,
                        onboardingData.title.orEmpty(),
                        onboardingData.description.orEmpty(),
                        onboardingData.buttonText.orEmpty(),
                        {
                            if (!onboardingData.deeplink.isNullOrEmpty()) {
                                deeplinkAction.performAction(context, onboardingData.deeplink)
                            } else {
                                actionPerformer?.performAction(OnboardingClicked("filter"))
                            }
                        },
                        {
                            if (!onboardingData.deeplink.isNullOrEmpty()) {
                                deeplinkAction.performAction(context, onboardingData.deeplink)
                            } else {
                                actionPerformer?.performAction(OnboardingClicked("filter"))
                            }
                        },
                        CustomToolTip(context),
                        onboardingData.audioUrl.orEmpty()
                    ).launchTourGuide(view)
                    model.isOnboardingEnabled = false
                }
            }
        }

        override fun getItemCount(): Int = model.data.items.size

        class FacultyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }

    class FilterTabsWidgetHolder(itemView: View) : CoreWidgetVH(itemView)

    class FilterTabsWidgetModel : WidgetEntityModel<FilterTabWidgetData, WidgetAction>()

    @Keep
    data class FilterTabWidgetData(
        @SerializedName("items") val items: List<FilterTabItem>,
        var selectedSubject: String? = "",
    ) : WidgetData()

    @Keep
    data class FilterTabItem(
        @SerializedName("color") val filterColor: String,
        @SerializedName("selected_color") val selectedColor: String?,
        @SerializedName("filter_id") val filterId: String,
        @SerializedName("text") val displayName: String,
        @SerializedName("is_selected") val isSelected: Boolean?,
    )
}