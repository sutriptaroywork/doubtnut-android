package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SelectPlan
import com.doubtnutapp.databinding.ItemChoosePlanBinding
import com.doubtnutapp.databinding.ItemChoosePlanEmiBinding
import com.doubtnutapp.databinding.WidgetPlanListBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.ImageTextView
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 02/08/20.
 */
class PlanListWidget(context: Context) : BaseBindingWidget<PlanListWidget.WidgetHolder,
    PlanListWidgetModel, WidgetPlanListBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetPlanListBinding {
        return WidgetPlanListBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: PlanListWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(WidgetLayoutConfig(0, 0, 0, 0))
        val binding = holder.binding
        val data: PlanListWidgetData = model.data
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        binding.recyclerView.adapter = Adapter(data.items.orEmpty(), actionPerformer)
        return holder
    }

    class Adapter(
        val items: List<PackageInfo>,
        val actionPerformer: ActionPerformer? = null
    ) : RecyclerView.Adapter<BaseViewHolder<PackageInfo>>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolder<PackageInfo> {
            return if (viewType == 1) {
                EmiPlanViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_choose_plan_emi,
                        parent, false
                    )
                ).also {
                    it.actionPerformer = actionPerformer
                }
            } else {
                PlanViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_choose_plan,
                        parent, false
                    )
                ).also {
                    it.actionPerformer = actionPerformer
                }
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder<PackageInfo>, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int = if (items[position].viewType == "emi") {
            1
        } else {
            2
        }
    }

    class WidgetHolder(binding: WidgetPlanListBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetPlanListBinding>(binding, widget)
}

class PlanViewHolder(itemView: View) : BaseViewHolder<PackageInfo>(itemView) {

    val binding = ItemChoosePlanBinding.bind(itemView)

    override fun bind(data: PackageInfo) {
        binding.textViewDuration.text = data.duration
        binding.textViewPlanOfferAmount.text = "/₹" + data.offerAmount
        val actualAmountTextCurrent = data.originalAmount
        binding.textViewPlanActualAmount.text = "₹$actualAmountTextCurrent"
        binding.textViewPlanActualAmount.paintFlags =
            binding.textViewPlanActualAmount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        if (data.selected == true) {
            binding.imageViewSelection.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_tick_vip)
            )
        } else {
            binding.imageViewSelection.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_vip_non_selected)
            )
        }

        itemView.setOnClickListener {
            if (data.selected == true) return@setOnClickListener
            performAction(SelectPlan(data))
        }
    }
}

class EmiPlanViewHolder(itemView: View) : BaseViewHolder<PackageInfo>(itemView) {

    val binding = ItemChoosePlanEmiBinding.bind(itemView)

    override fun bind(data: PackageInfo) {
        setSpan(data)
        val actualAmountText = "/₹" + data.originalAmount
        binding.textViewActualAmount.text = actualAmountText
        binding.textViewActualAmount.paintFlags =
            binding.textViewOfferAmount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val offerAmountText = data.subTitle1.orEmpty() + " ₹ " + data.offerAmount
        binding.textViewOfferAmount.text = offerAmountText
        binding.textViewEmiOptionTitle.text = data.subTitle2.orEmpty()
        binding.layoutEmiOption.removeAllViews()
        data.emiOptions?.forEach {
            val textView = ImageTextView(itemView.context)
            when {
                it.isCompleted -> {
                    textView.setViews(it.text, R.drawable.ic_completed_emi, null)
                }
                it.selectedToBuy == true -> {
                    textView.setViews(it.text, R.drawable.ic_selected_emi, it.suffixText)
                }
                else -> {
                    textView.setViews(it.text, R.drawable.ic_emi_non_selected, null)
                }
            }
            binding.layoutEmiOption.addView(textView)
        }

        if (data.bottomTitle.isNullOrBlank()) {
            binding.textViewBottomTitle.text = ""
            binding.textViewBottomTitle.hide()
        } else {
            binding.textViewBottomTitle.text = data.bottomTitle
            binding.textViewBottomTitle.show()
        }

        itemView.setOnClickListener {
            data.emiOptions?.firstOrNull { option -> !option.isCompleted }
                ?: return@setOnClickListener
            performAction(SelectPlan(data))
        }
    }

    private fun setSpan(data: PackageInfo) {
        val text1 = data.name.orEmpty() + " "
        val text2 = data.title1.orEmpty() + " "
        val text3 = data.downPayment.orEmpty() + " "
        val text4 = data.title2.orEmpty()
        val completeText = text1 + text2 + text3 + text4
        val builder = SpannableStringBuilder(completeText)

        val firstIndexText1 = builder.toString().indexOf(text1)
        val lastIndexText1 = firstIndexText1 + text1.length

        val firstIndexText2 = builder.toString().indexOf(text3)
        val lastIndexText2 = firstIndexText2 + text3.length

        val span1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(itemView.context, R.color.tomato)
            }
        }

        val span2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(itemView.context, R.color.tomato)
            }
        }
        builder.setSpan(span1, firstIndexText1, lastIndexText1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(span2, firstIndexText2, lastIndexText2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textViewName.text = builder
        binding.textViewName.movementMethod = LinkMovementMethod.getInstance()
    }
}

class PlanListWidgetModel : WidgetEntityModel<PlanListWidgetData, WidgetAction>()

@Keep
data class PlanListWidgetData(
    @SerializedName("items") val items: List<PackageInfo>?,
    @SerializedName("scroll_direction") val scrollDirection: String?
) : WidgetData()

@Keep
data class PackageInfo(
    @SerializedName("id") val id: String,
    @SerializedName("original_amount") val originalAmount: String?,
    @SerializedName("offer_amount") val offerAmount: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("off") val off: String?,
    @SerializedName("selected") var selected: Boolean?,
    @SerializedName("view_type") var viewType: String,
    @SerializedName("name") var name: String?,
    @SerializedName("title1") var title1: String?,
    @SerializedName("title2") var title2: String?,
    @SerializedName("sub_title1") var subTitle1: String?,
    @SerializedName("sub_title2") var subTitle2: String?,
    @SerializedName("downpayment") var downPayment: String?,
    @SerializedName("bottom_title") var bottomTitle: String?,
    @SerializedName("emi_options") var emiOptions: List<EmiOption>?
)

@Keep
data class EmiOption(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("text1") val suffixText: String?,
    @SerializedName("amount") val amount: String,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("selected_to_buy") var selectedToBuy: Boolean?
)
