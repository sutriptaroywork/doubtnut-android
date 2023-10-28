package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.sticker.BaseActivity
import kotlinx.android.synthetic.main.activity_bundle.*

class CourseSelectionActivity : BaseActivity() {
    companion object {

        fun getStartIntent(context: Context, page: String?) =
                Intent(context, CourseSelectionActivity::class.java).apply {
                    putExtra(PAGE, page)
                }

        const val PAGE = "page"
    }

    var fragment: CourseSelectionDialogFragment? = null
    var page = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bundle)
        page = intent?.getStringExtra(PAGE).orEmpty()
        fragment = CourseSelectionDialogFragment.newInstance(page)
        fragment?.show(supportFragmentManager, "")
        supportFragmentManager.executePendingTransactions()
        fragment?.dialog?.setOnCancelListener {
            finish()
        }
        fragment?.dialog?.setOnDismissListener {
            finish()
        }

        parentContainer.setOnClickListener {
            finish()
        }
    }

}