package com.doubtnutapp.feed.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AddLinkDialog(val okListener: OkListener) : DialogFragment() {

    interface OkListener {
        fun onOkPressed(dialogValue: String)
    }

    private var linkField: EditText? = null

    private val dialogLayout: LinearLayout
        private get() {
            val context = context
            val layout = LinearLayout(context)
            linkField = EditText(context)
            linkField!!.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
            linkField!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = 32
                marginEnd = 32
            }
            linkField!!.hint = "https://"
            layout.addView(linkField)
            layout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = 32
                marginEnd = 32
            }
            return layout
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder
                .setView(dialogLayout)
                .setTitle("Add a link")
                .setPositiveButton(
                        "Add", null)
                .setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int -> }
        return builder.create().apply {
            setOnShowListener {
                val button: Button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val link = linkField!!.text
                    if (link != null && link.length > 0) {
                        if (Patterns.WEB_URL.matcher(link).matches()) {
                            okListener.onOkPressed(linkField!!.text.toString())
                            dialog!!.dismiss()
                        } else {
                            linkField?.error = "Enter valid link"
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        linkField?.requestFocus()
    }
}