package com.doubtnutapp.dictionary

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.StatusBottomSheetClosed
import com.doubtnutapp.R

import com.doubtnutapp.base.OnDictionaryLangaugeSelected
import com.doubtnutapp.data.dictionary.Language
import com.doubtnutapp.dictionary.adapter.LanguageBottomsheetListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LanguageBottomFragment : BottomSheetDialogFragment(), ActionPerformer {

    companion object {
        fun newInstance(
            items: ArrayList<Language>?,
            actionPerformer: ActionPerformer
        ): LanguageBottomFragment {
            val fragment = LanguageBottomFragment()
            val args = Bundle()
            args.putSerializable(Constants.DATA, items ?: ArrayList<Language>())
            fragment.arguments = args
            fragment.actionPerformer = actionPerformer
            return fragment
        }
    }

    lateinit var items: ArrayList<Language>
    var actionText: String = ""
    lateinit var actionPerformer: ActionPerformer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        @Suppress("UNCHECKED_CAST")
        items =
            requireArguments().getSerializable(Constants.DATA) as ArrayList<Language>
        actionText = requireArguments().getString(Constants.TYPE, "View")

        val v = View.inflate(context, R.layout.layout_language_bootomsheet, null)
        dialog.setContentView(v)

        v.findViewById<RecyclerView>(R.id.rvLanguage).adapter =
            LanguageBottomsheetListAdapter(items, this)

        dialog.setCancelable(false)

        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        DoubtnutApp.INSTANCE.bus()?.send(StatusBottomSheetClosed())
    }

    override fun performAction(action: Any) {
        if (action is OnDictionaryLangaugeSelected) {
            actionPerformer?.performAction(action)
            dismiss()
        }
    }
}
