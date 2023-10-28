package com.doubtnutapp.liveclass.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.data.remote.models.HomeWorkQuestion
import com.doubtnutapp.databinding.ItemHomeworkBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.similarVideo.ui.SimilarWidgetViewHolder
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgets.mathview.HomeWorkMathView
import com.moengage.inapp.internal.model.enums.WidgetType

class HomeWorkAdapter(
    private val homeWorkWebViewJavaInterface: HomeWorkMathView.HomeWorkWebViewJavaInterface?,
    private val deeplinkAction: DeeplinkAction,
    private val source: String? = null
) : RecyclerView.Adapter<BaseViewHolder<RecyclerViewItem>>() {

    private val questionList: ArrayList<RecyclerViewItem> = arrayListOf()
    private val widgetMap = hashMapOf<Int, String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<RecyclerViewItem> {
        return when {
            widgetMap.containsKey(viewType) -> {
                SimilarWidgetViewHolder(
                    WidgetFactory.createViewHolder(
                        context = parent.context,
                        parent = parent,
                        type = widgetMap[viewType]!!,
                        source = source
                    )!!
                ) as BaseViewHolder<RecyclerViewItem>
            }
            else -> {
                HomeWorkViewHolder(
                    ItemHomeworkBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), deeplinkAction, homeWorkWebViewJavaInterface
                ) as BaseViewHolder<RecyclerViewItem>
            }

        }
    }

    fun updateList(list: List<HomeWorkQuestion>) {
        val changeStartIndex = questionList.size
        list.forEach {
            //if the questionType is "widget"
            if (isWidget(it.questionType.orEmpty())) {
                it.widget?.let { WEM ->
                    questionList.add(SimilarWidgetViewItem(WEM))
                    widgetMap[WEM.type.hashCode()] = WEM.type
                }
            } else {
                questionList.add(it)
            }
        }
        notifyItemRangeInserted(changeStartIndex, questionList.size)
    }

    private fun isWidget(type: String): Boolean {
        if (type == SimilarWidgetViewItem.type) return true
        return false
    }

    fun clearList() {
        val size = questionList.size
        questionList.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemViewType(position: Int): Int {
        return questionList[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<RecyclerViewItem>, position: Int) {
        val data = questionList[position]
        holder.bind(data)
    }

    override fun getItemCount() = questionList.size
}


class HomeWorkViewHolder(
    val binding: ItemHomeworkBinding,
    val deeplinkAction: DeeplinkAction,
    private val homeWorkWebViewJavaInterface: HomeWorkMathView.HomeWorkWebViewJavaInterface?
) : BaseViewHolder<HomeWorkQuestion>(binding.root) {

    override fun bind(data: HomeWorkQuestion) {

        with(binding) {
            tvQuestionNo.text = data.questionNumberText

            viewSolutionTv.apply {
                setVisibleState(!data.solutionDeeplink.isNullOrEmpty())
                text = data.solutionText
                setOnClickListener {
                    deeplinkAction.performAction(context, data.solutionDeeplink)
                }
            }

            tvVideoSolution.apply {
                setVisibleState(!data.videoText.isNullOrEmpty())
                text = data.videoText
                setOnClickListener {
                    deeplinkAction.performAction(context, data.videoDeeplink)
                }
            }

            hwMathView.apply {
                if (homeWorkWebViewJavaInterface != null) {
                    setJavaInterfaceForHomeWork(homeWorkWebViewJavaInterface)
                }
                setPosition(position)
                question = data.question.orEmpty()
                data.type = if (data.questionType?.toInt() == 0) {
                    "SINGLE"
                } else {
                    "MULTI"
                }
                questionData = data
                afterPageLoaded {
                    if (questionData.isResult) {
                        val colorWhite = "#ffffff"
                        val colorBlack = "black"

                        val optionColors = arrayListOf<String>()
                        questionData.options?.forEachIndexed { index, option ->
                            when (option.key) {
                                questionData.answer -> {
                                    //Correct answer, this option can also be submitted
                                    optionColors.add("#3b8700")
                                    setOptionTextColor(index, colorWhite)
                                }
                                questionData.submittedOption -> {
                                    //Wrong answer submitted
                                    optionColors.add("#ff0000")
                                    setOptionTextColor(index, colorWhite)
                                }
                                else -> {
                                    //No answer submitted
                                    optionColors.add("#cccccc")
                                    setOptionTextColor(index, colorBlack)
                                }
                            }
                        }
                        onQuizSubmit(optionColors)
                    }
                }
                reload()
            }
        }
    }
}
