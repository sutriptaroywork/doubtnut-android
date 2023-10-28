package com.doubtnutapp.similarVideo.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnSimilarWidgetClick
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.databinding.WidgetSimilarBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SimilarWidget(context: Context) : BaseBindingWidget<SimilarWidget.WidgetHolder,
        SimilarWidget.Model, WidgetSimilarBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    var source: String? = null

    override fun getViewBinding(): WidgetSimilarBinding {
        return WidgetSimilarBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(4, 4, 16, 16)
        })

        val data = model.data
        val binding = holder.binding
        binding.apply {
            if (data.resourceType == SOLUTION_RESOURCE_TYPE_TEXT) {
                textSolutionImage.show()
            } else {
                textSolutionImage.hide()
            }

            tvQuestionId.text = data.questionId
            questionAskedYear.toggleVisibilityAndSetText(data.ref)

            if (data.askedCount.isNullOrBlank()) {
                questionAskedCount.hide()
            } else {
                questionAskedCount.show()
                questionAskedCount.text =
                    data.askedCount.orEmpty() + "+ " + data.viewsText.orEmpty()
            }

            if (!data.ocrText.isNullOrBlank()) {
                dmathView.show()
                dmathView.text = data.ocrText
            } else {
                dmathView.invisible()
                dmathView.text = ""
            }

            if (data.questionThumbnail.isNullOrBlank()) {
                progressBar.hide()
                ivMatch.hide()
            } else {
                ivMatch.show()
                progressBar.show()
                setImageUrl(binding, data.questionThumbnail)
            }

            val page = data.page?.ifEmptyThenNull().orDefaultValue(Constants.PAGE_SIMILAR)

            clickHelperView.setOnClickListener {
                if (data.resourceType == SOLUTION_RESOURCE_TYPE_TEXT) {
                    val intent = TextSolutionActivity.startActivity(
                        context, data.questionId.orEmpty(),
                        "", "", page, "",
                        false, "", "", false
                    )
                    context.startActivity(intent)
                } else {
                    actionPerformer?.performAction(
                        OnSimilarWidgetClick(
                            data.questionId.orEmpty(),
                            page
                        )
                    )
                }
            }

            overflowMenuSimilar.setOnClickListener {
                PopupMenu(
                    ContextThemeWrapper(context, R.style.PopupMenuStyle),
                    overflowMenuSimilarView,
                    Gravity.END
                ).also { popupMenu ->
                    popupMenu.menuInflater.inflate(R.menu.menu_popup_similar, popupMenu.menu)

                    val menuHelper = MenuPopupHelper(
                        context,
                        popupMenu.menu as MenuBuilder,
                        overflowMenuSimilarView
                    )
                    menuHelper.gravity = Gravity.END
                    menuHelper.setForceShowIcon(true)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.itemWatchLaterSimilar -> {
                                performAction(WatchLaterRequest(data.questionId.orEmpty()))
                            }
                            R.id.itemShareSimilar -> {
                                checkInternetConnection(context) {
                                    whatsAppSharing.shareOnWhatsApp(
                                        ShareOnWhatApp(
                                            VIDEO_CHANNEL,
                                            featureType = "video",
                                            imageUrl = data.questionThumbnail.orEmpty(),
                                            controlParams = hashMapOf(
                                                Constants.PAGE to page,
                                                Constants.Q_ID to data.questionId.orEmpty(),
                                                Constants.PLAYLIST_ID to "",
                                                Constants.SOLUTION_RESOURCE_TYPE to data.resourceType.orEmpty()
                                            ),
                                            bgColor = "#000000",
                                            sharingMessage = data.shareMessage.orEmpty(),
                                            questionId = data.questionId.orEmpty()
                                        )
                                    )
                                    whatsAppSharing.startShare(context)
                                }
                            }
                        }
                        true
                    }
                    menuHelper.show()
                }
            }
        }

        return holder
    }

    private fun setImageUrl(binding: WidgetSimilarBinding, thumbnailUrl: String) {
        Glide.with(context)
            .load(thumbnailUrl)
            .addListener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    binding.ivMatch.hide()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.hide()
                    binding.dmathView.hide()
                    return false
                }
            })
            .into(binding.ivMatch)
    }

    class WidgetHolder(binding: WidgetSimilarBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetSimilarBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("ocr_text") val ocrText: String?,
        @SerializedName("question_thumbnail") val questionThumbnail: String?,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("ref") val ref: String?,
        @SerializedName("views") val askedCount: String?,
        @SerializedName("views_text") val viewsText: String?,
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("share_message") val shareMessage: String?,
        @SerializedName("page") val page: String?
    ) : WidgetData()

}