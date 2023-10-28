package com.doubtnut.core.utils

import android.text.InputFilter
import android.widget.EditText

fun EditText.blockSpecialCharacters() {
    val regex = Regex("^[~@?:.+,;'#^|$%&*!]*$")
    val filter = InputFilter { source, _, _, _, _, _ ->
        return@InputFilter when {
            source?.matches(regex) == true -> ""
            else -> null
        }
    }
    this.filters = arrayOf(filter)
}
