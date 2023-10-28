package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetNotesFilterBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class NotesFilterWidget(context: Context) :
    BaseBindingWidget<NotesFilterWidget.WidgetHolder,
        NotesFilterModel, WidgetNotesFilterBinding>(context),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: NotesFilterModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        holder.binding.tagView.addTags(model.data.filterItems, this)
        return holder
    }

    class WidgetHolder(
        binding: WidgetNotesFilterBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetNotesFilterBinding>(binding, widget)

    override fun getViewBinding(): WidgetNotesFilterBinding {
        return WidgetNotesFilterBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

class NotesFilterModel : WidgetEntityModel<NotesFilterData, WidgetAction>()

@Keep
data class NotesFilterData(
    @SerializedName("items") val filterItems: List<NotesFilterItem>
) : WidgetData()

@Keep
@Parcelize
data class NotesFilterItem(
    @SerializedName("filter_id") val id: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("is_selected") var isSelected: Boolean = false
) : WidgetData(), Parcelable
