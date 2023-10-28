package com.doubtnutapp.freeclasses.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemClassesByTeacherBinding
import com.doubtnutapp.databinding.WidgetClassesByTeacherBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadImage
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class ClassesByTeacherWidget(context: Context) :
    BaseBindingWidget<ClassesByTeacherWidget.WidgetViewHolder,
            ClassesByTeacherWidget.ClassesByTeacherWidgetModel, WidgetClassesByTeacherBinding>(
        context
    ) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "ClassesByTeacherWidget"
        const val EVENT_TAG = "classes_by_teacher_widget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetClassesByTeacherBinding {
        return WidgetClassesByTeacherBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: ClassesByTeacherWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvTitle.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvTitle.text = model.data.title
        binding.tvTitle.applyTextSize(model.data.titleTextSize)
        binding.tvTitle.applyTextColor(model.data.titleTextColor)

        binding.rvMain.adapter = Adapter(
            model.data.items.orEmpty()
        )
        return holder
    }

    inner class Adapter(
        val items: List<ClassesByTeacherWidgetItem>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemClassesByTeacherBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val binding = holder.binding
            val item = items[position]
            with(binding) {
                Utils.setWidthBasedOnPercentage(
                    binding.root.context,
                    binding.root,
                    "2.75",
                    R.dimen.spacing
                )

                card.applyBackgroundColor(item.imageBgColor)
                ivImage.loadImage(item.imageUrl)

                tvTitleOne.text = item.title
                tvTitleOne.applyTextSize(item.titleTextSize)
                tvTitleOne.applyTextColor(item.titleTextColor)

                tvTitleTwo.text = item.titleTwo
                tvTitleTwo.applyTextSize(item.titleTwoTextSize)
                tvTitleTwo.applyTextColor(item.titleTwoTextColor)

                tvItemTitleOne.apply {
                    text = item.subItems?.getOrNull(0)?.title
                    applyTextSize(item.subItems?.getOrNull(0)?.titleTextSize)
                    applyTextColor(item.subItems?.getOrNull(0)?.titleTextColor)
                }

                tvItemTitleTwo.apply {
                    text = item.subItems?.getOrNull(1)?.title
                    applyTextSize(item.subItems?.getOrNull(1)?.titleTextSize)
                    applyTextColor(item.subItems?.getOrNull(1)?.titleTextColor)
                }

                tvItemTitleThree.apply {
                    text = item.subItems?.getOrNull(2)?.title
                    applyTextSize(item.subItems?.getOrNull(2)?.titleTextSize)
                    applyTextColor(item.subItems?.getOrNull(2)?.titleTextColor)
                }

                tvItemTitleFour.apply {
                    text = item.subItems?.getOrNull(3)?.title
                    applyTextSize(item.subItems?.getOrNull(3)?.titleTextSize)
                    applyTextColor(item.subItems?.getOrNull(3)?.titleTextColor)
                }

                binding.root.setOnClickListener {
                    deeplinkAction.performAction(context, item.deeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to TAG,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId()
                            ).apply {
                                putAll(item.extraParams.orEmpty())
                            }
                        )
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(val binding: ItemClassesByTeacherBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetViewHolder(binding: WidgetClassesByTeacherBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetClassesByTeacherBinding>(binding, widget)

    class ClassesByTeacherWidgetModel :
        WidgetEntityModel<ClassesByTeacherWidgetData, WidgetAction>()

    @Keep
    data class ClassesByTeacherWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("items") val items: List<ClassesByTeacherWidgetItem>?,
    ) : WidgetData()

    @Keep
    data class ClassesByTeacherWidgetItem(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("title_two", alternate = ["bottom_title"]) val titleTwo: String?,
        @SerializedName(
            "title_two_text_size",
            alternate = ["bottom_title_text_size"]
        ) val titleTwoTextSize: String?,
        @SerializedName(
            "title_two_text_color",
            alternate = ["bottom_title_text_color"]
        ) val titleTwoTextColor: String?,

        @SerializedName("image_bg_color", alternate = ["bg_color"]) val imageBgColor: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("items") val subItems: List<ClassesByTeacherWidgetSubItem>?,
        @SerializedName("extra_params") var extraParams: HashMap<String, Any>? = null

    ) : WidgetData()

    @Keep
    data class ClassesByTeacherWidgetSubItem(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_color") val titleTextColor: String?
    ) : WidgetData()


}