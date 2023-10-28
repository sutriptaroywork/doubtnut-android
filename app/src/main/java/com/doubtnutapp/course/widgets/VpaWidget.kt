package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.repository.WalletRepository
import com.doubtnutapp.databinding.WidgetVpaBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.VpaTextView
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class VpaWidget @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<VpaWidget.WidgetHolder,
    VpaWidget.Model, WidgetVpaBinding>(context, attrs, defStyle) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var walletRepository: WalletRepository

    @Inject
    lateinit var networkUtil: NetworkUtil

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetVpaBinding {
        return WidgetVpaBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val binding = holder.binding
        val data: Data = model.data
        setAccountDetail(binding, data.account)
        binding.layoutShare.setOnClickListener {
            if (data.account?.waDetails.isNullOrBlank()) {
                return@setOnClickListener
            }
            whatsAppSharing.shareOnWhatsApp(
                context = context,
                imageUrl = "",
                imageFilePath = null,
                sharingMessage = data.account?.waDetails.orEmpty()
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.VPA_WHATSAPP_SHARE, ignoreSnowplow = true)
            )
        }

        binding.tvTitle.text = data.title.orEmpty()
        binding.imageView.loadImageEtx(data.imageUrl.orEmpty())

        binding.tvNew.text = data.hyperText.orEmpty()
        if (data.hyperText.isNullOrBlank()) {
            binding.tvNew.visibility = View.INVISIBLE
        } else {
            binding.tvNew.visibility = View.VISIBLE
        }

        if (data.isCollapsed == true) {
            binding.tvExpand.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_drop_down,
                0
            )
            binding.layoutBottom.hide()
        } else {
            binding.tvExpand.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_up_arrow,
                0
            )
            binding.layoutBottom.show()
        }

        binding.layoutTop.setOnClickListener {
            if (data.isCollapsed == true) {
                data.isCollapsed = false
                binding.tvExpand.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_up_arrow,
                    0
                )
                binding.layoutBottom.show()
            } else {
                data.isCollapsed = true
                binding.tvExpand.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_drop_down,
                    0
                )
                binding.layoutBottom.hide()
            }
            if (data.account == null) {
                if (networkUtil.isConnectedWithMessage()) {
                    (it.context as? AppCompatActivity)?.lifecycleScope?.launch {
                        binding.progressBarVpa.show()
                        walletRepository
                            .fetchVpaInfo()
                            .catch {
                                binding.progressBarVpa.hide()
                            }
                            .collect { res ->
                                binding.progressBarVpa.hide()
                                data.account = res.data
                                setAccountDetail(binding, data.account)
                            }
                    }
                } else {
                    binding.progressBarVpa.hide()
                }
            } else {
                binding.progressBarVpa.hide()
            }
        }
        return holder
    }

    private fun setAccountDetail(binding: WidgetVpaBinding, account: Account?) {
        binding.layoutPointers.removeAllViews()
        val details = account?.details
        details?.forEach {
            val textView = VpaTextView(context)
            textView.setViews(it.name.orEmpty() + ": ", it.value.orEmpty())
            binding.layoutPointers.addView(textView)
        }
        binding.tvDesc.text = account?.description.orEmpty()
        binding.tvShareLink.text = account?.btnText.orEmpty()
    }

    class WidgetHolder(binding: WidgetVpaBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVpaBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("hyper_text") val hyperText: String?,
        @SerializedName("image_ratio") val imageRatio: String?,
        @SerializedName("is_collapsed") var isCollapsed: Boolean?,
        @SerializedName("account") var account: Account?,
    ) : WidgetData()

    @Keep
    data class Account(
        @SerializedName("description") val description: String?,
        @SerializedName("details") val details: List<Detail>?,
        @SerializedName("btn_image_url") val btnImageUrl: String?,
        @SerializedName("btn_text") val btnText: String?,
        @SerializedName("wa_details") val waDetails: String?,
    )

    @Keep
    data class Detail(
        @SerializedName("name") val name: String?,
        @SerializedName("value") val value: String?
    )
}
