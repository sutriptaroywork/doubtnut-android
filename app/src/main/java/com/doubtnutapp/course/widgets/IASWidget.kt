package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.Keep
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.CourseFragmentCloseClicked
import com.doubtnutapp.databinding.WidgetIasBinding
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFragment
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 08/04/22.
 */
class IASWidget(context: Context) : BaseBindingWidget<IASWidget.WidgetHolder,
        IASWidget.WidgetModel, WidgetIasBinding>(context) {

    companion object {
        const val TAG = "IASWidget"
    }

    var source: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetIasBinding {
        return WidgetIasBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: WidgetModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                    marginTop = 0,
                    marginBottom = 0,
                    marginLeft = 0,
                    marginRight = 0
                )
            }
        )

        analyticsPublisher.publishEvent(AnalyticsEvent(TAG, model.extraParams ?: hashMapOf()))
        val data: IASData = model.data
        val binding = holder.binding

        binding.apply {
            ivBack.setVisibleState(data.showBackIcon ?: false)
            ivBack.setOnClickListener { actionPerformer?.performAction(CourseFragmentCloseClicked()) }

            globalSearch.text =
                data.title ?: context.getString(R.string.search_for_subjects_books_topics_or_pdfs)
            globalSearch.setOnClickListener {}

            globalSearch.setOnTouchListener { v, event ->
                val drawableRightIndex = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableRight = globalSearch.compoundDrawables[drawableRightIndex]
                    if (event.rawX >= (globalSearch.right - (drawableRight?.bounds?.width() ?: 0))
                    ) {
                        openSearchActivity(true, data.source)
                    } else if (event.rawX < (globalSearch.right - (drawableRight?.bounds?.width()
                            ?: 0))
                    ) {
                        openSearchActivity(false, data.source)
                    }
                }
                return@setOnTouchListener false
            }
        }

        return holder
    }

    private fun openSearchActivity(startVoiceSearch: Boolean, source: String?) {
        Utils.executeIfContextNotNull(context) { context: Context ->
            InAppSearchActivity.startActivity(
                context = context,
                source = source ?: CourseFragment.TAG,
                startVoiceSearch = startVoiceSearch,
                selectedClass = null
            )
        }
    }

    class WidgetHolder(binding: WidgetIasBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetIasBinding>(binding, widget)

    class WidgetModel : WidgetEntityModel<IASData, WidgetAction>()

    @Keep
    data class IASData(
        @SerializedName("source") val source: String?,
        @SerializedName("show_back_icon") val showBackIcon: Boolean?,
        @SerializedName("title") val title: String?
    ) : WidgetData()

}
