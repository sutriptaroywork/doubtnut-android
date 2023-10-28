package com.doubtnutapp.live.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.databinding.FragmentLiveStreamOverlayBinding
import com.doubtnutapp.databinding.ItemLiveStreamCommentBinding
import com.doubtnutapp.live.viewmodel.LiveOverlayViewModel
import com.doubtnutapp.sharing.FEED_POST_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.showToast
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LiveOverlayFragment :
    BaseBindingFragment<LiveOverlayViewModel, FragmentLiveStreamOverlayBinding>() {

    companion object {
        fun newInstance(livePostId: String): LiveOverlayFragment {
            return LiveOverlayFragment().apply {
                arguments = Bundle().apply { putString(Constants.POST_ID, livePostId) }
            }
        }
        const val TAG = "LiveOverlayFragment"
    }

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    private var commentsAdapter: LiveCommentsAdapter? = null

    private lateinit var postId: String

    private var livePost: FeedPostItem? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        postId = requireArguments().getString(Constants.POST_ID) ?: return

        setupUI()
        setClickListeners()
        setObservers()

        DataHandler.INSTANCE.teslaRepository.getPost(postId).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Outcome.Success -> {
                    setLivePostData(it.data.data.data as? FeedPostItem)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (livePost != null && !livePost!!.isEnded) {
            viewModel.getLiveViewerCount(postId)
            viewModel.getLiveStreamComments(postId)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun setObservers() {
        viewModel.latestCommentsLiveData.observe(viewLifecycleOwner, Observer {
            commentsAdapter?.updateComments(it)
        })

        viewModel.liveViewerCountLiveData.observe(viewLifecycleOwner, Observer {
            if (it < 0) {
                handleStreamEnd()
            } else if (it == 0) {
                mBinding?.tvLiveCount?.hide()
            } else {
                mBinding?.tvLiveCount?.show()
                mBinding?.tvLiveCount?.text = it.toString()
            }
        })
    }

    private fun setLivePostData(livePost: FeedPostItem?) {
        if (livePost == null)
            return

        this.livePost = livePost

        mBinding?.tvLiveCount?.text = livePost.viewerCount.toString()
        mBinding?.tvDescription?.text = livePost.message
        mBinding?.tvPrice?.text = if (livePost.isPaid) "₹ ${livePost.streamFee}" else "Free"

        if (livePost.studentId == getStudentId()) {
            mBinding?.profileContainer?.hide()
        } else {
            mBinding?.profileContainer?.show()
            mBinding?.ivProfileImage?.loadImage(livePost.studentImageUrl)
            mBinding?.tvProfileName?.text = livePost.username
        }

        if (livePost.isEnded) {
            mBinding?.tvLive?.hide()
        } else {
            mBinding?.tvLive?.show()
        }

        mBinding?.tvPrice?.show()

        if (!livePost.isEnded) {
            viewModel.getLiveViewerCount(postId)
            viewModel.getLiveStreamComments(postId)
        }
    }

    private fun setClickListeners() {
        mBinding?.overflowMenu?.setOnClickListener {
            showPopUpMenu(it)
        }

        mBinding?.btnShare?.setOnClickListener {
            shareLiveStream()
        }

        mBinding?.btnAddComment?.setOnClickListener {
            AppUtils.hideKeyboard(it)
            sendComment()
        }
    }

    private fun sendComment() {
        val commentMessage = mBinding?.tvAddComment?.text.toString()
        if (commentMessage.isEmpty()) {
            toast("Enter a comment")
            return
        }
        mBinding?.tvAddComment?.text?.clear()
        viewModel.addComment(postId, commentMessage).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Outcome.Success -> {
                    val comment = it.data.data
                    commentsAdapter?.addComment(comment)
                }
                is Outcome.ApiError -> {
                    toast("Error sending comment, Please try again")
                }
                is Outcome.Failure -> {
                    toast("Error sending comment, Please try again")
                }
            }
        })
    }

    private fun showPopUpMenu(anchor: View) {
        if (livePost == null) return
        val popupMenu = PopupMenu(anchor.context, anchor)
        val menu = R.menu.menu_live_stream
        popupMenu.inflate(menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemReport -> {
                    DataHandler.INSTANCE.teslaRepository.reportPost(livePost!!.id).subscribeOn(Schedulers.io()).subscribe()
                    showToast(anchor.context, "Live stream reported")
                }
                R.id.itemInvite -> {
                    shareLiveStream()
                }
            }
            true
        }
    }

    private fun shareLiveStream() {
        if (livePost == null) return
        whatsAppSharing.shareOnWhatsApp(ShareOnWhatApp(FEED_POST_CHANNEL,
                featureType = Constants.FEATURE_TYPE_FEED_POST,
                imageUrl = if (livePost!!.attachments != null && livePost!!.attachments.isNotEmpty())
                    livePost!!.cdnPath + livePost!!.attachments[0] else "",
                controlParams = hashMapOf(
                        Constants.POST_ID to livePost!!.id
                ),
                bgColor = "#000000",
                sharingMessage = "Hey! Join this live session on Doubtnut -\n${livePost!!.message}",
                questionId = livePost!!.id
        ))
        whatsAppSharing.startShare(requireContext())
    }

    private fun setupUI() {
        mBinding?.commentsRecyclerview?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true)
        commentsAdapter = LiveCommentsAdapter()
        mBinding?.commentsRecyclerview?.adapter = commentsAdapter
    }

    fun handleStreamEnd() {
        mBinding?.streamEndContainer?.show()
    }

    fun handleStreamError(message: String) {
        mBinding?.tvStreamError?.show()
        mBinding?.tvStreamError?.text = message
    }

    private class LiveCommentsAdapter : RecyclerView.Adapter<LiveCommentsAdapter.CommentViewHolder>() {

        private var comments: List<Comment> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            return CommentViewHolder(
                ItemLiveStreamCommentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            val comment = comments[position]
            val binding = holder.binding
            binding.ivProfileImage.loadImage(comment.studentAvatar)
            binding.tvProfileName.text = comment.studentUsername
            binding.tvComment.text = comment.message

            if (position >= 0 && position == comments.size - 1) {
                binding.ivProfileImage.alpha = 0.6f
                binding.tvProfileName.alpha = 0.6f
                binding.tvComment.alpha = 0.6f
            }

            if (position >= 0 && position == comments.size - 2) {
                binding.ivProfileImage.alpha = 0.8f
                binding.tvProfileName.alpha = 0.8f
                binding.tvComment.alpha = 0.8f
            }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        fun updateComments(newComments: List<Comment>) {
            if (newComments.size < this.comments.size) return
            this.comments = newComments
            notifyDataSetChanged()
        }

        fun addComment(comment: Comment) {
            this.comments = comments.toMutableList().apply {
                add(0, comment)
            }
            if (this.comments.size > 4) this.comments = this.comments.take(4)
            notifyDataSetChanged()
        }

        class CommentViewHolder(val binding: ItemLiveStreamCommentBinding) :
            RecyclerView.ViewHolder(binding.root)

    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveStreamOverlayBinding {
        return FragmentLiveStreamOverlayBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LiveOverlayViewModel {
        return ViewModelProviders.of(this, viewModelFactory).get(LiveOverlayViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}