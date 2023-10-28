package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.doubtnutapp.*
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.PrePurchaseCallingCardConstant
import com.doubtnutapp.sales.PrePurchaseCallingCardData2
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.dialog.PrePurchaseCallingCardBottomSheetDialogFragment
import dagger.android.AndroidInjection
import java.util.concurrent.TimeUnit

/**
 * Pre Purchase and Post purchase course screen..
 */
class CourseActivityV3 : AppCompatActivity() {

    companion object {
        const val TAG = "CourseActivityV3"
        const val TITLE = "title"
        const val ASSORTMENT_ID = "id"
        const val DEFAULT_SUBJECT = "ALL"

        fun startActivity(
            context: Context,
            start: Boolean = true,
            assortmentId: String,
            source: String?,
            studentClass: String? = null,
        ): Intent {
            return Intent(context, CourseActivityV3::class.java).apply {
                putExtra(ASSORTMENT_ID, assortmentId)
                putExtra(Constants.SOURCE, source.orEmpty())
                putExtra(Constants.STUDENT_CLASS, studentClass)
            }.also {
                if (start) context.startActivity(it)
            }
        }
    }

    private var assortmentId: String = ""
    private var studentClass: String = ""
    private var source: String = ""

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        statusbarColor(this, R.color.white_20)
        setContentView(R.layout.activity_course_v3)
        assortmentId = intent.getStringExtra(ASSORTMENT_ID).orEmpty()
        studentClass = intent.getStringExtra(Constants.STUDENT_CLASS).orEmpty()
        source = intent.getStringExtra(Constants.SOURCE).orEmpty()
        showFragment(
            R.id.container,
            CourseFragment.newInstance(assortmentId, source, studentClass),
            ""
        )
        handler.postDelayed(
            { showPrePurchaseCallingCardBottomSheetDialogFragment(false) },
            TimeUnit.SECONDS.toMillis(PrePurchaseCallingCardConstant.DELAY_TIME_IN_SEC_FOR_AUTO_SHOW_BOTTOM_SHEET)
        )
    }

    private fun showFragment(
        @IdRes viewId: Int,
        fragment: Fragment,
        tag: String,
        backstack: Boolean = false
    ) {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .add(viewId, fragment, tag)
            .apply { if (backstack) addToBackStack(tag) }
            .commitAllowingStateLoss()
    }

    fun replaceFragment(fragment: Fragment, tag: String, backstack: Boolean = false) {
        if (supportFragmentManager.isDestroyed) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment, tag)
            .apply { if (backstack) addToBackStack(tag) }
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        showPrePurchaseCallingCardBottomSheetDialogFragment(true)
    }

    private fun showPrePurchaseCallingCardBottomSheetDialogFragment(isCalledFromBackPressed: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment == null || fragment !is CourseFragment || fragment.isVip || fragment.binding.bottomBar.isNotVisible) {
            if (isCalledFromBackPressed) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            return
        }

        if (defaultPrefs().getString(Constants.TITLE_PROBLEM_PURCHASE, "").isNullOrEmpty()) {
            if (isCalledFromBackPressed) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            return
        }
        val prePurchaseCallingCardShownTimestamp =
            defaultPrefs().getLong(
                PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_LAST_SHOWN_TIMESTAMP,
                0
            )
        if (System.currentTimeMillis() - prePurchaseCallingCardShownTimestamp
            > TimeUnit.DAYS.toMillis(1)
        ) {
            defaultPrefs().edit {
                putString(
                    PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                    ""
                )
            }
        }

        if (defaultPrefs().getString(
                PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                ""
            ).orEmpty().contains(assortmentId)
        ) {
            if (isCalledFromBackPressed) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            return
        }

        defaultPrefs().edit {
            putLong(
                PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_LAST_SHOWN_TIMESTAMP,
                System.currentTimeMillis()
            )
        }
        defaultPrefs().edit {
            putString(
                PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                "${
                    defaultPrefs().getString(
                        PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                        ""
                    )
                }-$assortmentId"
            )
        }

        val chatDeepLink = if (fragment.callingCardChatDeeplink.isNullOrEmpty()) {
            defaultPrefs().getString(
                Constants.CHAT_DEEPLINK,
                ""
            )
        } else {
            fragment.callingCardChatDeeplink
        }

        supportFragmentManager.beginTransaction()
            .add(
                PrePurchaseCallingCardBottomSheetDialogFragment.newInstance(
                    PrePurchaseCallingCardModel2().apply {
                        _data = PrePurchaseCallingCardData2(
                            title = defaultPrefs().getString(
                                Constants.TITLE_PROBLEM_PURCHASE,
                                ""
                            ),
                            titleTextSize = PrePurchaseCallingCard2.titleTextSize(),
                            titleTextColor = PrePurchaseCallingCard2.titleTextColor(),
                            subtitle = defaultPrefs().getString(
                                Constants.SUBTITLE_PROBLEM_PURCHASE,
                                ""
                            ),
                            subtitleTextSize = PrePurchaseCallingCard2.subtitleTextSize(),
                            subtitleTextColor = PrePurchaseCallingCard2.subtitleTextColor(),
                            actionText = PrePurchaseCallingCard2.action(),
                            actionTextSize = PrePurchaseCallingCard2.actionTextSize(),
                            actionTextColor = PrePurchaseCallingCard2.actionTextColor(),
                            actionImageUrl = PrePurchaseCallingCard2.actionImageUrl(),
                            actionDeepLink = defaultPrefs().getString(
                                Constants.CALLBACK_DEEPLINK,
                                ""
                            ),
                            imageUrl = PrePurchaseCallingCard2.imageUrl(),
                            source = PrePurchaseCallingCardConstant.SOURCE_COURSE_CATEGORY_BOTTOM_SHEET,
                        )
                    },
                    isCalledFromBackPressed
                ), PrePurchaseCallingCardBottomSheetDialogFragment.TAG
            )
            .commitAllowingStateLoss()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}