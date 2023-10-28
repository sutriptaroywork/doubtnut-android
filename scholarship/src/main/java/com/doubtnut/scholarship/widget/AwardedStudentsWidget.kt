package com.doubtnut.scholarship.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.ItemAwardStudentsBinding
import com.doubtnut.scholarship.databinding.WidgetAwardStudentsBinding
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AwardedStudentsWidget(
    context: Context
) : CoreBindingWidget<AwardedStudentsWidget.WidgetHolder, AwardedStudentsWidgetModel, WidgetAwardStudentsBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    private var autoScrollJob: Job? = null
    private var data: AwardedStudentsWidgetData? = null

    var isDragging = false
    var isAutoRotated = false

    override fun getViewBinding(): WidgetAwardStudentsBinding {
        return WidgetAwardStudentsBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: AwardedStudentsWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        data = model.data

        binding.root.applyBackgroundColor(model.data.backgroundColor)

        binding.tvWidgetName.isVisible = model.data.title.isNullOrEmpty().not()
        binding.tvWidgetName.text = model.data.title

        (context as? AppCompatActivity)?.let { appCompatActivity ->
            binding.rvAwardedStudents.removeItemDecorations2()

            binding.rvAwardedStudents.addOnItemTouchListener(object :
                RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isDragging = true
                    }
                    return super.onInterceptTouchEvent(rv, e)
                }
            })

            binding.rvAwardedStudents.layoutManager = LinearLayoutManagerWithSmoothScroller(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            binding.rvAwardedStudents.adapter = AwardedStudentsAdapter(
                context,
                model.data.items.orEmpty(),
                analyticsPublisher,
                deeplinkAction
            )

            binding.rvAwardedStudents.smoothScrollToPosition(model.data.selectedPagePosition)

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            binding.rvAwardedStudents,
                            autoScrollTimeInSec,
                            model.data.items?.lastIndex ?: return@launchWhenResumed
                        )
                    }
                }
            }
        }

        return holder
    }

    private suspend fun autoRotate(
        recyclerView: RecyclerView,
        delayInSec: Long,
        lastIndex: Int
    ) {
        if (isDragging) {
            isDragging = false
            delay(delayInSec)
        } else {
            if (isAutoRotated) {
                delay(TimeUnit.SECONDS.toMillis(delayInSec))
            }
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
            val scrollToPosition =
                if (visibleItemPosition == lastIndex) 0 else visibleItemPosition + 1
            recyclerView.smoothScrollToPosition(scrollToPosition)
            data?.selectedPagePosition = scrollToPosition
            isAutoRotated = true
        }
        autoRotate(recyclerView, delayInSec, lastIndex)
    }

    class WidgetHolder(binding: WidgetAwardStudentsBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetAwardStudentsBinding>(binding, widget)

    companion object {
        const val TAG = "AwardedStudentsWidget"
        const val EVENT_TAG = "awarded_students_widget"
    }
}

class AwardedStudentsAdapter(
    val context: Context,
    val items: List<AwardedStudentsWidgetItem>,
    val analyticsPublisher: IAnalyticsPublisher,
    val deeplinkAction: IDeeplinkAction
) :
    RecyclerView.Adapter<AwardedStudentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAwardStudentsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ViewUtils.setWidthBasedOnPercentage(context, holder.binding.root, "2.5", R.dimen.spacing_8)
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemAwardStudentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            items[bindingAdapterPosition].let { item ->
                binding.root.applyBackgroundColor(item.backgroundColor)

                binding.ivMain.loadImage2(
                    url = item.profileImage,
                    placeholder = R.drawable.ic_profile_placeholder
                )

                item.highlightColor?.let {
                    binding.ivBadgeOne.setImageDrawable(ColorDrawable(Color.parseColor(it)))
                }

                binding.rank.text = item.rank
                binding.rank.applyTextSize(item.rankTextSize)
                binding.rank.applyTextColor(item.rankTextColor)

                binding.studentName.text = item.name
                binding.studentName.applyTextSize(item.nameTextSize)
                binding.studentName.applyTextColor(item.nameTextSize)
                binding.studentName.applyBackgroundTint(item.highlightColor)

                binding.scholarshipGiven.text = item.scholarshipGranted
                binding.scholarshipGiven.applyTextColor(item.scholarshipGrantedTextColor)
                binding.scholarshipGiven.applyTextSize(item.scholarshipGrantedTextSize)

                binding.examTargeted.text = item.examTargeted
                binding.examTargeted.applyTextColor(item.examTargetedTextColor)
                binding.examTargeted.applyTextSize(item.examTargetedTextSize)

                binding.scholarshipTestGiven.text = item.scholarshipTest
                binding.scholarshipTestGiven.applyTextColor(item.scholarshipTestTextColor)
                binding.scholarshipTestGiven.applyTextSize(item.scholarshipTestTextSize)

                if (item.deeplink.isNullOrEmpty()) {
                    binding.root.applyRippleColor("#00000000")
                } else {
                    binding.root.rippleColor = ColorStateList.valueOf(
                        MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
                    )
                }

                binding.root.setOnClickListener {
                    if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                    deeplinkAction.performAction(context, item.deeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${AwardedStudentsWidget.EVENT_TAG}_${EventConstants.CARD_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to AwardedStudentsWidget.TAG,
                                EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                            ).apply {
                                putAll(item.extraParams.orEmpty())
                            }
                        )
                    )
                }
            }
        }
    }
}

@Keep
class AwardedStudentsWidgetModel :
    WidgetEntityModel<AwardedStudentsWidgetData, WidgetAction>()

@Keep
data class AwardedStudentsWidgetData(
    @SerializedName("title")
    var title: String?,
    @SerializedName("background_color", alternate = ["bg_color"])
    var backgroundColor: String?,
    @SerializedName("items")
    val items: List<AwardedStudentsWidgetItem>?,
    @SerializedName("auto_scroll_time_in_sec")
    val autoScrollTimeInSec: Long?,
    var selectedPagePosition: Int = 0
) : WidgetData()

@Keep
data class AwardedStudentsWidgetItem(

    @SerializedName("rank")
    val rank: String?,
    @SerializedName("rank_text_color")
    val rankTextColor: String?,
    @SerializedName("rank_text_size")
    val rankTextSize: String?,

    @SerializedName("highlight_color")
    val highlightColor: String?,
    @SerializedName("profile_image")
    val profileImage: String?,

    @SerializedName("name")
    val name: String?,
    @SerializedName("name_text_size")
    val nameTextSize: String?,
    @SerializedName("name_text_color")
    val nameTextColor: String?,

    @SerializedName("scholarship_granted")
    val scholarshipGranted: String?,
    @SerializedName("scholarship_granted_text_size")
    val scholarshipGrantedTextSize: String?,
    @SerializedName("scholarship_granted_text_color")
    val scholarshipGrantedTextColor: String?,

    @SerializedName("exam_targeted")
    val examTargeted: String?,
    @SerializedName("exam_targeted_text_size")
    val examTargetedTextSize: String?,
    @SerializedName("exam_targeted_text_color")
    val examTargetedTextColor: String?,

    @SerializedName("scholarship_test")
    val scholarshipTest: String?,
    @SerializedName("scholarship_test_text_size")
    val scholarshipTestTextSize: String?,
    @SerializedName("scholarship_test_text_color")
    val scholarshipTestTextColor: String?,

    @SerializedName("background_color", alternate = ["bg_color"])
    val backgroundColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?,
) : WidgetData()
