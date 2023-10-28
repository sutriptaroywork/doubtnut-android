package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.OpenFacultyPage
import com.doubtnutapp.base.PublishEvent
import com.doubtnutapp.data.remote.models.FacultyGridItem
import com.doubtnutapp.data.remote.models.FacultyGridWidgetData
import com.doubtnutapp.data.remote.models.FacultyGridWidgetModel
import com.doubtnutapp.databinding.ItemFacultyCardV2Binding
import com.doubtnutapp.databinding.WidgetTitleFilterListBinding
import com.doubtnutapp.libraryhome.course.ui.FilterDropDownAdapter
import com.doubtnutapp.libraryhome.course.ui.FilterDropdownMenu
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH

class FacultyListV2Widget(context: Context) :
    BaseBindingWidget<FacultyListV2Widget.FacultyListWidgetHolder,
            FacultyGridWidgetModel, WidgetTitleFilterListBinding>(context) {

    override fun getViewBinding(): WidgetTitleFilterListBinding {
        return WidgetTitleFilterListBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = FacultyListWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: FacultyListWidgetHolder,
        model: FacultyGridWidgetModel
    ): FacultyListWidgetHolder {
        super.bindWidget(holder, model)
        val data: FacultyGridWidgetData = model.data!!
        val binding = holder.binding
        binding.tvTitle.text = data.title
        binding.rvItems.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var selectedFilterKey = model.data.selectedFilterKey
        if (selectedFilterKey.isNullOrBlank()) {
            selectedFilterKey = model.data.subjectFilterList?.firstOrNull()?.key.orEmpty()
        }

        val selectedFilter =
            model.data.subjectFilterList?.firstOrNull { it.key == selectedFilterKey }
        binding.textViewFilter.text = selectedFilter?.display.orEmpty()
        val adapter = FacultyListV2Adapter(
            data.items.orEmpty().toMutableList(),
            actionPerformer,
            selectedFilter?.key.orEmpty()
        )
        binding.rvItems.adapter = adapter

        val filterListPair: List<Triple<String, String, String>> =
            model.data.subjectFilterList?.map {
                Triple(
                    it.key.orEmpty(),
                    it.display.orEmpty(),
                    it.color.orEmpty()
                )
            }.orEmpty()
        binding.textViewFilter.setOnClickListener {
            showFilterMenu(filterListPair, adapter, model)
        }
        return holder
    }

    private fun showFilterMenu(
        filterableList: List<Triple<String, String, String>>,
        adapter: FacultyListV2Adapter,
        model: FacultyGridWidgetModel
    ) {
        val menu = FilterDropdownMenu(context!!, filterableList)
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(200f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.showAsDropDown(widgetViewHolder.binding.textViewFilter)
        menu.setCategorySelectedListener(object : FilterDropDownAdapter.FilterSelectedListener {
            override fun onFilterSelected(position: Int, triple: Triple<String, String, String>?) {
                menu.dismiss()
                val key = triple?.first
                val display = triple?.second
                if (key.isNullOrBlank().not()) {
                    adapter.filter.filter(key)
                    model.data?.selectedFilterKey = key
                }
                widgetViewHolder.binding.textViewFilter.text = display.orEmpty()
            }
        })
    }

    class FacultyListV2Adapter(
        val items: MutableList<FacultyGridItem>,
        val actionPerformer: ActionPerformer? = null,
        selectedKey: String
    ) :
        RecyclerView.Adapter<FacultyListV2Adapter.FacultyListViewHolder>(), Filterable {
        private var filteredItems = mutableListOf<FacultyGridItem>()

        init {
            filteredItems = if (selectedKey.isEmpty()
                || selectedKey.equals("all", true)
                || selectedKey.equals("0", true)
            ) {
                items
            } else {
                val filterFacultyGridItem = ArrayList<FacultyGridItem>()
                for (row in items) {
                    if (row.subject?.equals(selectedKey, true) == true) {
                        filterFacultyGridItem.add(row)
                    }
                }
                filterFacultyGridItem
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyListViewHolder {
            return FacultyListViewHolder(
                ItemFacultyCardV2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        }

        override fun onBindViewHolder(holder: FacultyListViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context, holder.itemView, "3.25", R.dimen.spacing_zero
            )
            holder.binding.imageView.loadImageEtx(filteredItems[position].imageUrl.orEmpty())
            holder.binding.textViewFacultyName.text = filteredItems[position].title

            holder.binding.textViewSubject.text = filteredItems[position].subject
            holder.binding.textViewExam.text = filteredItems[position].course
            holder.binding.textViewDegreeCollege.text = filteredItems[position].degreeCollege
            holder.binding.textViewExperience.text =
                "Exp:" + filteredItems[position].yearsExperience + " Years"
            holder.binding.textViewStudent.text = filteredItems[position].students
            holder.binding.buttonWatchDemo.text = filteredItems[position].buttonText
            holder.binding.buttonWatchDemo.setOnClickListener {
                actionPerformer?.performAction(
                    PublishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                            hashMapOf(EventConstants.SOURCE to "teacher_" + filteredItems[position].title)
                        )
                    )
                )

                holder.binding.root.context.startActivity(
                    VideoPageActivity.startActivity(
                        holder.itemView.context,
                        filteredItems[position].questionId.toString(),
                        "", "",
                        filteredItems[position].page.orEmpty(),
                        "", false, "", "", false
                    )
                )
            }

            holder.binding.root.setOnClickListener {
                actionPerformer?.performAction(
                    PublishEvent(
                        AnalyticsEvent(
                            EventConstants.COURSES_TEACHER_CLICK,
                            hashMapOf(EventConstants.WIDGET to "grid"),
                            ignoreSnowplow = true
                        )
                    )
                )

                actionPerformer?.performAction(
                    OpenFacultyPage(
                        filteredItems[position].id?.toIntOrNull()
                            ?: 0, items[position].ecmId?.toIntOrNull() ?: 0
                    )
                )
            }
            val color = Utils.parseColor(filteredItems[position].color)
            holder.binding.textViewSubject.background = getShape(color, color)
        }

        private fun getShape(colorString: Int, strokeColor: Int): GradientDrawable {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
            shape.setColor(colorString)
            shape.setStroke(3, strokeColor)
            return shape
        }

        override fun getItemCount(): Int = filteredItems.size

        class FacultyListViewHolder(val binding: ItemFacultyCardV2Binding) :
            RecyclerView.ViewHolder(binding.root)

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val charString = charSequence.toString()
                    filteredItems = if (charString.isEmpty() || charString.equals(
                            "all",
                            true
                        ) || charString.equals("0", true)
                    ) {
                        items
                    } else {
                        val filteredList = ArrayList<FacultyGridItem>()
                        for (row in items) {
                            if (row.subject?.equals(charString, true) == true) {
                                filteredList.add(row)
                            }
                        }
                        filteredList
                    }

                    val filterResults = FilterResults()
                    filterResults.values = filteredItems
                    return filterResults
                }

                override fun publishResults(
                    charSequence: CharSequence,
                    filterResults: FilterResults
                ) {
                    filteredItems = filterResults.values as ArrayList<FacultyGridItem>
                    notifyDataSetChanged()
                }
            }
        }
    }

    class FacultyListWidgetHolder(binding: WidgetTitleFilterListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTitleFilterListBinding>(binding, widget)

}