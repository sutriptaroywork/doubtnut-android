package com.doubtnutapp.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.LibraryExamWidgetClick
import com.doubtnutapp.databinding.WidgetLibraryExamBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImage
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName

class LibraryExamWidget(context: Context) : BaseBindingWidget<LibraryExamWidget.WidgetHolder,
        LibraryExamWidget.Model, WidgetLibraryExamBinding>(context) {

    var source: String? = null

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding

        holder.itemView.apply {
            binding.tvExam.text = data.title
            binding.examCheckbox.isChecked = data.isChecked == true
            binding.ivIcon.loadImage(data.imageUrl)
            setOnClickListener {
                binding.examCheckbox.isChecked = binding.examCheckbox.isChecked.not()
            }
            binding.examCheckbox.setOnCheckedChangeListener { _, isChecked ->
                actionPerformer?.performAction(LibraryExamWidgetClick(data.id, isChecked))
            }
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetLibraryExamBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetLibraryExamBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("title") val title: String,
        @SerializedName("is_checked") var isChecked: Boolean?
    ) : WidgetData()

    override fun getViewBinding(): WidgetLibraryExamBinding {
        return WidgetLibraryExamBinding.inflate(LayoutInflater.from(context), this, true)
    }
}

