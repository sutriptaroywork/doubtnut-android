package com.doubtnutapp.liveclass.adapter

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.data.remote.models.LiveClassPopUpItem
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//class LiveClassQuizAdapter(
//    val context: Context,
//    val actionPerformer: ActionPerformer
//) : RecyclerView.Adapter<LiveClassQuizViewHolder>() {
//
//    private val quizList = mutableListOf<LiveClassQuizQuestionData>()
//
//    private var timer: CountDownTimer? = null
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
//        LiveClassQuizViewHolder(
//            LayoutInflater.from(parent.context).inflate(
//                R.layout.widget_live_class_quiz, parent, false
//            )
//        )
//            .apply {
//                actionPerformer = this@LiveClassQuizAdapter.actionPerformer
//            }
//
//    override fun onBindViewHolder(holder: LiveClassQuizViewHolder, position: Int) {
//        holder.binding.group.show()
//        val quizData: LiveClassQuizQuestionData = quizList[position]
//        holder.binding.rvItems.layoutManager =
//            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//        holder.binding.rvItems.adapter = LiveClassQuizOptionsAdapter(
//            quizData.optionsList,
//            actionPerformer,
//            holder.adapterPosition
//        )
//        holder.binding.mathViewQuestion.text = quizData.question
//        timer?.cancel()
//        timer = object : CountDownTimer(1200, 1200) {
//            override fun onTick(millisUntilFinished: Long) {
//            }
//
//            override fun onFinish() {
//                holder.binding.group.hide()
//            }
//        }.start()
//    }
//
//    override fun getItemCount(): Int {
//        return quizList.size
//    }
//
//    fun setQuestionList(questionList: List<LiveClassQuizQuestionData>) {
//        quizList.clear()
//        quizList.addAll(questionList)
//        notifyDataSetChanged()
//    }
//}
//
//class LiveClassQuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//    lateinit var actionPerformer: ActionPerformer
//
//    val binding = WidgetLiveClassQuizBinding.bind(view)
//}

@Keep
@Parcelize
data class LiveClassQuestionResponse(
    @SerializedName("widgets") val widgets: List<Widgets>,
    @SerializedName("quiz_resource_id") val quizResourceId: Long = 0,
    @SerializedName("liveclass_resource_id") val liveClassResourceId: Long = 0,
    @SerializedName("live_at") val liveAt: Long? = 0
) : Parcelable

@Keep
@Parcelize
data class LiveClassQuestionDataList(
    @SerializedName("list") val list: MutableList<LiveClassQuizQuestionData?>,
    @SerializedName("quiz_resource_id") val quizResourceId: Long = 0,
    @SerializedName("liveclass_resource_id") val liveClassResourceId: Long = 0,
    @SerializedName("live_at") val liveAt: Long = 0
) : Parcelable

@Keep
@Parcelize
data class Widgets(@SerializedName("data") val data: LiveClassQuizQuestionData?) : Parcelable

@Keep
@Parcelize
data class LiveClassQuizQuestionData(
    @SerializedName("question") val question: String?,
    @SerializedName("quiz_question_id") val quizQuestionId: String?,
    @SerializedName("expiry") val expiry: String?,
    @SerializedName("response_expiry") val responseExpiry: String?,
    @SerializedName("answer") val answer: String?,
    @SerializedName("items") val optionsList: List<LiveClassQuizOptions>
) : Parcelable

@Keep
@Parcelize
data class LiveClassQuizOptions(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String?,
    @SerializedName("progress") val progress: Int?,
    @SerializedName("progress_color") val progressColour: String?,
    var isSelected: Boolean?
) : Parcelable

@Keep
@Parcelize
data class LiveClassPollsList(
    @SerializedName("list") val list: MutableList<LiveClassPollData?>,
    @SerializedName("detail_id") val detailId: Long = 0,
    override var liveAt: Long? = -1
) : Parcelable, LiveClassPopUpItem

@Keep
@Parcelize
data class PollWidgets(
    @SerializedName("data") val data: LiveClassPollData?,
    @SerializedName("publish_id") val publishId: Int?
) : Parcelable

@Keep
@Parcelize
data class LiveClassPollData(
    @SerializedName("question") val question: String?,
    @SerializedName("question_text_color") val questionTextColor: String?,
    @SerializedName("question_text_size") val questionTextSize: String?,
    var publishId: Int,
    @SerializedName("quiz_question_id") val quizQuestionId: String?,
    @SerializedName("expiry") val expiry: String?,
    @SerializedName("expiry_text_color") val expiryTextColor: String?,
    @SerializedName("expiry_text_size") val expiryTextSize: String?,
    @SerializedName("response_expiry") val responseExpiry: String?,
    @SerializedName("answer") val answer: String?,
    @SerializedName("show_close_btn") val showCloseBtn: Boolean?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("items") var optionsList: List<LiveClassPollOptions>
) : Parcelable

@Keep
@Parcelize
data class LiveClassPollOptions(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String?,
    @SerializedName("progress") var progress: Double?,
    var progressDisplay: String?,
    var isResultShown: Boolean = false,
    @SerializedName("progress_color") var progressColour: String?,
    var isSelected: Boolean?
) : Parcelable