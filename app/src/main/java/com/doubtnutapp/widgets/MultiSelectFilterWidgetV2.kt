package com.doubtnutapp.widgets

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.edit
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ExamSelectionAction
import com.doubtnutapp.base.FetchDetails
import com.doubtnutapp.base.FilterSelectionAction
import com.doubtnutapp.course.widgets.NotesFilterItem
import com.doubtnutapp.databinding.WidgetMultiselectFilterV2Binding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.liveclass.ui.dialog.ChooseExamBottomSheetDialogFragment
import com.doubtnutapp.liveclass.ui.dialog.FilterSelectionDialogFragment
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import tourguide.tourguide.util.locationOnScreen
import javax.inject.Inject

/**
 * PRD: https://docs.google.com/document/d/1U5Q5yXQHsSgPb2PXRHK6VTyHCbifUa-o8mgh_vv3p60/edit
 */
class MultiSelectFilterWidgetV2(
    context: Context
) : BaseBindingWidget<MultiSelectFilterWidgetV2.WidgetHolder, MultiSelectFilterWidgetV2Model, WidgetMultiselectFilterV2Binding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var categoryId: String? = null
    var source: String? = null

    override fun getViewBinding(): WidgetMultiselectFilterV2Binding {
        return WidgetMultiselectFilterV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(
        holder: WidgetHolder,
        model: MultiSelectFilterWidgetV2Model
    ): WidgetHolder {
        model.layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
        super.bindWidget(holder, model)
        categoryId = model.extraParams?.get(EventConstants.CATEGORY).toString()

        val binding = holder.binding

        if (model.data.showExamBottomSheet == true) {
            model.data.widgetItems
                ?.firstOrNull { it.isExamFilter == true }
                ?.let { widgetItem ->
                    showChooseExamBottomSheetDialogFragment(model, widgetItem)
                }
        }

        binding.llMain.removeAllViews()

        binding.hsvMain.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                defaultPrefs(context).edit {
                    putBoolean(Constants.SHOW_MULTI_SELECT_FILTER_WIDGET_V2_SCROLL_ANIM, false)
                }
            }
            return@setOnTouchListener false
        }
        if (showMultiSelectFilterWidgetV2ScrollAnim && defaultPrefs(context).getBoolean(
                Constants.SHOW_MULTI_SELECT_FILTER_WIDGET_V2_SCROLL_ANIM,
                true
            )
        ) {
            animateHsvMain(binding.hsvMain)
            showMultiSelectFilterWidgetV2ScrollAnim = false
        }
        model.data.widgetItems?.forEach { widgetItem ->
            val categorySelectionTextView = CategorySelectionTextView(context)
            categorySelectionTextView.setUpView(widgetItem)
            if (widgetItem.isExamFilter == true) {
                categorySelectionTextView.tag = TAG_EXAM_FILTER
            }
            categorySelectionTextView.setOnClickListener {
                defaultPrefs(context).edit {
                    putBoolean(Constants.SHOW_MULTI_SELECT_FILTER_WIDGET_V2_SCROLL_ANIM, false)
                }

                val locationOnScreen = Point()
                it.locationOnScreen.let { point ->
                    locationOnScreen.x = point.x
                    locationOnScreen.y =
                        point.y - resources.getDimension(R.dimen.toolbar_height).toInt()
                }

                FilterSelectionDialogFragment.newInstance(
                    widgetItem,
                    locationOnScreen
                ).apply {
                    actionPerformer = object : ActionPerformer {
                        override fun performAction(action: Any) {
                            categorySelectionTextView.setUpView(widgetItem)
                            if (action is FilterSelectionAction) {
                                model.data.widgetItems?.let { widgetItems ->
                                    fetchDetails(widgetItems)
                                }
                                sendAnalyticsData(
                                    EventConstants.CHILD_FILTER_CLICKED,
                                    widgetItem,
                                    action.lastSelectedItem,
                                    ignoreSnowplow = true
                                )
                            }
                        }
                    }
                }
                    .show(
                        (context as AppCompatActivity).supportFragmentManager,
                        FilterSelectionDialogFragment.TAG
                    )
            }

            binding.llMain.addView(categorySelectionTextView)
            categorySelectionTextView.updateMargins(end = 8.dpToPx())
        }
        return holder
    }

    private fun animateHsvMain(hsvMain: HorizontalScrollView) {
        ObjectAnimator.ofInt(hsvMain, "scrollX", 80.dpToPx()).apply {
            duration = 800
            doOnEnd {
                ObjectAnimator.ofInt(hsvMain, "scrollX", (-80).dpToPx()).apply {
                    duration = 800
                    start()
                }
            }
            start()
        }
    }

    private fun showChooseExamBottomSheetDialogFragment(
        model: MultiSelectFilterWidgetV2Model,
        widgetItem: MultiSelectFilterWidgetV2Item
    ) {
        ChooseExamBottomSheetDialogFragment.newInstance(
            widgetItem,
            model.data.examBottomSheetTitle.orEmpty(),
            model.data.examBottomSheetAction.orEmpty()
        )
            .apply {
                actionPerformer = object : ActionPerformer {
                    override fun performAction(action: Any) {
                        @Suppress("SimpleRedundantLet")
                        // This could return null. This lint warning is not correct.
                        findViewWithTag<CategorySelectionTextView?>(TAG_EXAM_FILTER)
                            ?.let {
                                it.setUpView(widgetItem)
                            }

                        if (action is ExamSelectionAction) {
                            model.data.widgetItems?.let { widgetItems ->
                                fetchDetails(widgetItems)
                            }

                            sendAnalyticsData(
                                EventConstants.EXAM_SELECTION_BOTTOM_SHEET,
                                widgetItem,
                                action.lastSelectedItem
                            )
                        }
                    }
                }
            }
            .show(
                (context as AppCompatActivity).supportFragmentManager,
                ChooseExamBottomSheetDialogFragment.TAG
            )
    }

    private fun sendAnalyticsData(
        eventName: String,
        widgetItem: MultiSelectFilterWidgetV2Item,
        lastSelectedItem: NotesFilterItem,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.CATEGORY, categoryId.orEmpty())
                    put(
                        EventConstants.MASTER_FILTER_NAME,
                        widgetItem.title.orEmpty()
                    )
                    if (widgetItem.isMultiSelectFilter()) {
                        put(EventConstants.CHILD_FILTER_IDS,
                            widgetItem.filterItems
                                ?.filter { filterItem -> filterItem.isSelected }
                                ?.map { filterItem -> filterItem.id }
                                .orEmpty()
                        )
                    } else {
                        put(
                            EventConstants.CHILD_FILTER_ID,
                            lastSelectedItem.id.orEmpty()
                        )
                    }
                },
                ignoreSnowplow= ignoreSnowplow
            )
        )
    }

    private fun fetchDetails(widgetItems: List<MultiSelectFilterWidgetV2Item>) {
        val filterList = ArrayList<String>()
        widgetItems.forEach { multiSelectFilterWidgetV2Item ->
            multiSelectFilterWidgetV2Item.filterItems?.forEach { notesFilterItem ->
                if (notesFilterItem.isSelected && !notesFilterItem.id.isNullOrEmpty()) {
                    filterList.add(notesFilterItem.id.orEmpty())
                }
            }
        }
        this@MultiSelectFilterWidgetV2.actionPerformer?.performAction(
            FetchDetails(filterList)
        )
    }

    class WidgetHolder(binding: WidgetMultiselectFilterV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetMultiselectFilterV2Binding>(binding, widget)

    companion object {
        @Suppress("unused")
        private const val TAG = "MultiSelectFilterWidgetV2"
        private const val TAG_EXAM_FILTER = "exam_filter"

        private var showMultiSelectFilterWidgetV2ScrollAnim = true
    }
}

class MultiSelectFilterWidgetV2Model :
    WidgetEntityModel<MultiSelectFilterWidgetV2Data, WidgetAction>()

@Keep
data class MultiSelectFilterWidgetV2Data(
    @Suppress("SpellCheckingInspection")
    @SerializedName("show_exam_bottom_sheet")
    val showExamBottomSheet: Boolean?,
    @SerializedName("exam_bottom_sheet_title")
    val examBottomSheetTitle: String?,
    @SerializedName("exam_bottom_sheet_action")
    val examBottomSheetAction: String?,
    @SerializedName("items")
    val widgetItems: List<MultiSelectFilterWidgetV2Item>?
) : WidgetData()

@Keep
@Parcelize
data class MultiSelectFilterWidgetV2Item(
    @SerializedName("key") val key: String?,
    @SerializedName("filter_title") val title: String?,
    @SerializedName("filter_items") val filterItems: List<NotesFilterItem>?,
    // Not using this value as its filter item selection changing time to time
//    @SerializedName("is_selected") val isSelected: Boolean = false,
    @SerializedName("is_multi_select") val isMultiSelect: Boolean?,
    @SerializedName("is_exam_filter") val isExamFilter: Boolean?
) : WidgetData(), Parcelable {
    fun isItemSelected() = filterItems?.count { it.isSelected } ?: 0 > 0

    fun isMultiSelectFilter() = isMultiSelect == true
}
