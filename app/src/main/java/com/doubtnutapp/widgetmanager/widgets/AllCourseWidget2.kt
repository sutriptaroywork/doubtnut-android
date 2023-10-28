package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.data.remote.models.AllCourseWidget2Data
import com.doubtnutapp.data.remote.models.AllCourseWidget2Item
import com.doubtnutapp.data.remote.models.AllCourseWidget2Model
import com.doubtnutapp.databinding.ItemAllCourse2Binding
import com.doubtnutapp.databinding.WidgetAllCourse2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import javax.inject.Inject


class AllCourseWidget2(context: Context) : BaseBindingWidget<AllCourseWidget2.WidgetHolder,
        AllCourseWidget2Model, WidgetAllCourse2Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetAllCourse2Binding {
        return WidgetAllCourse2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: AllCourseWidget2Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data: AllCourseWidget2Data = model.data
        holder.binding.textViewTitleMain.text = data.title.orEmpty()
        holder.binding.textViewViewAll.text = data.linkText.orEmpty()
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL, false
        )
        holder.binding.recyclerView.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            source
        )
        return holder
    }

    class Adapter(
        val items: List<AllCourseWidget2Item>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val source: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemAllCourse2Binding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item: AllCourseWidget2Item = items[position]
            holder.binding.imageViewBackground.loadImageEtx(item.imageBg.orEmpty())
            holder.binding.imageViewIcon.loadImageEtx(item.iconUrl.orEmpty())
            holder.binding.textViewTitle.text = item.title.orEmpty()
            holder.binding.textViewSubtitle.text = item.subTitle.orEmpty()
            holder.binding.textViewDescription.text = item.description.orEmpty()
            holder.binding.textViewCourseType.text = item.courseType.orEmpty()
            holder.binding.view.setBackgroundColor(Utils.parseColor(items[position].imageBarColor))
            holder.binding.imageViewOne.loadImageEtx(
                item.facultyImageUrlList?.getOrNull(0).orEmpty()
            )
            holder.binding.imageViewTwo.loadImageEtx(
                item.facultyImageUrlList?.getOrNull(1).orEmpty()
            )
            holder.binding.imageViewThree.loadImageEtx(
                item.facultyImageUrlList?.getOrNull(2).orEmpty()
            )

            holder.binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.ALL_COURSE_ITEM_CLICK,
                        hashMapOf(
                            EventConstants.EVENT_NAME_ID to item.id.toString(),
                            EventConstants.WIDGET to "AllCourseWidget2"
                        )
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, item.deeplink)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemAllCourse2Binding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetAllCourse2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetAllCourse2Binding>(binding, widget)

}