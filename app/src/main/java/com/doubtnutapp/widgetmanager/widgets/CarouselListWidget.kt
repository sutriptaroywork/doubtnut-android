package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.HorizontalMarginItemDecoration
import com.doubtnut.core.adapter.ViewPagerAdapter2
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.databinding.ItemWidgetCarouselListBinding
import com.doubtnutapp.databinding.WidgetCarouselListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH

import com.google.gson.annotations.SerializedName
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CarouselListWidget(
    context: Context
) : BaseBindingWidget<CarouselListWidget.CarouselListWidgetHolder,
        CarouselListWidget.CarouselListWidgetModel,
        WidgetCarouselListBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var autoScrollJob: Job? = null
    private var carouselListWidgetData: CarouselListWidgetData? = null

    private val itemDecoration: RecyclerView.ItemDecoration by lazy {
        HorizontalMarginItemDecoration(
            24.dpToPx()
        )
    }

    override fun getViewBinding(): WidgetCarouselListBinding {
        return WidgetCarouselListBinding.inflate(LayoutInflater.from(context),this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = CarouselListWidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(
        holder: CarouselListWidgetHolder,
        model: CarouselListWidgetModel
    ): CarouselListWidgetHolder {
        super.bindWidget(holder, model)
        val data: CarouselListWidgetData = model.data
        carouselListWidgetData = data

        if (data.items.isNullOrEmpty() || data.items.size == 1) {
            holder.binding.circleIndicator.hide()
        } else {
            holder.binding.circleIndicator.show()
        }

        (context as? AppCompatActivity)?.let { appCompatActivity ->

            holder.binding.viewPager.offscreenPageLimit = 3

            holder.binding.viewPager.setPageTransformer(null)
            holder.binding.viewPager.removeItemDecoration(itemDecoration)

            if (!model.data.fullWidthCards) {
                val nextItemVisiblePx = 16.dpToPx()
                val currentItemHorizontalMarginPx = 24.dpToPx()
                val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
                val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                    page.translationX = -pageTranslationX * position
                }
                holder.binding.viewPager.setPageTransformer(pageTransformer)
                holder.binding.viewPager.addItemDecoration(itemDecoration)
            }

            val adapter = ViewPagerAdapter2(
                appCompatActivity,
                model.data.items.mapIndexed { index, _ ->
                    CarouselListWidgetBannerFragment.newInstance(
                        data = data,
                        type = model.type,
                        parentPosition = model.data.parentPosition ?: holder.bindingAdapterPosition,
                        position = index
                    )
                }
            )
            holder.binding.viewPager.adapter = adapter

            holder.binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
            holder.binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback)

            holder.binding.viewPager.setCurrentItem(model.data.selectedPagePosition, false)

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            holder.binding.viewPager,
                            autoScrollTimeInSec,
                            model.data.items.lastIndex
                        )
                    }
                }
            }
        }

        holder.binding.circleIndicator.setViewPager(holder.binding.viewPager)
        return holder
    }

    private suspend fun autoRotate(viewPager2: ViewPager2, delayInSec: Long, lastIndex: Int) {
        delay(TimeUnit.SECONDS.toMillis(delayInSec))
        val scrollToPosition =
            if (viewPager2.currentItem == lastIndex) 0 else viewPager2.currentItem + 1
        viewPager2.setCurrentItem(scrollToPosition, true)
        autoRotate(viewPager2, delayInSec, lastIndex)
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            carouselListWidgetData?.selectedPagePosition = position
        }
    }

    class CarouselListWidgetHolder(binding: WidgetCarouselListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCarouselListBinding>(binding, widget)


    class CarouselListWidgetModel : WidgetEntityModel<CarouselListWidgetData, WidgetAction>()

    @Parcelize
    @Keep
    data class CarouselListWidgetData(
        @SerializedName("_id") val id: String?,
        @SerializedName("parent_position") var parentPosition: Int?,
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<CarouselListItemData>,
        @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
        @SerializedName("full_width_cards") val fullWidthCards: Boolean = false,
        var selectedPagePosition: Int = 0
    ) : WidgetData(), Parcelable

    @Parcelize
    @Keep
    data class CarouselListItemData(
        @SerializedName("id") val id: String?,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("deeplink") val deeplink: String?
    ) : Parcelable
}

class CarouselListWidgetBannerFragment : Fragment(R.layout.item_widget_carousel_list) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    val data: CarouselListWidget.CarouselListWidgetData by lazy {
        requireArguments().getParcelable(
            KEY_DATA
        )!!
    }

    val parentPosition: Int by lazy {
        requireArguments().getInt(
            KEY_PARENT_POSITION
        )
    }

    val position: Int by lazy {
        requireArguments().getInt(
            KEY_POSITION
        )
    }

    val type: String by lazy {
        requireArguments().getString(
            KEY_TYPE
        ).orEmpty()
    }

    private val binding: ItemWidgetCarouselListBinding by viewBinding(ItemWidgetCarouselListBinding::bind)

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = data.items.getOrNull(position) ?: return

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.CAROUSEL_BANNER_VIEW,
                hashMapOf(
                    EventConstants.EVENT_NAME_ID to (item.id ?: data.id).orEmpty(),
                    EventConstants.ITEM_PARENT_POSITION to parentPosition,
                    EventConstants.ITEM_POSITION to position
                ), ignoreSnowplow = true
            )
        )

        binding.cardView.useCompatPadding = data.fullWidthCards.not()
        binding.cardView.radius = if (data.fullWidthCards) 0f else 8f.dpToPx()

        binding.ivImage.loadImage(item.imageUrl)
        binding.root.setOnClickListener {
            deeplinkAction.performAction(requireContext(), item.deeplink, type)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CAROUSEL_BANNER_CLICK,
                    hashMapOf(
                        EventConstants.EVENT_NAME_ID to (item.id ?: data.id).orEmpty(),
                        EventConstants.ITEM_PARENT_POSITION to parentPosition,
                        EventConstants.ITEM_POSITION to position
                    ), ignoreSnowplow = true
                )
            )
        }
    }

    companion object {
        private const val KEY_DATA = "data"
        private const val KEY_TYPE = "type"
        const val KEY_PARENT_POSITION = "parent_position"
        const val KEY_POSITION = "position"

        fun newInstance(
            data: CarouselListWidget.CarouselListWidgetData,
            type: String,
            parentPosition: Int,
            position: Int
        ) = CarouselListWidgetBannerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_DATA, data)
                putString(KEY_TYPE, type)
                putInt(KEY_PARENT_POSITION, parentPosition)
                putInt(KEY_POSITION, position)
            }
        }
    }

}