package com.doubtnutapp.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * Created by Anand Gaurav on 08/12/20.
 */
class EditTextLimitUtil(private val min: Int, private val max: Int) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}