package com.doubtnutapp.utils.wrapperinterface

import android.text.Editable
import android.text.TextWatcher

/**
 * Created this class so that the, any who need TextWatcher but does not required all the methods.
 */
interface SimpleTextWatcherListener : TextWatcher {

    override fun afterTextChanged(text: Editable?) {
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
    }
}