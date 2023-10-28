package com.doubtnut.referral.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.utils.applyBackgroundColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.loadImage2
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.databinding.ItemReferAndEarnStepBinding
import com.doubtnut.referral.databinding.WidgetReferAndEarnStepsBinding
import com.google.gson.annotations.SerializedName

class ReferAndEarnStepsWidget(context: Context) :
    CoreBindingWidget<ReferAndEarnStepsWidget.WidgetHolder,
            ReferAndEarnStepsWidget.WidgetModel, WidgetReferAndEarnStepsBinding>(context) {

    override fun getViewBinding(): WidgetReferAndEarnStepsBinding {
        return WidgetReferAndEarnStepsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data = model.data
        binding.rvReferralSteps.layoutManager = LinearLayoutManager(context)
        if (model.data.items != null) {
            binding.rvReferralSteps.adapter = Adapter(context, model.data.items!!)
        }
        binding.tvLabelHowItWorks.text = data.title.orEmpty()
        binding.tvLabelHowItWorks.applyTextSize(data.titleTextSize)
        binding.rootContainer.applyBackgroundColor(data.bgColor)

        return holder
    }

    class Adapter(val context: Context, val listStepsData: List<ReferAndEarnStepsData.StepData>) :
        RecyclerView.Adapter<Adapter.StepViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
            val binding = ItemReferAndEarnStepBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return StepViewHolder(binding)
        }

        override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
            holder.bind(listStepsData[position])
            if (position == listStepsData.size - 1) {
                holder.binding.imageDottedStrip.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return listStepsData.size
        }

        class StepViewHolder(val binding: ItemReferAndEarnStepBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(stepsData: ReferAndEarnStepsData.StepData) {
                binding.imageStep.loadImage2(stepsData.imageUrlStep)
                binding.tvTitle.text = stepsData.title.orEmpty()
                binding.tvSubtitle.text = stepsData.subtitle.orEmpty()
                stepsData.stepsCount?.let {
                    binding.tvStepCount.text = stepsData.stepsCount.toString()
                }
            }
        }
    }

    class WidgetHolder(binding: WidgetReferAndEarnStepsBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetReferAndEarnStepsBinding>(binding, widget)

    @Keep
    class WidgetModel : WidgetEntityModel<ReferAndEarnStepsData, WidgetAction>()

    @Keep
    data class ReferAndEarnStepsData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("items") val items: List<StepData>?
    ) : WidgetData() {

        data class StepData(
            @SerializedName("image_url") val imageUrlStep: String?,
            @SerializedName("title") val title: String?,
            @SerializedName("subtitle") val subtitle: String?,
            @SerializedName("steps_count") val stepsCount: Int?,
        )
    }


}