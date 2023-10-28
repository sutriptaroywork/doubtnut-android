package com.doubtnut.core.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.AnyRes
import androidx.cardview.widget.CardView
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.Utils.not
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

object ViewUtils {

    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }

    fun convertPixelsToDp(px: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun dpToPx(dp: Float, context: Context?): Float {
        val px: Float? = context?.resources?.let {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, it.displayMetrics)
        }
        return Math.max(px ?: 0f, dp)
    }

    fun Activity.screenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun setWidthBasedOnPercentage(
        context: Context,
        view: View,
        size: String,
        @AnyRes spacing: Int
    ) {
        val newSize = size.toFloatOrNull() ?: return
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val outValue = TypedValue()
        context.resources.getValue(spacing, outValue, true)
        val value = outValue.float
        val deviceWidth = ((displayMetrics.widthPixels - (2 * convertDpToPixel(value))) / newSize)
        view.layoutParams.width = deviceWidth.toInt()
    }
}

fun Int.dpToPx() = ViewUtils.convertDpToPixel(toFloat()).toInt()
fun Int.dpToPxFloat() = ViewUtils.convertDpToPixel(toFloat())

fun Int.pxToDp() = ViewUtils.convertPixelsToDp(toFloat()).toInt()
fun Int.pxToDpFloat() = ViewUtils.convertPixelsToDp(toFloat())

fun Float.dpToPx() = ViewUtils.convertDpToPixel(this)

fun Int.spToPx(): Int = toFloat().spToPx().toInt()

fun Float.spToPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )
}

fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(left, top, right, bottom)
        p.marginStart = left
        p.marginEnd = right
        requestLayout()
    }
}

fun View.applyBackgroundColor(color: String?) {
    setBackgroundColor(parseColor(color))
}

fun View.applyForegroundColor(color: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        foreground = ColorDrawable(parseColor(color))
    }
}

fun View.applyBackgroundTint(color: String?, default: Int = Color.WHITE) {
    if (!color.isNullOrEmpty()) {
        backgroundTintList = ColorStateList.valueOf(parseColor(color, default))
    }
}

fun View.applyBackgroundColor(color: String?, default: String) {
    if (!color.isNullOrEmpty()) {
        setBackgroundColor(Color.parseColor(color))
        return
    }
    setBackgroundColor(Color.parseColor(default))
}

fun View.applyBackgroundTintList(color: String?, default: String) {
    if (!color.isNullOrEmpty()) {
        backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        return
    }
    backgroundTintList = ColorStateList.valueOf(Color.parseColor(default))
}

fun CardView.applyBackgroundColor(color: String?, default: Int = Color.WHITE) {
    setCardBackgroundColor(parseColor(color, default))
}

fun MaterialCardView.applyStrokeColor(color: String?) {
    strokeColor = parseColor(color)
}

fun MaterialCardView.applyCornerRadius(cornerRadius: Float?) {
    cornerRadius?.let { r ->
        radius = r.dpToPx()
    }
}

fun RecyclerView.disableTouchEvents() {
    addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            return true
        }
    })
}

fun parseColor(colorString: String?, default: Int = Color.WHITE): Int {
    if (colorString.isNullOrBlank()) return default
    return try {
        Color.parseColor(colorString)
    } catch (e: Exception) {
        default
    }
}

fun MaterialCardView.applyRippleColor(color: String?) {
    if (!color.isNullOrEmpty()) {
        rippleColor = ColorStateList.valueOf(Color.parseColor(color))
        return
    }
}

fun MaterialCardView.applyRippleColor(color: String?, default: String) {
    if (!color.isNullOrEmpty()) {
        rippleColor = ColorStateList.valueOf(Color.parseColor(color))
        return
    }
    rippleColor = ColorStateList.valueOf(Color.parseColor(default))
}

fun ImageView.applyImageTintList(color: String?, default: String) {
    if (!color.isNullOrEmpty()) {
        imageTintList = ColorStateList.valueOf(Color.parseColor(color))
        return
    }
    imageTintList = ColorStateList.valueOf(Color.parseColor(default))
}

fun ShapeableImageView.applyStrokeColor(color: String?) {
    strokeColor = ColorStateList.valueOf(parseColor(color))
}

fun MaterialButton.applyRippleColor(color: String?) {
    if (!color.isNullOrEmpty()) {
        rippleColor = ColorStateList.valueOf(Color.parseColor(color))
        return
    }
}

fun CheckBox.applyButtonTint(color: String?, default: Int = Color.WHITE) {
    if (!color.isNullOrEmpty()) {
        buttonTintList = ColorStateList.valueOf(parseColor(color, default))
    }
}

fun View.gone() {
    if (this.visibility not View.GONE)
        this.visibility = View.GONE
}

fun View.visible() {
    if (this.visibility not View.VISIBLE)
        this.visibility = View.VISIBLE
}

fun View.setVisibleState2(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.updateMargins2(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom,
    start: Int = marginStart,
    end: Int = marginEnd,
) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        setMargins(left, top, right, bottom)
        marginStart = start
        marginEnd = end
    }
}

