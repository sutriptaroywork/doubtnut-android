package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.doubtnutapp.*
import com.doubtnutapp.databinding.ActivityCourseCategoryBinding
import com.doubtnutapp.libraryhome.course.ui.ExploreFragment
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.PrePurchaseCallingCardConstant
import com.doubtnutapp.sales.PrePurchaseCallingCardData2
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.dialog.PrePurchaseCallingCardBottomSheetDialogFragment
import com.uxcam.UXCam
import kotlinx.android.synthetic.main.activity_course_category.*
import java.util.concurrent.TimeUnit

/**
 * Open Pages like Kota Classes, IIT JEE, NEET etc.
 */
class CourseCategoryActivity : AppCompatActivity() {

    private val categoryId: String
        get() = intent?.getStringExtra(CATEGORY_ID) ?: ""

    private val filters: String?
        get() = intent.getStringExtra(FILTERS)

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.white_20)
        setContentView(ActivityCourseCategoryBinding.inflate(layoutInflater).root)

        init()
        initListener()
    }

    private fun init() {
        val source = intent.getStringExtra(Constants.SOURCE)
        title = intent.getStringExtra(TITLE)

        tvToolbarTitle.text = title

        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            ExploreFragment.newInstance(
                categoryId = categoryId,
                filters = filters,
                source = source.orEmpty()
            )
        ).commit()

        handler.postDelayed(
            { showPrePurchaseCallingCardBottomSheetDialogFragment(false) },
            TimeUnit.SECONDS.toMillis(PrePurchaseCallingCardConstant.DELAY_TIME_IN_SEC_FOR_AUTO_SHOW_BOTTOM_SHEET)
        )
    }

    private fun initListener() {
        ivBack.setOnClickListener {
            onBackPressed()
        }
        ivSearch.setOnClickListener {
            startActivity(CategorySearchActivity.getStartIntent(this, categoryId))
        }
    }

    companion object {
        const val TAG = "CourseCategoryActivity"
        const val CATEGORY_ID = "category_id"
        const val FILTERS = "filters"
        const val TITLE = "title"

        fun startActivity(
            context: Context,
            start: Boolean = true,
            categoryId: String,
            title: String,
            filters: String?,
            source: String? = ""
        ): Intent {
            return Intent(context, CourseCategoryActivity::class.java).apply {
                putExtra(CATEGORY_ID, categoryId)
                putExtra(FILTERS, filters)
                putExtra(TITLE, title)
                putExtra(Constants.SOURCE, source.orEmpty())
            }.also {
                if (start) context.startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UXCam.tagScreenName("Category")
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
        if (fragment == null || fragment !is ExploreFragment) {
            if (isCalledFromBackPressed) {
                finish()
            }
            return
        }
        val titleSubtitle = if (fragment.isCourse) {
            Pair(
                defaultPrefs().getString(Constants.TITLE_PROBLEM_PURCHASE, ""),
                defaultPrefs().getString(Constants.SUBTITLE_PROBLEM_PURCHASE, "")
            )
        } else {
            Pair(
                defaultPrefs().getString(Constants.TITLE_PROBLEM_SEARCH, ""),
                defaultPrefs().getString(Constants.SUBTITLE_PROBLEM_SEARCH, "")
            )
        }

        if (titleSubtitle.first.isNullOrEmpty()) {
            if (isCalledFromBackPressed) {
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
            ).orEmpty().contains(categoryId)
        ) {
            if (isCalledFromBackPressed) {
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
                }-$categoryId"
            )
        }

        supportFragmentManager.beginTransaction()
            .add(
                PrePurchaseCallingCardBottomSheetDialogFragment.newInstance(
                    PrePurchaseCallingCardModel2().apply {
                        _data = PrePurchaseCallingCardData2(
                            title = titleSubtitle.first,
                            titleTextSize = PrePurchaseCallingCard2.titleTextSize(),
                            titleTextColor = PrePurchaseCallingCard2.titleTextColor(),
                            subtitle = titleSubtitle.second,
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