package com.doubtnutapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickContent : ActivityResultContract<PickContentInput, PickContentOutput>() {

    private var requestCode: Int? = 0

    override fun createIntent(context: Context, input: PickContentInput?): Intent =
        Intent().apply {
            type = input?.type
            if (input?.category != null) {
                addCategory(input.category)
            }
            action = input?.action
            if (input?.flags != null) {
                flags = input.flags
            }
            requestCode = input?.requestCode
            if (input?.extraMimeTypes.isNullOrEmpty().not()) {
                putExtra(Intent.EXTRA_MIME_TYPES, input?.extraMimeTypes!!.toTypedArray())
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): PickContentOutput? {
        return when {
            resultCode != Activity.RESULT_OK -> null
            else -> {
                PickContentOutput(
                    uri = intent?.data,
                    requestCode = requestCode
                )
            }
        }
    }
}

data class PickContentInput(
    val type: String,
    val requestCode: Int,
    val extraMimeTypes: List<String>? = null,
    val action: String = Intent.ACTION_GET_CONTENT,
    val flags: Int? = null,
    val category: String? = null
)

data class PickContentOutput(
    val uri: Uri?,
    val requestCode: Int?
)