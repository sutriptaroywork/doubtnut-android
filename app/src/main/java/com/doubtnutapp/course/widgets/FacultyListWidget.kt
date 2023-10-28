package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemFacultyBinding
import com.doubtnutapp.databinding.WidgetFacultyBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.BannerActionUtils.deeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class FacultyListWidget(context: Context) :
    BaseBindingWidget<FacultyListWidget.WidgetHolder,
        FacultyListWidgetModel, WidgetFacultyBinding>(context) {

    companion object {
        const val TAG = "FacultyListWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetFacultyBinding {
        return WidgetFacultyBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: FacultyListWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: FacultyListWidgetData = model.data
        val binding = holder.binding
        binding.titleTv.text = data.title.orEmpty()
        binding.rvFaculty.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        binding.rvFaculty.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            hashMapOf<String, Any>().apply {
                putAll(model.extraParams.orEmpty())
            }
        )
        return holder
    }

    class Adapter(
        val items: List<FacultyListWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val extraParams: HashMap<String, Any>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemFacultyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Utils.setWidthBasedOnPercentage(
                holder.itemView.context,
                holder.itemView,
                "1.3",
                R.dimen.spacing
            )
            val data = items[position]

            holder.binding.imageViewFaculty.loadImageEtx(data.imageUrl.orEmpty())
            holder.binding.textViewTitle.text = data.title.orEmpty()
            holder.binding.textViewDescription.text = data.description.orEmpty()
            holder.binding.textViewDescriptionTwo.text = data.descriptionTwo.orEmpty()
            holder.binding.tvExperience.text = data.experience.orEmpty()
            holder.binding.ratingBar.rating = data.rating ?: 0f
            if (!data.deeplink.isNullOrEmpty()) {
                holder.binding.playIcon.visibility = VISIBLE
                holder.binding.cardContainer.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(EventConstants.FACULTY_CARD_CLICKED, extraParams, ignoreSnowplow = true)
                    )
                    deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
                }
            } else {
                holder.binding.playIcon.visibility = GONE
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemFacultyBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetFacultyBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetFacultyBinding>(binding, widget)
}

class FacultyListWidgetModel : WidgetEntityModel<FacultyListWidgetData, WidgetAction>()

@Keep
data class FacultyListWidgetData(
    @SerializedName("items") val items: List<FacultyListWidgetItem>?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class FacultyListWidgetItem(
    @SerializedName("id") val id: String?,
    @SerializedName("faculty_name") val title: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("faculty_desc") val description: String?,
    @SerializedName("bottom_title") val descriptionTwo: String?,
    @SerializedName("experience") val experience: String?,
    @SerializedName("rating") val rating: Float?,
    @SerializedName("deeplink") val deeplink: String?
)
