package com.doubtnutapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.util.Linkify
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnutapp.utils.MathUtils
import com.doubtnutapp.utils.Utils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.resources.TextAppearance
import com.instacart.library.truetime.TrueTimeRx
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

@BindingAdapter("addTags")
fun addVideoTags(view: ChipGroup, tags: List<String>?) {
    tags?.let {
        for (item in tags) {
            val tagView = Chip(view.context, null, R.style.Widget_MaterialComponents_Chip_Choice)
            tagView.text = item
            tagView.isChipIconEnabled = false
            tagView.setTextAppearance(TextAppearance(view.context, R.style.TextAppearance_Body2))
            tagView.chipBackgroundColor =
                ColorStateList.valueOf(view.context.resources.getColor(R.color.buttonColor))
            view.addView(tagView)
        }
    }
}

@BindingAdapter("app:hideIfEmpty")
fun hideIfEmpty(view: View, content: String?) {
    if (TextUtils.isEmpty(content)) view.hide() else view.show()
}

@BindingAdapter("app:hideIfNull")
fun hideIfNull(view: View, any: Any?) {
    if (any != null) view.hide() else view.show()
}

@BindingAdapter(value = ["app:imageurl", "app:placeholder"], requireAll = false)
fun loadImage(view: ImageView, image: String?, placeholder: Drawable?) {
    val imageUrl = image ?: ""
    Glide.with(view.context)
        .load(imageUrl)
        .also { glide ->
            placeholder?.also { drawable ->
                val requestOptions = RequestOptions().placeholder(drawable)
                glide.apply(requestOptions)
            }
        }.into(view)
}

@BindingAdapter("app:time")
fun setTime(view: TextView, date: String?) {
    try {
        view.text = formatTime(view.context, date ?: "")
    } catch (ex: ParseException) {
        view.text = date ?: ""
    }
}

@BindingAdapter("app:isSelected")
fun setIsSelected(view: View, isSelected: Int) {
    view.isSelected = isSelected == 1
}

@BindingAdapter("app:isSelected")
fun setIsSelected(view: View, isSelected: Boolean) {
    view.isSelected = isSelected
}

@BindingAdapter(value = ["app:dimensionWidth", "app:dimensionHeight"], requireAll = true)
fun setDimension(view: View, width: Int?, height: Int?) {
    (view.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
        if (isDimensionInValid(width, height)) "16:9" else MathUtils.getAdjustedHeight(
            width!!,
            height!!
        )
}

fun isDimensionInValid(width: Int?, height: Int?) =
    width == 0 || width == null || height == 0 || height == null

@Throws(ParseException::class)
private fun formatTime(context: Context, date: String): String {
    return Utils.formatTime(context, date)
}

@BindingAdapter("app:isliked")
fun setIsLiked(view: View, flag: Int) {
    view.isSelected = flag != 0
}

@BindingAdapter("app:isUpVoted")
fun setIsUpVoted(view: View, flag: Int?) {
    view.isSelected = flag != 0
}

@BindingAdapter("app:showIfUnAnswered")
fun showIfUnAnswered(view: View, type: String) {
}

@BindingAdapter("app:titleText")
fun setTitleText(view: TextView, title: String?) {
    if (title == null) return
    val endIndex = title.length
    view.text = styleText(title, 0, endIndex, StyleSpan(Typeface.BOLD))
}

@BindingAdapter("app:bodyText")
fun setBodyText(view: TextView, title: String?) {
    view.text = title ?: ""
    Linkify.addLinks(view, Linkify.ALL)
}

private fun styleText(text: String, startIndex: Int, endIndex: Int, style: Any): SpannableString {
    val spannableString = SpannableString(text)
    spannableString.setSpan(style, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannableString
}

@BindingAdapter("app:capsuleTextColor")
fun setCapsuleTextColor(view: TextView, color: String) {
    view.setTextColor(Color.parseColor(color))
}

@BindingAdapter("app:capsuleBgColor")
fun setCapsuleBgColor(view: TextView, color: String) {
    view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
}

@BindingAdapter("app:capsuleBgVisibility")
fun setCapsuleBgVisibility(view: TextView, text: String?) {
    if (!text.isNullOrBlank()) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

fun isQuizOver(unpublishTime: String?): Boolean {
    val now = if (TrueTimeRx.isInitialized()) {
        TrueTimeRx.now()
    } else {
        Calendar.getInstance().time
    }
    val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val trueTime = readFormat.parse(readFormat.format(now.time))
    val endTime =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(unpublishTime)
    return trueTime.after(endTime)
}

fun setUpTestTimer(time: Long, view: TextView) {

    val countDownTimer = object : CountDownTimer(time, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            view.text = view.context.getString(
                R.string.string_quiz_question_timer,
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                (
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))
                    ),
                (
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )
                    )
            )
        }

        override fun onFinish() {
            view.text = view.context.getString(R.string.string_quiz_time_over)
        }
    }
    countDownTimer.start()
}

fun getTestStartBeforeTimeDifferenceLong(publishTime: String?): Long {
    val now = if (TrueTimeRx.isInitialized()) {
        TrueTimeRx.now()
    } else {
        Calendar.getInstance().time
    }
    val trueTime = readFormat.parse(readFormat.format(now.time))
    val startTestTime =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
    return startTestTime.time - trueTime.time
}

fun getTestStartAfterTimeDifferenceLong(unpublishTime: String?): Long {
    val now = if (TrueTimeRx.isInitialized()) {
        TrueTimeRx.now()
    } else {
        Calendar.getInstance().time
    }
    return readFormat.parse(unpublishTime).time - readFormat.parse(readFormat.format(now.time)).time
}

private fun getTrueTimeDecision(publishTime: String?, unpublishTime: String?, now: Date): String {
    val trueTime = readFormat.parse(readFormat.format(now.time))
    val startTestTime =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
    val endTestTime =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(unpublishTime)

    val flag = if (trueTime.after(endTestTime)) {
        Constants.TEST_OVER
    } else if (trueTime.before(startTestTime)) {
        Constants.TEST_UPCOMING
    } else {
        Constants.TEST_ACTIVE
    }
    return flag
}

@BindingAdapter("app:videoDuration")
fun setVideoDuration(view: TextView, duration: String?) {
    if (!duration.isNullOrBlank() && duration != "NULL") {
        val minutes = ((duration!!.toInt()) / 60)
        val seconds = ((duration.toInt()) % 60)
        view.text =
            view.context.resources.getString(R.string.string_video_duration, minutes, seconds)
    }
}

@BindingAdapter("app:textNullOrNot")
fun setTextNullOrNot(view: TextView, data: String?) {
    if (data != null && !data.equals("")) {
        view.text = data
    }
}

@BindingAdapter(value = ["app:videoDes", "app:videoViews"], requireAll = true)
fun setVideoDesView(view: TextView, des: String?, views: Int?) {
    var viewShow = ""
    var description = ""
    if ((views != null && views != 0)) {
        viewShow = views.toString()
    }
    if (!(des.isNullOrBlank())) {
        description = des.toString()
    }
    view.text =
        view.context.resources.getString(R.string.string_video_views_and_des, viewShow, description)
}

@BindingAdapter(value = ["app:videoDesVis", "app:videoViewsVis"], requireAll = true)
fun setVideoViewDesVisibility(view: TextView, des: String?, views: Int?) {
    if (!(des.isNullOrBlank()) || (views != null && views != 0)) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("app:buttonBgColor")
fun setButtonBgColor(view: Button, color: String) {
    view.background.setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY)
}

@BindingAdapter("app:buttonTextColor")
fun setButtonTextColor(view: Button, color: String) {
    view.setTextColor(Color.parseColor(color))
}

@BindingAdapter("app:data")
fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, data: T) {
    if (recyclerView.adapter is com.doubtnutapp.base.BindingAdapter<*>) {
        (recyclerView.adapter as com.doubtnutapp.base.BindingAdapter<T>).setData(data)
    }
}

@BindingAdapter("app:srcCompat")
fun setImageResource(view: ImageView, resourceId: Drawable) {
    view.setImageDrawable(resourceId)
}

@BindingAdapter("app:imageGoneOnNullOrBlank")
fun setImageVisibility(view: ImageView, text: String?) {
    if (!text.isNullOrBlank()) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("app:constraintDimension")
fun setConstraintDimension(view: View, layoutType: String?) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    if (layoutType != null && layoutType == "video_wrapper") {
        params.dimensionRatio = "16:12"
    } else {
        params.dimensionRatio = "16:9"
    }
    view.layoutParams = params
}

@BindingAdapter("app:constraintDimensionOrDefault")
fun setUpConstraintDimension(view: View, ratio: String?) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    if (ratio == null) {
        params.dimensionRatio = "16:9"
    } else {
        params.dimensionRatio = ratio
    }
    view.layoutParams = params
}

@BindingAdapter("app:viewsCount")
fun setViewsCount(textView: TextView, views: Long?) {
    if (views == null) {
        textView.isVisible = false
        return
    }
    val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
    val value = kotlin.math.floor(kotlin.math.log10(views.toDouble())).toInt()
    val base = value / 3
    val viewsText = if (value >= 3 && base < suffix.size) {
        DecimalFormat("#0.0").format(
            views / Math.pow(
                10.0,
                base * 3.toDouble()
            )
        ) + suffix[base]
    } else {
        DecimalFormat("#,##0").format(views)
    }
    textView.text = String.format("\u25CF  %s Views", viewsText)
}

@BindingAdapter("app:firstCapText")
fun setFirstCapText(textView: TextView, text: String?) {
    if (text.isNullOrEmpty()) {
        textView.isVisible = false
        return
    }
    textView.text = text.toLowerCase().capitalize()
}

@BindingAdapter("app:setDotText")
fun setDotText(textView: TextView, text: String?) {
    if (text.isNullOrEmpty()) {
        textView.isVisible = false
        return
    }
    textView.text = String.format(
        textView.context.getString(R.string.dot_start),
        text.toLowerCase().capitalize()
    )
}

@BindingAdapter("app:hideOnNull")
fun hideOnNull(view: View, data: Any?) {
    view.isVisible = data != null
}
