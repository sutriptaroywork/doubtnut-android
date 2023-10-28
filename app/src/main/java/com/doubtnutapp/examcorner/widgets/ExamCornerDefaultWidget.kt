package com.doubtnutapp.examcorner.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ExamCornerDefaultWidgetBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.examcorner.repository.ExamCornerBookmarkRepository
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ExamCornerDefaultWidget(context: Context) : BaseBindingWidget<ExamCornerDefaultWidget.WidgetHolder,
        ExamCornerDefaultWidgetModel, ExamCornerDefaultWidgetBinding>(context) {

    companion object {
        const val TAG = "ExamCornerDefaultWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var examCornerBookmarkRepository: ExamCornerBookmarkRepository

    @Inject
    lateinit var whatsappSharing: WhatsAppSharing

    @Inject
    lateinit var networkUtil: NetworkUtil

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent
                .forceUnWrap()
                .inject(this)
    }

    override fun getViewBinding(): ExamCornerDefaultWidgetBinding {
        return ExamCornerDefaultWidgetBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
            holder: WidgetHolder,
            model: ExamCornerDefaultWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data: ExamCornerDefaultWidgetData = model.data
        val binding = holder.binding

        binding.tvTitle.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()
        binding.tvDate.text = data.date.orEmpty()
        binding.tvTime.text = data.time.orEmpty()
        binding.tvTagText.text = data.tagText.orEmpty()
        if (data.tagText.isNullOrEmpty()) {
            binding.tvTagText.visibility = INVISIBLE
        } else {
            binding.tvTagText.visibility = View.VISIBLE
        }
        binding.tvTagText.background.setTint(
                Utils.parseColor(
                        data.tagColor,
                        Color.RED
                )
        )

        if (data.imageUrl != null) {
            binding.ivImage.loadImage(data.imageUrl)
            binding.ivImage.isVisible = true
            binding.ivVideoIcon.isVisible = data.isPlayable == true
            binding.ivImage.setOnClickListener {
                deeplinkAction.performAction(context, data.imageThumbnailDeeplink)
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                TAG.toLowerCase(Locale.ROOT)
                                        + "_" + "image"
                                        + "_" + EventConstants.WIDGET_ITEM_CLICK,
                                hashMapOf<String, Any>(
                                        EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty()
                                ).apply {
                                    putAll(model.extraParams ?: hashMapOf())
                                }
                        )
                )
            }
        } else {
            binding.ivImage.isVisible = false
            binding.ivVideoIcon.isVisible = false
        }

        binding.btnAction.text = data.buttonText.orEmpty()
        binding.btnAction.isVisible = !data.buttonText.isNullOrBlank()
        binding.btnAction.setOnClickListener {
            deeplinkAction.performAction(context, data.buttonDeeplink)
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            TAG.toLowerCase(Locale.ROOT)
                                    + "_" + "button"
                                    + "_" + EventConstants.WIDGET_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty()
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                    )
            )
        }

        if (data.isBookmarked == true) {
            binding.ivBookmark.setImageResource(R.drawable.ic_bookmark)
        } else {
            binding.ivBookmark.setImageResource(R.drawable.ic_group_396)
        }

        binding.ivBookmark.setOnClickListener {
            data.isBookmarked = data.isBookmarked != true
            val state = data.isBookmarked ?: false
            if (state) {
                binding.ivBookmark.setImageResource(R.drawable.ic_bookmark)
            } else {
                binding.ivBookmark.setImageResource(R.drawable.ic_group_396)
            }

            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            TAG.toLowerCase(Locale.ROOT)
                                    + "_" + "bookmark"
                                    + "_" + EventConstants.WIDGET_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty(),
                                    EventConstants.STATE to state
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                    )
            )
            if (networkUtil.isConnectedWithMessage()) {
                (it.context as? AppCompatActivity)?.lifecycleScope?.launch {
                    examCornerBookmarkRepository
                            .setExamCornerBookmark(data.examCornerId.orEmpty(), state)
                            .catch {}
                            .collect {}
                }
                val message = if (state) {
                    context.getString(R.string.successfully_bookmarked)
                } else {
                    context.getString(R.string.removed_bookmarked)
                }
                ToastUtils.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivShare.setOnClickListener {
            if (!data.deeplink.isNullOrBlank()) {
                whatsappSharing.shareOnWhatsAppFromDeeplink(data.deeplink.orEmpty(), "", "", null)
                whatsappSharing.startShare(context)
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                TAG.toLowerCase(Locale.ROOT)
                                        + "_" + "share"
                                        + "_" + EventConstants.WIDGET_ITEM_CLICK,
                                hashMapOf<String, Any>(
                                        EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty()
                                ).apply {
                                    putAll(model.extraParams ?: hashMapOf())
                                }
                        )
                )
            }
        }

        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, data.deeplink)
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                            TAG.toLowerCase(Locale.ROOT)
                                    + "_" + EventConstants.WIDGET_ITEM_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.EVENT_NAME_ID to data.examCornerId.orEmpty()
                            ).apply {
                                putAll(model.extraParams ?: hashMapOf())
                            }
                    )
            )
        }

        return holder
    }

    class WidgetHolder(binding: ExamCornerDefaultWidgetBinding, widget: BaseWidget<*, *>) :
            WidgetBindingVH<ExamCornerDefaultWidgetBinding>(binding, widget)

}

class ExamCornerDefaultWidgetModel : WidgetEntityModel<ExamCornerDefaultWidgetData, WidgetAction>()

@Keep
data class ExamCornerDefaultWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("tag_color") val tagColor: String?,
        @SerializedName("exam_corner_id") val examCornerId: String?,
        @SerializedName("tag_text") val tagText: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("date") val date: String?,
        @SerializedName("time") val time: String?,
        @SerializedName("image_thumbnail_deeplink") val imageThumbnailDeeplink: String?,
        @SerializedName("button_text") val buttonText: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("is_bookmarked") var isBookmarked: Boolean?,
        @SerializedName("is_playable") val isPlayable: Boolean?,
) : WidgetData()
