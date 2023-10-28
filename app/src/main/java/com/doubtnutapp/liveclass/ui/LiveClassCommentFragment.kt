package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.ADD_COMMENTS_LANDSCAPE
import com.doubtnut.analytics.EventConstants.PREDEFINED_COMMENTS_ClICK_LANDSCAPE
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCommentEditTextClicked
import com.doubtnutapp.base.OnCommentTagClicked
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.PreComment
import com.doubtnutapp.databinding.FragmentLiveClassCommentsBinding
import com.doubtnutapp.hide
import com.doubtnutapp.liveclass.adapter.CommentTagsAdapter
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.youtubeVideoPage.comment.CommentsInVideoPageViewModel
import javax.inject.Inject

class LiveClassCommentFragment :
    BaseBindingFragment<CommentsInVideoPageViewModel, FragmentLiveClassCommentsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "LiveClassCommentFragment"
        private const val TAGS_LIST = "Tags"
        private const val ENTITY_ID = "entityId"
        private const val DETAIL_ID = "detailId"
        private const val ENTITY_TYPE = "entityType"
        private const val OFFSET = "offset"
        const val IS_LIVE = "is_live"
        fun newInstance(
            entityType: String?, entityId: String?,
            detailId: String, tagsList: ArrayList<PreComment>?, offset: Long,
            isLive: Boolean
        ): LiveClassCommentFragment {
            val fragment = LiveClassCommentFragment()
            val args = Bundle()
            args.putParcelableArrayList(TAGS_LIST, tagsList)
            args.putString(ENTITY_TYPE, entityType)
            args.putString(ENTITY_ID, entityId)
            args.putString(DETAIL_ID, detailId)
            args.putLong(OFFSET, offset)
            args.putBoolean(IS_LIVE, isLive)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var actionPerformer: ActionPerformer? = null

    private var tagsList: List<PreComment>? = null
    private var commentCallInProgress: Boolean = false
    private lateinit var entityType: String
    private lateinit var detailId: String
    private lateinit var entityId: String
    private var batchId: String? = null
    private var lastPreComment: PreComment? = null
    private var currentOffsetInSec = 0L
    private var isForLiveVideo = false

    fun setCurrentOffset(offset: Long) {
        currentOffsetInSec = offset
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        setupListeners()
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun init() {
        entityId = arguments?.getString(ENTITY_ID).orEmpty()
        entityType = arguments?.getString(ENTITY_TYPE).orEmpty()
        detailId = arguments?.getString(DETAIL_ID).orEmpty()
        batchId = arguments?.getString(Constants.INTENT_EXTRA_BATCH_ID).orEmpty()
        currentOffsetInSec = arguments?.getLong(OFFSET) ?: 0L
        tagsList = arguments?.getParcelableArrayList<PreComment>(TAGS_LIST)
        isForLiveVideo = arguments?.getBoolean(IS_LIVE) ?: false
        mBinding?.rvComments?.adapter = CommentTagsAdapter(tagsList.orEmpty(), this)
        mBinding?.rvComments?.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    private fun onEditTextFocused(
        params: ViewGroup.LayoutParams,
        tvParams: ViewGroup.LayoutParams
    ) {
        val imm: InputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mBinding?.tvComment, InputMethodManager.SHOW_IMPLICIT)
        mBinding?.mainLayout?.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_cc000000
            )
        )
        params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        tvParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        mBinding?.layoutComment?.setMargins(
            ViewUtils.dpToPx(12f, context).toInt(), 0,
            ViewUtils.dpToPx(12f, context).toInt(), ViewUtils.dpToPx(32f, context).toInt()
        )
        mBinding?.playBtn?.visibility = View.VISIBLE
        mBinding?.tvComment?.maxHeight = ViewUtils.dpToPx(100f, context).toInt()
        mBinding?.tvComment?.maxLines = 5
        mBinding?.rvComments?.hide()
    }

    private fun setupListeners() {
        mBinding?.tvComment?.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            actionPerformer?.performAction(OnCommentEditTextClicked(hasFocus))
            val params = mBinding?.layoutComment?.layoutParams
            val tvParams = mBinding?.tvComment?.layoutParams
            if (hasFocus) {
                onEditTextFocused(params!!, tvParams!!)
            } else {
                hideKeyboard()
                mBinding?.mainLayout?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTransparent
                    )
                )
                params?.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                tvParams?.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                mBinding?.layoutComment?.setMargins(0, 0, 0, 0)
                mBinding?.tvComment?.text?.clear()
                mBinding?.playBtn?.visibility = View.GONE
                mBinding?.tvComment?.maxHeight = 0
                mBinding?.tvComment?.maxLines = 1
                mBinding?.rvComments?.show()
            }
            mBinding?.layoutComment?.layoutParams = params
            mBinding?.tvComment?.layoutParams = tvParams
        }
        mBinding?.playBtn?.setOnClickListener {
            onSendButtonClicked()
        }
    }

    private fun onSendButtonClicked() {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.string_noInternetConnection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (TextUtils.isEmpty(mBinding?.tvComment?.text)) {
            ToastUtils.makeText(
                requireActivity(),
                R.string.plz_write_something_about_post,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (commentCallInProgress) {
            showToast(context, getString(R.string.api_call_in_progress))
            return
        }
        val message = mBinding?.tvComment?.text.toString()
        mBinding?.tvComment?.text?.clear()
        sendCommentAddRequest(message)
    }

    override fun performAction(action: Any) {
        if (action is OnCommentTagClicked) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(PREDEFINED_COMMENTS_ClICK_LANDSCAPE,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.DETAIL_ID, detailId)
                        put("comment", action.tag)
                    }, ignoreSnowplow = true)
            )
            lastPreComment = action.tag
            mBinding?.tvComment?.text?.append(action.tag.title.orEmpty() + " ")
            mBinding?.tvComment?.requestFocus()
            actionPerformer?.performAction(OnCommentEditTextClicked(true))
        }
    }

    private fun sendCommentAddRequest(message: String) {
        this.commentCallInProgress = true
        analyticsPublisher.publishEvent(
            AnalyticsEvent(ADD_COMMENTS_LANDSCAPE,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.DETAIL_ID, detailId)
                }, ignoreSnowplow = true)
        )
        val isDoubt =
            if (lastPreComment?.isDoubt == "1" && message.contains(lastPreComment?.title.orEmpty())) {
                "1"
            } else {
                "0"
            }
        viewModel.addComment(
            entityType, entityId, detailId, message, null, null, isDoubt,
            if (isForLiveVideo) {
                "-1"
            } else {
                currentOffsetInSec.toString()
            }, batchId
        ).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Outcome.Progress -> {
                }
                is Outcome.Failure -> {
                    commentCallInProgress = false
                    if (NetworkUtils.isConnected(requireActivity())) {
                        toast(getString(R.string.api_error))
                        return@Observer
                    }
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }

                is Outcome.ApiError -> {
                    commentCallInProgress = false
                    toast(getString(R.string.api_error))
                }

                is Outcome.BadRequest -> {
                    commentCallInProgress = false
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(requireFragmentManager(), "BadRequestDialog")
                }

                is Outcome.Success -> {
                    actionPerformer?.performAction(OnCommentEditTextClicked(false))
                    hideKeyboard()
                    showToast(context, "Comment Sent")
                    commentCallInProgress = false
                }
            }
        })
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveClassCommentsBinding {
        return FragmentLiveClassCommentsBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CommentsInVideoPageViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}