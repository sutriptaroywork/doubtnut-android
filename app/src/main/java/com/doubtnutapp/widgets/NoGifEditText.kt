package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat

open class NoGifEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))
        val callback = InputConnectionCompat.OnCommitContentListener { _, _, _ -> true }
        return InputConnectionCompat.createWrapper(ic!!, editorInfo, callback)
    }
}