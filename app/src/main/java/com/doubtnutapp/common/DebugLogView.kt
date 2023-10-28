package com.doubtnutapp.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.doubtnutapp.*
import kotlinx.android.synthetic.main.debug_log_view.view.*

class DebugLogView(ctx: Context, attrs: AttributeSet) : LinearLayout(ctx, attrs) {

    private var debugLogEnabled = false

    init {
        debugLogEnabled = defaultPrefs(context).getBoolean(Constants.ENABLE_DEBUG_LOG, false)
        if (debugLogEnabled) {
            LayoutInflater.from(context).inflate(R.layout.debug_log_view, this, true)
            show()
        } else {
            hide()
        }
    }

    fun addLog(debugText: String) {
        if (!debugLogEnabled) return

        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.apply {
            text = debugText
            setTextColor(Color.WHITE)
            textSize = 12f
        }
        debugTrackingContainer.addView(textView)
        debugTrackingView.post {
            debugTrackingView.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun isDebugLogEnabled(): Boolean {
        return debugLogEnabled
    }
}
