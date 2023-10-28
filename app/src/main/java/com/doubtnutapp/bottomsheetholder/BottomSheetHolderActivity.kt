package com.doubtnutapp.bottomsheetholder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.referral.data.entity.ButtonData
import com.doubtnut.referral.ui.ShareYourReferralCodeBottomSheetDialogFragment
import com.doubtnutapp.Constants
import com.doubtnutapp.CourseBottomSheetDialogFragment
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.data.remote.models.PreComment
import com.doubtnutapp.databinding.ActivityBundleBinding
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.survey.ui.fragments.UserSurveyBottomSheetFragment
import com.doubtnutapp.ui.forum.comments.CommentBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.AndroidInjection
import io.branch.referral.Defines
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Generic Bottom Sheet holder Activity.
 */
class BottomSheetHolderActivity : BaseActivity() {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    var fragment: BottomSheetDialogFragment? = null
    private var fragmentTag: String = ""

    private val surveyId: Long by lazy { intent.getLongExtra(PARAM_SURVEY_ID, 0) }
    private val page: String by lazy { intent.getStringExtra(PARAM_PAGE) ?: "" }
    private val type: String by lazy { intent.getStringExtra(PARAM_TYPE) ?: "" }
    val ids: ArrayList<String> by lazy { intent.getStringArrayListExtra(PARAMS_IDS)!! }
    val position: Int by lazy { intent.getIntExtra(PARAMS_POSITION, 0) }
    val flagrId: String by lazy { intent.getStringExtra(KEY_FLAGR_ID).orEmpty() }
    val variantId: String by lazy { intent.getStringExtra(KEY_VARIANT_ID).orEmpty() }
    val source: String by lazy { intent.getStringExtra(KEY_SOURCE).orEmpty() }
    val fragmentId: String by lazy { intent.getStringExtra(FRAGMENT_ID).orEmpty() }
    val deeplinkSource: String by lazy { intent.getStringExtra(KEY_DEEPLINK_SOURCE).orEmpty() }

    private val buttonData: ButtonData? by lazy {
        intent.getParcelableExtra(KEY_BUTTON_DATA)
    }

    private val binding by viewBinding(ActivityBundleBinding::inflate)

    var isBranchLink = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (fragmentId) {
            FRAGMENT_SURVEY -> {
                fragment = UserSurveyBottomSheetFragment.newInstance(surveyId, page, type)
                fragmentTag = UserSurveyBottomSheetFragment.TAG
                fragment?.show(supportFragmentManager, fragmentTag)
                supportFragmentManager.executePendingTransactions()
            }
            FRAGMENT_COURSE_DETAILS -> {
                fragment =
                    CourseBottomSheetDialogFragment.newInstance(
                        ids = ids,
                        position = position,
                        flagrId = flagrId,
                        variantId = variantId,
                        source = source,
                        deeplinkSource = deeplinkSource
                    )
                fragmentTag = UserSurveyBottomSheetFragment.TAG
                fragment?.show(supportFragmentManager, fragmentTag)
                supportFragmentManager.executePendingTransactions()
            }
            FRAGMENT_COMMENT -> {
                fragment =
                    CommentBottomSheetFragment.newInstance(
                        intent.getStringExtra(Constants.INTENT_EXTRA_ENTITY_TYPE),
                        intent.getStringExtra((Constants.INTENT_EXTRA_ENTITY_ID)),
                        intent.getStringExtra((CommentBottomSheetFragment.DETAIL_ID))!!,
                        intent.getParcelableArrayListExtra(CommentBottomSheetFragment.TAGS_LIST),
                        intent.getStringExtra((CommentBottomSheetFragment.PINNED_POST)),
                        intent.getLongExtra((CommentBottomSheetFragment.OFFSET), 0L),
                        intent.getStringExtra((Constants.INTENT_EXTRA_BATCH_ID)),
                        intent.getBooleanExtra((CommentBottomSheetFragment.IS_LIVE), false),
                        intent.getStringExtra((CommentBottomSheetFragment.OPEN_ANSWERED_DOUBT_COMMENT_ID)),
                        intent.getStringExtra(
                            (CommentBottomSheetFragment.ASSORTMENT_ID)
                        ),
                        intent.getStringExtra(Constants.CHAPTER),
                        intent.getStringExtra(Constants.QUESTION_ID),
                        intent.getBooleanExtra(Constants.IS_VIP,true),
                        intent.getBooleanExtra(Constants.IS_PREMIUM,true),
                        intent.getBooleanExtra(Constants.IS_RTMP,false)
                    )
                fragment?.show(supportFragmentManager, fragmentTag)
                supportFragmentManager.executePendingTransactions()
            }
            ShareYourReferralCodeBottomSheetDialogFragment.TAG -> {
                var hasPermission = false
                var isWhatsappLaunched = false
                var isPermissionRequested = false
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        hasPermission = true
                        ShareYourReferralCodeBottomSheetDialogFragment.isCalledAfterGrantPermission =
                            false
                        launchShareYourReferralCodeBottomSheetDialogFragment()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) -> {
                        isWhatsappLaunched = true
                        val message = URLEncoder.encode(
                            buttonData?.shareMessage ?: return,
                            "UTF-8"
                        )
                        launchWhatsapp(
                            deeplink = "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?text=$message",
                            isGranted = null
                        )
                    }
                    else -> {
                        isPermissionRequested = true
                        requestContactPermission.launch(Manifest.permission.READ_CONTACTS)
                    }
                }

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${CoreEventConstants.VIEWED}",
                        hashMapOf(
                            CoreEventConstants.ID to CoreUserUtils.getStudentId(),
                            FRAGMENT_ID to fragmentId,
                            CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            CoreEventConstants.HAS_PERMISSION to hasPermission,
                            CoreEventConstants.IS_WA_LAUNCHED to isWhatsappLaunched,
                            CoreEventConstants.IS_PERMISSION_REQUESTED to isPermissionRequested,
                        )
                    )
                )
            }
        }

        fragment?.dialog?.setOnCancelListener {
            finish()
        }

        fragment?.dialog?.setOnDismissListener {
            finish()
        }

        binding.root.setOnClickListener {
            finish()
        }
    }

    private fun launchShareYourReferralCodeBottomSheetDialogFragment() {
        fragment = ShareYourReferralCodeBottomSheetDialogFragment.newInstance(
            buttonData ?: return
        )
        fragmentTag = ShareYourReferralCodeBottomSheetDialogFragment.TAG

        fragment?.show(supportFragmentManager, fragmentTag)
        supportFragmentManager.executePendingTransactions()
    }

    private val requestContactPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> {
                    ShareYourReferralCodeBottomSheetDialogFragment.isCalledAfterGrantPermission =
                        true
                    launchShareYourReferralCodeBottomSheetDialogFragment()
                }
                else -> {
                    val message = URLEncoder.encode(
                        buttonData?.shareMessage ?: return@registerForActivityResult,
                        "UTF-8"
                    )
                    launchWhatsapp(
                        deeplink = "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?text=$message",
                        isGranted = false
                    )
                }
            }
        }

    private fun launchWhatsapp(deeplink: String, isGranted: Boolean?) {
        deeplinkAction.performAction(
            this,
            deeplink
        )

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${EVENT_TAG}_${CoreEventConstants.WHATSAPP_LAUNCHED}",
                hashMapOf(
                    CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                    FRAGMENT_ID to fragmentId,
                    CoreEventConstants.IS_PERMISSION_GRANTED to isGranted.toString(),
                )
            )
        )

        finish()
    }

    companion object {

        private const val FRAGMENT_ID = "fragment_id"

        private const val FRAGMENT_SURVEY = "fragment_survey"
        private const val PARAM_SURVEY_ID = "survey_id"
        private const val PARAM_PAGE = "page"
        private const val PARAM_TYPE = "type"

        private const val FRAGMENT_COURSE_DETAILS = "fragment_course_details"
        private const val FRAGMENT_COMMENT = "fragment_comment"

        private const val PARAMS_IDS = "ids"
        private const val PARAMS_POSITION = "position"
        private const val KEY_FLAGR_ID = "flagr_id"
        private const val KEY_VARIANT_ID = "variant_id"
        private const val KEY_SOURCE = "source"
        private const val KEY_DEEPLINK_SOURCE = "deeplink_source"

        private const val KEY_BUTTON_DATA = "button_data"

        private const val EVENT_TAG = "bottom_sheet_holder_activity"

        fun getSurveyStartIntent(
            context: Context,
            id: Long,
            page: String?,
            type: String?
        ) =
            Intent(context, BottomSheetHolderActivity::class.java).apply {
                putExtra(PARAM_SURVEY_ID, id)
                putExtra(PARAM_PAGE, page)
                putExtra(PARAM_TYPE, type)
                putExtra(FRAGMENT_ID, FRAGMENT_SURVEY)
            }

        fun getCourseDetailsStartIntent(
            context: Context,
            ids: List<String>,
            position: Int,
            flagrId: String?,
            variantId: String?,
            source: String?,
            deeplinkSource: String?
        ) =
            Intent(context, BottomSheetHolderActivity::class.java).apply {
                putExtra(FRAGMENT_ID, FRAGMENT_COURSE_DETAILS)
                putStringArrayListExtra(PARAMS_IDS, ids as ArrayList<String>)
                putExtra(PARAMS_POSITION, position)
                putExtra(KEY_FLAGR_ID, flagrId)
                putExtra(KEY_VARIANT_ID, variantId)
                putExtra(KEY_SOURCE, source)
                putExtra(KEY_DEEPLINK_SOURCE, deeplinkSource)
            }

        fun getCommentsStartIntent(
            context: Context,
            entityType: String?,
            entityId: String?,
            detailId: String,
            tagsList: java.util.ArrayList<PreComment>?,
            pinnedPost: String?,
            offset: Long,
            batchId: String?,
            isLive: Boolean,
            openAnsweredDoubtWithCommentId: String? = null,
            assortmentId: String?,
            chapter: String,
            qid: String,
            isVip:Boolean,
            isPremium:Boolean,
            isRtmp:Boolean
        ) =
            Intent(context, BottomSheetHolderActivity::class.java).apply {
                putExtra(FRAGMENT_ID, FRAGMENT_COMMENT)
                putExtra(Constants.INTENT_EXTRA_ENTITY_TYPE, entityType)
                putExtra(Constants.INTENT_EXTRA_ENTITY_ID, entityId)
                putExtra(CommentBottomSheetFragment.DETAIL_ID, detailId)
                putExtra(Constants.INTENT_EXTRA_BATCH_ID, batchId)
                putParcelableArrayListExtra(CommentBottomSheetFragment.TAGS_LIST, tagsList)
                putExtra(CommentBottomSheetFragment.PINNED_POST, pinnedPost)
                putExtra(CommentBottomSheetFragment.OFFSET, offset)
                putExtra(CommentBottomSheetFragment.IS_LIVE, isLive)
                putExtra(
                    CommentBottomSheetFragment.OPEN_ANSWERED_DOUBT_COMMENT_ID,
                    openAnsweredDoubtWithCommentId
                )
                putExtra(CommentBottomSheetFragment.ASSORTMENT_ID, assortmentId)
                putExtra(Constants.CHAPTER, chapter)
                putExtra(Constants.QUESTION_ID, qid)
                putExtra(Constants.IS_VIP,isVip)
                putExtra(Constants.IS_PREMIUM,isPremium)
                putExtra(Constants.IS_RTMP,isRtmp)
            }

        fun getShareReferralStartIntent(
            context: Context,
            shareMessage: String,
            shareContactBatchSize: String?
        ) =
            Intent(context, BottomSheetHolderActivity::class.java).apply {
                putExtra(
                    KEY_BUTTON_DATA, ButtonData(
                        text = null,
                        icon = null,
                        bgColor = null,
                        shareMessage = shareMessage,
                        shareContactBatchSize = shareContactBatchSize?.toIntOrNull(),
                    )
                )
                putExtra(FRAGMENT_ID, ShareYourReferralCodeBottomSheetDialogFragment.TAG)
            }
    }

    override fun startActivity(intent: Intent?) {
        isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    override fun onStop() {
        if (isBranchLink) finishAffinity()
        super.onStop()
    }
}