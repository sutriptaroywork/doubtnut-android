package com.doubtnutapp.dialogHolder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.VideoDialog
import com.doubtnutapp.databinding.ActivityBundleBinding
import com.doubtnutapp.liveclass.ui.dialog.CouponListDialogFragment
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.ui.common.address.SubmitAddressDialogFragment

class DialogHolderActivity : BaseActivity() {

    companion object {
        private const val TAG = "DialogHolderActivity"
        private const val FRAGMENT_ID = "fragment_id"
        private const val PAGE: String = "PAGE"
        private const val KEY_TYPE = "type"
        private const val KEY_ID = "id"

        private const val VIDEO_WITH_URL = "video_with_url"
        private const val COUPON_LIST = "coupon_list"
        private const val SUBMIT_ADDRESS = "submit_address"
        const val VIDEO_URL: String = "VIDEO_URL"

        fun getVideoWithUrlIntent(context: Context, url: String) =
            Intent(context, DialogHolderActivity::class.java).apply {
                putExtra(VIDEO_URL, url)
                putExtra(FRAGMENT_ID, VIDEO_WITH_URL)
            }

        fun getCouponListDialog(context: Context, page: String) =
            Intent(context, DialogHolderActivity::class.java).apply {
                putExtra(PAGE, page)
                putExtra(FRAGMENT_ID, COUPON_LIST)
            }

        fun getSubmitAddressDialog(
            context: Context,
            type: String,
            id: String?
        ) =
            Intent(context, DialogHolderActivity::class.java).apply {
                putExtra(FRAGMENT_ID, SUBMIT_ADDRESS)
                putExtra(KEY_TYPE, type)
                putExtra(KEY_ID, id)
            }
    }

    var fragment: DialogFragment? = null

    private lateinit var binding: ActivityBundleBinding

    private val page: String by lazy { intent?.getStringExtra(PAGE).orEmpty() }
    private val type: String by lazy { intent?.getStringExtra(KEY_TYPE).orEmpty() }
    private val id: String by lazy { intent?.getStringExtra(KEY_ID).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBundleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var fragmentTag = ""

        when (intent.getStringExtra(FRAGMENT_ID)) {
            VIDEO_WITH_URL -> {
                val videoUrl = intent.getStringExtra(VIDEO_URL).orEmpty()
                fragment = VideoDialog.newInstance(
                    videoUrl = videoUrl,
                    orientation = "portrait",
                    questionId = -1,
                    page = ""
                )
                fragmentTag = VideoDialog.TAG
            }
            COUPON_LIST -> {
                CouponListDialogFragment.newInstance(page).show(supportFragmentManager, TAG)
            }
            SUBMIT_ADDRESS -> {
                SubmitAddressDialogFragment.newInstance(type, id)
                    .show(supportFragmentManager, SubmitAddressDialogFragment.TAG)
            }
        }

        fragment?.show(supportFragmentManager, fragmentTag)

        supportFragmentManager.executePendingTransactions()

        fragment?.dialog?.setOnCancelListener {
            finish()
        }

        fragment?.dialog?.setOnDismissListener {
            finish()
        }

        binding.parentContainer.setOnClickListener {
            finish()
        }
    }
}
