package com.doubtnutapp.pcmunlockpopup.ui.adapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.doubtnutapp.R


@BindingAdapter(value = ["app:currentBadgeCount", "app:requireBadgeCount"], requireAll = true)
fun setBadgeProgressText(textView: TextView, currentBadgeCount: Int, requiredBadgeCount: Int) {
    val spannableString = SpannableStringBuilder()

    val currentBadgeAsString = currentBadgeCount.toString()
    val currentBadgeStringLength = currentBadgeAsString.length
    spannableString.append(currentBadgeAsString)
    spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(textView.context, R.color.topaz)), 0, currentBadgeStringLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.append("/")
    spannableString.append(requiredBadgeCount.toString())

    textView.text = spannableString

}