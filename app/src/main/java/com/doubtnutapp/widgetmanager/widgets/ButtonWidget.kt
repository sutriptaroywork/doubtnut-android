package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.ButtonWidgetClickEvent

import com.doubtnutapp.data.remote.models.ButtonWidgetModel
import com.doubtnutapp.databinding.WidgetButtonBinding

class ButtonWidget(
        context: Context)
    : BaseBindingWidget<ButtonWidget.ButtonWidgetHolder,
        ButtonWidgetModel, WidgetButtonBinding>(context) {

    override fun getViewBinding(): WidgetButtonBinding {
        return WidgetButtonBinding.inflate(LayoutInflater.from(context), this,true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
        this.widgetViewHolder = ButtonWidgetHolder(getViewBinding(),this)
    }

    override fun bindWidget(holder: ButtonWidgetHolder, model: ButtonWidgetModel): ButtonWidgetHolder {
        val binding = holder.binding
        binding.button.apply {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = model.data.horizontalBias ?: 0.5f
            }
            text = model.data.buttonText.orEmpty()
            isAllCaps = model.data.isAllCaps
            setTypeface(typeface, if (model.data.isBold) Typeface.BOLD else Typeface.NORMAL)

            model.data.bgColor?.let {
                background.setColorFilter(
                    Color.parseColor(it),
                    PorterDuff.Mode.MULTIPLY
                )
            }

            setOnClickListener {
                DoubtnutApp.INSTANCE.bus()?.send(ButtonWidgetClickEvent(model.action))
            }
        }

        return holder
    }

    class ButtonWidgetHolder(binding: WidgetButtonBinding, widget: BaseWidget<*, *>) :
    WidgetBindingVH<WidgetButtonBinding>(binding, widget)

}