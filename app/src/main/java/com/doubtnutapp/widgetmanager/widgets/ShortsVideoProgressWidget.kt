package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetShortsVideoProgressBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.shorts.repository.ShortsRepository
import com.doubtnutapp.utils.NetworkUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ShortsVideoProgressWidget(context: Context) :
    BaseBindingWidget<ShortsVideoProgressWidget.WidgetHolder,
            ShortsVideoProgressWidget.Model, WidgetShortsVideoProgressBinding>(context) {

    companion object {
        const val TAG = "ShortsVideoProgressWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var shortsRepository: ShortsRepository

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var whatsappSharing: WhatsAppSharing

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent
            .forceUnWrap()
            .inject(this)

        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    }

    override fun getViewBinding(): WidgetShortsVideoProgressBinding {
        return WidgetShortsVideoProgressBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
        })
        return holder
    }

    class WidgetHolder(binding: WidgetShortsVideoProgressBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetShortsVideoProgressBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String? = null
    ) : WidgetData()
}