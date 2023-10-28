package com.doubtnutapp.sales.widget

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.collectSafely
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.getActivity
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.databinding.WidgetPrePurchaseCallingCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.sales.PrePurchaseCallingCardConstant
import com.doubtnutapp.sales.data.remote.PrePurchaseCallingCardRepository
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.utils.ChatUtil
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrePurchaseCallingCard
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<PrePurchaseCallingCard.WidgetHolder, PrePurchaseCallingCardModel,
        WidgetPrePurchaseCallingCardBinding>(context, attrs, defStyle) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var repository: PrePurchaseCallingCardRepository

    @Inject
    lateinit var checkForPackageInstall: CheckForPackageInstall

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var studyGroupRepository: StudyGroupRepository

    class WidgetHolder(binding: WidgetPrePurchaseCallingCardBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPrePurchaseCallingCardBinding>(binding, widget)

    companion object {
        @Suppress("unused")
        private const val TAG = "PrePurchaseCallingCard"

        private const val VIEW_CLOSE = "close"
        private const val VIEW_CARD = "card"
        private const val VIEW_CHAT = "chat"
        private const val VIEW_CALL = "call"

        var isShownOnCheckAllCourses = false
    }

    override fun getViewBinding(): WidgetPrePurchaseCallingCardBinding {
        return WidgetPrePurchaseCallingCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PrePurchaseCallingCardModel
    ): WidgetHolder {
        val marginTop = when (model.data.source) {
            Constants.SOURCE_HOME -> 8
            else -> 0
        }
        model.layoutConfig = WidgetLayoutConfig(
            marginTop = marginTop,
            marginBottom = 0,
            marginLeft = 0,
            marginRight = 0
        )

        super.bindWidget(holder, model)

        val binding = holder.binding

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.CARD_SHOWN,
                hashMapOf(
                    EventConstants.SOURCE to model.data.source.orEmpty(),
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                    EventConstants.FLAG_ID to model.data.flagId.orEmpty(),
                    EventConstants.VARIANT_ID to model.data.variantId.orEmpty()
                )
            )
        )

        binding.tvTitle.text = model.data.title
        binding.tvSubtitle.text = model.data.subtitle

        if (model.data.imageUrl.isNullOrEmpty()) {
            binding.ivImage.hide()
        } else {
            binding.ivImage.show()
            binding.ivImage.loadImage(model.data.imageUrl)
        }

        when {
            model.data.callDeepLink?.contains("dialer", ignoreCase = true) == true -> {
                binding.tvCall.text = context.getString(R.string.call_now)
            }
            model.data.callDeepLink?.contains("callback", ignoreCase = true) == true -> {
                binding.tvCall.text = context.getString(R.string.request_a_call_back)
            }
        }

        when (model.data.source) {
            Constants.SOURCE_COURSE_DETAIL,
            Constants.SOURCE_COURSE_CATEGORY -> {
                binding.tvTitle.textSize = 18f
                binding.tvTitle.layoutParams = binding.tvTitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 18.dpToPx()
                        this.topMargin = 20.dpToPx()
                        this.rightMargin = 18.dpToPx()
                    }
                }

                binding.tvSubtitle.textSize = 13f
                binding.tvSubtitle.layoutParams = binding.tvSubtitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 19.dpToPx()
                        this.rightMargin = 19.dpToPx()
                    }
                }
                binding.clChat.layoutParams = binding.clChat.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.bottomMargin = 8.dpToPx()
                    }
                }
                binding.clCall.layoutParams = binding.clCall.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.bottomMargin = 8.dpToPx()
                    }
                }
            }

            Constants.SOURCE_PAYMENT_FAILURE -> {
                binding.tvTitle.textSize = 19f
                binding.tvTitle.layoutParams = binding.tvTitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 23.dpToPx()
                        this.topMargin = 15.dpToPx()
                        this.rightMargin = 23.dpToPx()
                    }
                }

                binding.tvSubtitle.textSize = 14f
                binding.tvSubtitle.layoutParams = binding.tvSubtitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 23.dpToPx()
                        this.topMargin = 6.dpToPx()
                        this.rightMargin = 23.dpToPx()
                    }
                }
            }
            Constants.SOURCE_ALL_COURSES,
            Constants.SOURCE_HOME -> {
                if (model.data.source == Constants.SOURCE_ALL_COURSES
                    || (model.data.source == Constants.SOURCE_HOME && model.data.imageUrl.isNullOrEmpty())
                ) {
                    binding.tvTitle.textSize = 16f
                    binding.tvTitle.layoutParams = binding.tvTitle.layoutParams.apply {
                        if (this is ConstraintLayout.LayoutParams) {
                            this.leftMargin = 17.dpToPx()
                            this.topMargin = 18.dpToPx()
                            this.rightMargin = 17.dpToPx()
                        }
                    }

                    binding.tvSubtitle.textSize = 13f
                    binding.tvSubtitle.layoutParams = binding.tvSubtitle.layoutParams.apply {
                        if (this is ConstraintLayout.LayoutParams) {
                            this.leftMargin = 18.dpToPx()
                            this.rightMargin = 17.dpToPx()
                        }
                    }
                    binding.clChat.layoutParams = binding.clChat.layoutParams.apply {
                        if (this is ConstraintLayout.LayoutParams) {
                            this.bottomMargin = 6.dpToPx()
                        }
                    }
                    binding.clCall.layoutParams = binding.clCall.layoutParams.apply {
                        if (this is ConstraintLayout.LayoutParams) {
                            this.bottomMargin = 6.dpToPx()
                        }
                    }
                }
            }

            PrePurchaseCallingCardConstant.SOURCE_COURSE_CATEGORY_BOTTOM_SHEET -> {
                binding.ivClose.layoutParams = binding.ivClose.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.topMargin = 6.dpToPx()
                        this.rightMargin = 6.dpToPx()
                    }
                }

                binding.tvTitle.textSize = 20f
                binding.tvTitle.layoutParams = binding.tvTitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 13.dpToPx()
                        this.topMargin = 41.dpToPx()
                        this.rightMargin = 17.dpToPx()
                    }
                }

                binding.tvSubtitle.textSize = 18f
                binding.tvSubtitle.layoutParams = binding.tvSubtitle.layoutParams.apply {
                    if (this is ConstraintLayout.LayoutParams) {
                        this.leftMargin = 17.dpToPx()
                        this.topMargin = 10.dpToPx()
                        this.rightMargin = 18.dpToPx()
                    }
                }

                binding.clChat.layoutParams = binding.clChat.layoutParams.apply {
                    minimumWidth = 119.dpToPx()
                    minimumHeight = 32.dpToPx()
                    if (this is ConstraintLayout.LayoutParams) {
                        this.topMargin = 38.dpToPx()
                        this.bottomMargin = 11.dpToPx()
                    }
                }
                binding.ivChat.layoutParams = binding.ivChat.layoutParams.apply {
                    width = 17.dpToPx()
                    height = 17.dpToPx()
                }
                binding.tvChat.textSize = 16f

                binding.clCall.layoutParams = binding.clCall.layoutParams.apply {
                    minimumWidth = 119.dpToPx()
                    minimumHeight = 32.dpToPx()
                    if (this is ConstraintLayout.LayoutParams) {
                        this.topMargin = 38.dpToPx()
                        this.bottomMargin = 11.dpToPx()
                    }
                }
                binding.ivCall.layoutParams = binding.ivCall.layoutParams.apply {
                    width = 17.dpToPx()
                    height = 17.dpToPx()
                }
                binding.tvCall.textSize = 16f
            }
        }

        if (model.data.source != PrePurchaseCallingCardConstant.SOURCE_COURSE_CATEGORY_BOTTOM_SHEET) {
            if (model.data.backgroundColor.isNullOrEmpty()) {
                holder.itemView.setBackgroundColor(Color.WHITE)
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor(model.data.backgroundColor))
            }
        }

        binding.ivClose.isVisible = model.data.isDismissable
        binding.ivClose.setOnClickListener {
            DoubtnutApp.INSTANCE.bus()?.send(PrePurchaseCallingCardDismiss(model.data.source))
            dismissPrePurchaseCallingCard(model.data, VIEW_CLOSE)
        }

        holder.itemView.setOnClickListener {
            if (model.data.deeplink.isNullOrEmpty().not()) {
                deeplinkAction.performAction(context, model.data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.CALLING_CARD_CLICK,
                        ignoreFacebook = true,
                        params = hashMapOf(
                            EventConstants.SOURCE to model.data.source.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                            EventConstants.FLAG_ID to model.data.flagId.orEmpty(),
                            EventConstants.VARIANT_ID to model.data.variantId.orEmpty()
                        ),
                        ignoreSnowplow = true
                    )
                )
            }
            dismissPrePurchaseCallingCard(model.data, VIEW_CARD)
        }

        binding.clChat.isVisible = defaultPrefs().getBoolean(Constants.CHAT_BTN_SHOW, true)
        binding.clChat.setOnClickListener {
            val isWAAvailable =
                checkForPackageInstall.appInstalled(Constants.WHATSAPP_PACKAGE_NAME) || checkForPackageInstall.appInstalled(
                    Constants.WHATSAPP_FOR_BUSINESS_PACKAGE_NAME
                )
            if (!isWAAvailable || model.data.chatDeeplink?.contains(
                    "chat_support",
                    ignoreCase = true
                ) == true
            ) {
                if (FeaturesManager.isFeatureEnabled(
                        DoubtnutApp.INSTANCE,
                        Features.STUDY_GROUP_AS_FRESH_CHAT
                    )
                ) {
                    createGroup(
                        groupName = null,
                        groupImage = null,
                        isSupport = true
                    )
                } else {
                    ChatUtil.setUser(
                        context,
                        model.data.assortmentId,
                        "",
                        "",
                        "",
                        model.data.source.orEmpty()
                    )
                    ChatUtil.startConversation(context)
                    defaultPrefs().edit()
                        .putLong(Constants.PREF_CHAT_START_TIME, System.currentTimeMillis()).apply()
                }
            } else {
                deeplinkAction.performAction(context, model.data.chatDeeplink)
            }

            dismissPrePurchaseCallingCard(model.data, VIEW_CHAT)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.CARD_CHAT_CLICK,
                    ignoreFacebook = true,
                    params = hashMapOf(
                        EventConstants.SOURCE to model.data.source.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.FLAG_ID to model.data.flagId.orEmpty(),
                        EventConstants.VARIANT_ID to model.data.variantId.orEmpty()
                    ), ignoreBranch = false
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(PrePurchaseCallingCardDismiss(model.data.source))
        }

        binding.clCall.isVisible = defaultPrefs().getBoolean(Constants.CALLBACK_BTN_SHOW, true)
        binding.clCall.setOnClickListener {
            when {
                model.data.callDeepLink?.contains("callback", ignoreCase = true) == true -> {
                    context.getActivity()?.lifecycleScope?.launch {
                        repository
                            .requestCallback(model.data.assortmentId.orEmpty())
                            .catch {
                                it.printStackTrace()
                            }
                            .collect { message ->
                                message?.let {
                                    ToastUtils.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                else -> deeplinkAction.performAction(context, model.data.callDeepLink)
            }

            dismissPrePurchaseCallingCard(model.data, VIEW_CALL)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CARD_CALL_CLICK,
                    hashMapOf(
                        EventConstants.SOURCE to model.data.source.orEmpty(),
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.ASSORTMENT_ID to model.data.assortmentId.orEmpty(),
                        EventConstants.FLAG_ID to model.data.flagId.orEmpty(),
                        EventConstants.VARIANT_ID to model.data.variantId.orEmpty()
                    ), ignoreBranch = false
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(PrePurchaseCallingCardDismiss(model.data.source))
        }
        return holder
    }

    private fun createGroup(groupName: String?, groupImage: String?, isSupport: Boolean?) {
        compositeDisposable.add(
            studyGroupRepository.createGroup(groupName, groupImage, isSupport)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        deeplinkAction.performAction(
                            widgetViewHolder.binding.root.context,
                            it.groupChatDeeplink
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    private fun dismissPrePurchaseCallingCard(data: PrePurchaseCallingCardData, view: String) {
        CoreApplication.INSTANCE.launch {
            repository.dismissPrePurchaseCallingCard(
                assortmentId = data.assortmentId,
                view = view,
                source = data.source
            )
                .collectSafely { }
        }
    }
}

@Parcelize
@Keep
class PrePurchaseCallingCardModel :
    WidgetEntityModel<PrePurchaseCallingCardData, WidgetAction>(), Parcelable

@Parcelize
@Keep
data class PrePurchaseCallingCardData(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("background_color")
    val backgroundColor: String? = null,
    @SerializedName("deeplink")
    val deeplink: String? = null,
    @SerializedName("chat_deeplink")
    val chatDeeplink: String? = null,
    @SerializedName("callback_deeplink")
    val callDeepLink: String? = null,
    @SerializedName("assortment_id")
    val assortmentId: String? = null,
    @SerializedName("flag_id")
    val flagId: String? = null,
    @SerializedName("variant_id")
    val variantId: String? = null,

    var source: String?,
    var isDismissable: Boolean = true
) : WidgetData(), Parcelable
