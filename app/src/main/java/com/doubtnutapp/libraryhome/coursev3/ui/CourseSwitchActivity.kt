package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnutapp.liveclass.ui.dialog.CourseSwitchDialogFragment

class CourseSwitchActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CourseSwitchActivity"
        const val TYPE = "type"
        const val SELECTED_ASSORTMENT_ID = "selected_assortment"
        const val ASSORTMENT_ID = "assortment_id"

        fun getStartIntent(
                context: Context, type: String,
                selectedAssortmentId: String? = null,
                assortmentId: String? = null,
        ): Intent {
            return Intent(context, CourseSwitchActivity::class.java).apply {
                putExtra(TYPE, type)
                putExtra(SELECTED_ASSORTMENT_ID, selectedAssortmentId)
                putExtra(ASSORTMENT_ID, assortmentId)
            }
        }
    }

    private var type: String = ""
    private var selectedAssortmentId: String = ""
    private var assortmentId: String = ""
    var dialog: CourseSwitchDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getStringExtra(TYPE).orEmpty()
        selectedAssortmentId = intent.getStringExtra(SELECTED_ASSORTMENT_ID).orEmpty()
        assortmentId = intent.getStringExtra(ASSORTMENT_ID).orEmpty()
        dialog = CourseSwitchDialogFragment.newInstance(type, selectedAssortmentId, assortmentId)
        dialog?.show(supportFragmentManager, TAG)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        dialog?.dismiss()
        finish()

    }
}