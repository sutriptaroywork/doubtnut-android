package com.doubtnutapp.similarVideo.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.databinding.WidgetCollapseExpandMathviewBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlin.math.ceil
import kotlin.math.floor

class CollapseExpandMathViewWidget(context: Context)
    : BaseBindingWidget<CollapseExpandMathViewWidget.WidgetHolder,
        CollapseExpandMathViewWidgetModel,WidgetCollapseExpandMathviewBinding>(context) {

    override fun getViewBinding(): WidgetCollapseExpandMathviewBinding {
        return WidgetCollapseExpandMathviewBinding.inflate(LayoutInflater.from(context),  this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CollapseExpandMathViewWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        holder.binding.mathView.text = data.questionId + " : " +
                if (data.ocrText.contains("<math")) data.question
                else data.ocrText

        holder.binding.ivExpand.setOnClickListener {
            if (holder.binding.mathView.height == ceil(this.resources.getDimension(R.dimen.video_question_minimum_height).toDouble()).toInt()
                    || holder.binding.mathView.height == floor(this.resources.getDimension(R.dimen.video_question_minimum_height).toDouble()).toInt()) {
                toggleMathView(true,holder)
            } else {
                toggleMathView(false,holder)
            }
        }

        return holder
    }

    private fun toggleMathView(toExpand: Boolean, holder : CollapseExpandMathViewWidget.WidgetHolder) {
        val width = holder.binding.mathView.width
        if (toExpand) {
            holder.binding.mathView.layoutParams = ConstraintLayout.LayoutParams(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            holder.binding.ivExpand.setImageResource(R.drawable.ic_expand_less)
        } else {
            holder.binding.mathView.layoutParams = ConstraintLayout.LayoutParams(width, context.resources.getDimension(R.dimen.video_question_minimum_height).toInt())
            holder.binding.ivExpand.setImageResource(R.drawable.ic_expand_more)
        }
    }

    class WidgetHolder(binding: WidgetCollapseExpandMathviewBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCollapseExpandMathviewBinding>(binding, widget)

}

class CollapseExpandMathViewWidgetModel : WidgetEntityModel<CollapseExpandMathViewWidgetData, WidgetAction>()

@Keep
data class CollapseExpandMathViewWidgetData(
        @SerializedName("id") val id: String,
        @SerializedName("ocr_text") val ocrText: String,
        @SerializedName("question_id") val questionId: String,
        @SerializedName("question") val question: String
) : WidgetData()

