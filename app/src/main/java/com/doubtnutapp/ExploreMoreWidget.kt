package com.doubtnutapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.DialogImageQuestionAskedBinding
import com.doubtnutapp.databinding.ItemWidgetExploreBinding
import com.doubtnutapp.databinding.WidgetExploreMoreBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 30/11/21
 */

class ExploreMoreWidget constructor(
    context: Context
) : BaseBindingWidget<ExploreMoreWidget.WidgetHolder, ExploreMoreWidgetModel, WidgetExploreMoreBinding>(
    context
) {

    companion object {
        const val TAG = "ExploreMoreWidget"
        const val EVENT_TAG = "qa_widget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var source: String? = ""

    override fun getViewBinding(): WidgetExploreMoreBinding {
        return WidgetExploreMoreBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ExploreMoreWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(8, 8, 16, 16)
        })

        val data = model.data
        val binding = holder.binding

        binding.apply {
            title.text = data.title
            questionImage.loadImage(data.questionImageUrl)

            data.questionImageUrl?.let { imageUrl ->
                questionImage.setOnClickListener {
                    showImageInDialog(imageUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            name = "${EVENT_TAG}_${EventConstants.EVENT_IMAGE_CLICK}",
                            params = hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, source.orEmpty())
                                EventConstants.WIDGET to TAG
                            }.apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }
                zoomImageButton.visibility = View.VISIBLE
                questionText.visibility = View.GONE
            } ?: run {
                questionText.visibility = View.VISIBLE
                questionText.text = data.questionText
                zoomImageButton.visibility = View.GONE
            }

            if (data.backgroundColor.isValidColorCode()) {
                root.setBackgroundColor(Color.parseColor(data.backgroundColor))
            }

            val adapter = Adapter(
                data.items,
                model,
                model.extraParams ?: HashMap()
            )

            rv.adapter = adapter

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = "${EVENT_TAG}_${EventConstants.EVENT_ITEMS_SHOWN}",
                    params = hashMapOf<String, Any>().apply {
                        put(EventConstants.ITEM, data.items
                            .map {
                                it.title
                            }
                        )
                        put(EventConstants.SOURCE, source.orEmpty())
                        put(EventConstants.ITEM_COUNT, data.items.size)
                        EventConstants.WIDGET to TAG
                    }.apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        return holder
    }

    private fun showImageInDialog(url: String) {
        val layoutInflater = LayoutInflater.from(context)
        val viewImageDialog = DialogImageQuestionAskedBinding.inflate(layoutInflater, null, false)
        viewImageDialog.imageQuestion.loadImage(url)

        val alertDialog = AlertDialog.Builder(context)
            .setView(viewImageDialog.root)
            .create()
        alertDialog.show()

        viewImageDialog.imageClose.setOnClickListener {
            alertDialog.dismiss()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = "${EVENT_TAG}_${EventConstants.EVENT_DIALOG_CLOSE_CLICK}",
                    params = hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, source.orEmpty())
                    }
                )
            )
        }
    }

    inner class Adapter(
        val items: List<ExploreMoreWidgetItem>,
        val model: ExploreMoreWidgetModel,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemWidgetExploreBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        inner class ViewHolder(private val binding: ItemWidgetExploreBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ExploreMoreWidgetItem) {
                binding.title.text = item.title
                binding.icon.loadImage(item.imageUrl)
                binding.root.setOnClickListener {
                    deeplinkAction.performAction(context, item.deeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            name = "${EVENT_TAG}_${EventConstants.WIDGET_ITEM_CLICK}",
                            params = hashMapOf<String, Any>().apply {
                                put(EventConstants.ITEM_POSITION, bindingAdapterPosition)
                                put(EventConstants.SOURCE, source.orEmpty())
                                put(EventConstants.EVENT_NAME_TITLE, item.title)
                            }
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetHolder(
        binding: WidgetExploreMoreBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetExploreMoreBinding>(binding, widget)
}

@Keep
class ExploreMoreWidgetModel :
    WidgetEntityModel<ExploreMoreWidgetData, WidgetAction>()

@Keep
data class ExploreMoreWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("question_image_url") val questionImageUrl: String?,
    @SerializedName("question_text") val questionText: String?,
    @SerializedName("background_color") val backgroundColor: String,
    @SerializedName("items") val items: List<ExploreMoreWidgetItem>,
) : WidgetData()

@Keep
data class ExploreMoreWidgetItem(
    @SerializedName("title") val title: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("image_url") val imageUrl: String
)