package com.doubtnutapp.ui.errorRequest

import com.doubtnut.core.ui.base.CoreBadRequestDialog

class BadRequestDialog : CoreBadRequestDialog() {

    companion object {
        fun newInstance(
            from: String,
            cancellable: Boolean = false
        ) = CoreBadRequestDialog.newInstance(from, cancellable)
    }
}

