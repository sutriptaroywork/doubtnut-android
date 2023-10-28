package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.ItemFacultyV3Binding
import com.doubtnutapp.databinding.WidgetFacultyV3Binding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FacultyWidget(context: Context) : BaseBindingWidget<FacultyWidget.WidgetHolder,
    FacultyWidgetModel, WidgetFacultyV3Binding>(context) {

    companion object {
        const val TAG = "FacultyWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFacultyV3Binding {
        return WidgetFacultyV3Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: FacultyWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: FacultyWidgetData = model.data
        val binding = holder.binding
        binding.apply {
            root.setBackgroundColor(
                Utils.parseColor(
                    data.bgColor, R.color.blue_66e0eaff
                )
            )
            titleTv.text = data.title.orEmpty()
            root.setVisibleState(!data.items.isNullOrEmpty())
            rvFaculty.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL, false
            )
            rvFaculty.adapter = Adapter(
                data.items.orEmpty(),
                actionPerformer,
                analyticsPublisher
            )
        }

        return holder
    }

    class Adapter(
        val items: List<FacultyWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFacultyV3Binding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding

            binding.apply {
                tvTeacherName.text = data.teacherName
                ivTeacher.loadImage(data.imageUrl)
                tvCredential.text = data.credentials
                tvViewDemo.text = data.buttonText
                tvViewDemo.setTextColor(Utils.parseColor(data.buttonColor))
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemFacultyV3Binding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetFacultyV3Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFacultyV3Binding>(binding, widget)
}

class FacultyWidgetModel : WidgetEntityModel<FacultyWidgetData, WidgetAction>()

@Keep
data class FacultyWidgetData(
    @SerializedName("items") val items: List<FacultyWidgetItem>?,
    @SerializedName("bgColor") val bgColor: String?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class FacultyWidgetItem(
    @SerializedName("id") val id: String?,
    @SerializedName("top_title") val teacherName: String?,
    @SerializedName("credential") val credentials: String?,
    @SerializedName("buttonText") val buttonText: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("buttonColor") val buttonColor: String?
)
