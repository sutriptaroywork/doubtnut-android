package com.doubtnut.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object Utils {

    infix fun Int.not(value: Int): Boolean = this != value

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) return false
        if (context is Activity) {
            if (context.isDestroyed || context.isFinishing) {
                return false
            }
        }
        return true
    }

    fun getShape(
        colorString: String?,
        strokeColor: String?,
        cornerRadius: Float = 8f,
        strokeWidth: Int = 3,
        shape: Int = GradientDrawable.RECTANGLE
    ): GradientDrawable {
        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = shape
        shapeDrawable.cornerRadii = floatArrayOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius
        )
        shapeDrawable.setColor(parseColor(colorString))
        shapeDrawable.setStroke(strokeWidth, parseColor(strokeColor))
        return shapeDrawable
    }

    fun isAlphaNumeric(name: String?): Boolean {
        if (name != null) {
            for (c in name) {
                if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9' && c.isWhitespace().not()) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

}

fun HashMap<String, Any>.toRequestBody2(): RequestBody {
    return JSONObject(this as Map<*, *>).toString()
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
}