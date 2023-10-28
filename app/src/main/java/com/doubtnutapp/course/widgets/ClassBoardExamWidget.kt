package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.RemoveWidget
import com.doubtnutapp.databinding.WidgetClassBoardExamBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 04/01/21.
 */
class ClassBoardExamWidget(context: Context) : BaseBindingWidget<ClassBoardExamWidget.WidgetHolder,
    ClassBoardExamWidget.Model, WidgetClassBoardExamBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetClassBoardExamBinding {
        return WidgetClassBoardExamBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        setMargins(
            WidgetLayoutConfig(
                marginTop = 0,
                marginBottom = 0,
                marginLeft = 0,
                marginRight = 0
            )
        )

        val data: Data = model.data
        trackingViewId = data.id

        holder.binding.apply {
            Glide.with(context).load(data.bgImage)
                .apply(RequestOptions().fitCenter()).into(
                    object : CustomTarget<Drawable>(360, 66) {
                        override fun onLoadCleared(placeholder: Drawable?) {
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                        ) {
                            classWidgetContainer.background = resource
                        }
                    }
                )

            tvTitle.text = data.title

            data.userClass?.let { userClass ->
                classLayout.show()
                ivClass.loadImage(userClass.image)
                tvClass.text = userClass.title
            } ?: classLayout.hide()

            data.userExam?.let { userExam ->
                examLayout.show()
                ivExam.loadImage(userExam.image)
                tvExam.text = userExam.title
            } ?: examLayout.hide()

            data.userBoard?.let { userBoard ->
                boardLayout.show()
                ivBoard.loadImage(userBoard.image)
                tvBoard.text = userBoard.title
            } ?: boardLayout.hide()

            ivClose.setOnClickListener {
                defaultPrefs().edit {
                    putBoolean(Constants.REMOVE_CLASS_EXAM_BOARD_WIDGET, true)
                }
                actionPerformer?.performAction(RemoveWidget(widgetEntityModel))
            }

            classWidgetContainer.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink, source.orEmpty())
            }
        }

        return holder
    }

    class WidgetHolder(binding: WidgetClassBoardExamBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetClassBoardExamBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("bg_image") val bgImage: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("user_class") val userClass: UserClass?,
        @SerializedName("user_board") val userBoard: UserBoard?,
        @SerializedName("user_exam") val userExam: UserExam?
    ) : WidgetData()

    @Keep
    data class UserClass(
        @SerializedName("image") var image: String,
        @SerializedName("title") var title: String,
    )

    @Keep
    data class UserExam(
        @SerializedName("image") var image: String,
        @SerializedName("title") var title: String,
    )

    @Keep
    data class UserBoard(
        @SerializedName("image") var image: String,
        @SerializedName("title") var title: String,
    )
}
