package com.doubtnutapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.FetchDetails
import com.doubtnutapp.base.OnNotesFilterClicked
import com.doubtnutapp.course.widgets.NotesFilterItem
import com.doubtnutapp.databinding.WidgetMultiselectFilterBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class MultiSelectFilterWidget(context: Context) :
    BaseBindingWidget<MultiSelectFilterWidget.WidgetHolder,
            MultiSelectFilterWidgetModel, WidgetMultiselectFilterBinding>(context),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    //maps parentId to List of child
    private val filterHashMap = HashMap<String, List<NotesFilterItem>?>()

    //maps childId to parentId
    private var childToParentHashMap = HashMap<String, String>()

    //maps parentId with id of the child selected
    //used to ensure each parent has only one child selected
    private var parentToFilterHashMap = HashMap<String, String>()
    private val filtersList: MutableList<String> = ArrayList()
    private var notesFilterItemList: MutableList<NotesFilterItem> = ArrayList()
    private var categoryId: String? = null
    var source: String? = null
    lateinit var binding: WidgetMultiselectFilterBinding

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
            holder: WidgetHolder,
            model: MultiSelectFilterWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        binding = holder.binding
        filtersList.clear()
        childToParentHashMap = hashMapOf()
        parentToFilterHashMap = hashMapOf()

        if (model.data.widgetItems?.isEmpty()!!) {
            return holder
        }

        model.data.widgetItems?.filter { it.isSelected }?.forEach { multiSelectFilterWidgetItem ->
            multiSelectFilterWidgetItem.filterItems?.filter { it.isSelected }?.forEach { notesFilterItem ->
                parentToFilterHashMap[multiSelectFilterWidgetItem.title.orEmpty()] = notesFilterItem.id.orEmpty()
                filtersList.add(notesFilterItem.id.orEmpty())
            }
        }

        var isChildVisible = false
        notesFilterItemList.clear()
        for (item in model.data.widgetItems!!) {

            //converting parentFilters to NoteFilterItem
            val notesItem = NotesFilterItem(item.title, item.title, item.isSelected)
            notesFilterItemList.add(notesItem)

            if (item.isSelected) {
                isChildVisible = true
                binding.tagView.visibility = View.VISIBLE
                binding.tagView.addTags(item.filterItems, this)
            }

            //mapping parentFilterId list of subFilters
            filterHashMap[item.title!!] = item.filterItems

            for (childItem in item.filterItems.orEmpty()) {
                childToParentHashMap[childItem.id!!] = item.title
            }
        }

        binding.multiTagView.addTags(notesFilterItemList, this)
        if (!isChildVisible) {
            binding.tagView.visibility = View.VISIBLE
            binding.tagView.addTags(model.data.widgetItems!![0].filterItems, this)
        }

        categoryId = model.extraParams?.get(EventConstants.CATEGORY).toString()

        return holder
    }

    class WidgetHolder(
        binding: WidgetMultiselectFilterBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetMultiselectFilterBinding>(binding, widget)

    override fun performAction(action: Any) {
        super.performAction(action)
        if (action is OnNotesFilterClicked) {
            val id = action.id
            if (action.isMultiSelect) {
                binding.multiTagView.removeAllViews()
                for (tag in notesFilterItemList) {
                    tag.isSelected = parentToFilterHashMap.containsKey(tag.id) or (tag.id == id)
                }
                binding.multiTagView.addTags(notesFilterItemList, this)
                binding.tagView.visibility = View.VISIBLE
                binding.tagView.addTags(filterHashMap[id], this)

                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                EventConstants.MASTER_FILTER_CLICKED,
                                hashMapOf(
                                        EventConstants.CATEGORY to categoryId.orEmpty(),
                                        EventConstants.MASTER_FILTER_NAME to id.orEmpty()), ignoreSnowplow = true)

                )
            } else {
                val parId = childToParentHashMap[id].orEmpty()
                if (action.isSelected) {
                    parentToFilterHashMap[parId] = id.orEmpty()
                } else if (!parentToFilterHashMap[parId].isNullOrBlank()) {
                    parentToFilterHashMap.remove(parId)
                }
                filtersList.clear()
                for (it in parentToFilterHashMap) {
                    filtersList.add(it.value)
                }

                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                EventConstants.CHILD_FILTER_CLICKED,
                                hashMapOf(
                                        EventConstants.CATEGORY to categoryId.orEmpty(),
                                        EventConstants.MASTER_FILTER_NAME to parId,
                                        EventConstants.CHILD_FILTER_ID to id.orEmpty()),
                            ignoreSnowplow = true
                        )
                )

                actionPerformer?.performAction(FetchDetails(filtersList))
            }
        }
    }

    companion object {
        private const val TAG = "MultiSelectFilterWidget"
    }

    override fun getViewBinding(): WidgetMultiselectFilterBinding {
        return WidgetMultiselectFilterBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class MultiSelectFilterWidgetModel : WidgetEntityModel<MultiSelectFilterWidgetData, WidgetAction>()

@Keep
data class MultiSelectFilterWidgetData(
        @SerializedName("items") val widgetItems: List<MultiSelectFilterWidgetItem>?
) : WidgetData()

@Keep
data class MultiSelectFilterWidgetItem(
        @SerializedName("key") val key: String?,
        @SerializedName("filter_title") val title: String?,
        @SerializedName("filter_items") val filterItems: List<NotesFilterItem>?,
        @SerializedName("is_selected") val isSelected: Boolean = false
) : WidgetData()

