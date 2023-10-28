package com.doubtnutapp.utils

import java.text.BreakIterator


object TextUtil {

    fun graphemeLength(str: String?): Int {
        val iter: BreakIterator = BreakIterator.getCharacterInstance()
        iter.setText(str)
        var count = 0
        while (iter.next() !== BreakIterator.DONE) count++
        return count
    }

}