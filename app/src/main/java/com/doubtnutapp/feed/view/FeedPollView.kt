package com.doubtnutapp.feed.view

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PostUpdatedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.feed.FeedPollData
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.remote.models.feed.PollResult
import com.doubtnutapp.utils.showToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.view_feed_poll.view.*
import javax.inject.Inject

class FeedPollView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_feed_poll, this, true)
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val pollingOptionViews by lazy {
        arrayOf(
                radioButtonPollOption1,
                radioButtonPollOption2,
                radioButtonPollOption3,
                radioButtonPollOption4
        )
    }

    private val pollingResultViews by lazy {
        arrayOf(
                linearLayout_itemPolling_result1,
                linearLayout_itemPolling_result2,
                linearLayout_itemPolling_result3,
                linearLayout_itemPolling_result4)
    }

    private val pollingResultProgressViews by lazy {
        arrayOf(
                progressBar_itemPolling_result1,
                progressBar_itemPolling_result2,
                progressBar_itemPolling_result3,
                progressBar_itemPolling_result4)
    }


    private val pollingResultTextViews by lazy {
        arrayOf(
                textView_itemPolling_result1,
                textView_itemPolling_result2,
                textView_itemPolling_result3,
                textView_itemPolling_result4)
    }

    private val pollingResultTextPercentViews by lazy {
        arrayOf(
                textView_itemPolling_resultPercent1,
                textView_itemPolling_resultPercent2,
                textView_itemPolling_resultPercent3,
                textView_itemPolling_resultPercent4)
    }

    private val MAX_POLL_OPTIONS = 4
    private val INVALID_OPTION = -1

    fun bindData(feedPostItem: FeedPostItem) {
        if (feedPostItem.pollData == null) return

        val pollData = feedPostItem.pollData
        tvPollCount.text = String.format(context.getString(R.string.string_itemtimeline_finalResult),
                pollData!!.totalPolledCount)

        if (pollData.isPolled.toBoolean()) {
            viewPollResults.show()
            viewPollSubmit.hide()
            setPollResults(pollData)
        } else {
            viewPollSubmit.show()
            viewPollResults.hide()
            setPollQuestion(pollData)
        }

        btnPollSubmit.setOnClickListener {
            val selectedOptionId = getOptionId()
            if (selectedOptionId == INVALID_OPTION) {
                ToastUtils.makeText(context, R.string.string_timelineFeed_chooseAnOption, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DataHandler.INSTANCE.teslaRepository
                    .postPollAction(getPollRequestBody(pollData.pollId.toString(), selectedOptionId).toRequestBody())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe({
                        pollSubmitted(feedPostItem, it.data.result)
                    }, {
                        showToast(context, "Error submitting poll")
                    })

            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_FEED_POLL_SUBMIT, ignoreSnowplow = true))
        }

    }

    private fun setPollQuestion(pollData: FeedPollData) {
        for (options in pollData.options) {
            pollingOptionViews[pollData.options.indexOf(options)].also {
                it.text = options
                it.visibility = View.VISIBLE
            }
        }

        for (index in pollData.options.size until MAX_POLL_OPTIONS) {
            pollingOptionViews[index].also {
                it.visibility = View.GONE
            }
        }
    }

    private fun setPollResults(pollData: FeedPollData) {
        var maxValue = 0
        var progressBar: ProgressBar? = null

        for (index in pollData.results.indices) {

            val result = pollData.results[index]
            pollingResultViews[index]?.apply {
                visibility = View.VISIBLE
            }

            pollingResultProgressViews[index]?.apply {
                progress = result.value
            }

            pollingResultTextPercentViews[index]?.apply {
                text = StringBuilder().append(result.value.toString()).append("%")
                visibility = View.VISIBLE
            }

            pollingResultTextViews[index]?.apply {
                text = result.option
            }

            if (result.value > maxValue) {
                maxValue = result.value
                progressBar = pollingResultProgressViews[index]
            }
        }

        for (index in pollData.results.size until MAX_POLL_OPTIONS) {
            pollingResultViews[index]?.apply {
                visibility = View.GONE
            }
            pollingResultTextPercentViews[index]?.apply {
                visibility = View.GONE
            }
        }

        changeWinningViewColor(progressBar)
    }

    private fun pollSubmitted(feedItem: FeedPostItem, results: List<PollResult>) {
        val pollData = feedItem.pollData!!
        feedItem.pollData = pollData.copy(pollId = pollData.pollId, options = pollData.options,
                results = results, isPolled = 1, totalPolledCount = pollData.totalPolledCount + 1)
        DoubtnutApp.INSTANCE.bus()?.send(PostUpdatedEvent(feedItem))
    }

    private fun getPollRequestBody(id: String, optionId: Int): HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            this["poll_id"] = id
            this["option_id"] = optionId.toString()
        }
    }

    private fun getOptionId(): Int {
        if (radioGroupPoll.checkedRadioButtonId == -1) {
            return INVALID_OPTION
        }
        return pollingOptionViews.indexOf<RadioButton>(findViewById(radioGroupPoll.checkedRadioButtonId))
    }

    /**
     * Change color of ProgressBar of Winning option.
     */
    private fun changeWinningViewColor(progressBar: ProgressBar?) {
        progressBar?.also {

            val layerDrawable = ContextCompat.getDrawable(context, R.drawable.drawable_progressbar_custom) as LayerDrawable

            layerDrawable
                    .findDrawableByLayerId(android.R.id.progress)
                    .setColorFilter(
                            ContextCompat.getColor(context, R.color.colorAccent),
                            PorterDuff.Mode.SRC_IN
                    )

            it.progressDrawable = layerDrawable
        }

    }
}