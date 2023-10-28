package com.doubtnutapp.ui.userstatus

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.userstatus.StatusAttachment
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.FragmentStatusDetailBinding
import com.doubtnutapp.feed.UserStatusActionType
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatusDetailFragment : DaggerFragment() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var userStatus: UserStatus

    var onStatusListFinshListener: OnStatusListPositionChangeListener? = null
    var pageNumber: Int = -1
    var popupMenu: PopupMenu? = null
    var source: String = ""

    var isStatusPaused = false

    private var mDisposable: Disposable? = null
    private var mCurrentProgress: Long = 0
    private var mCurrentIndex: Int = 0
    private var imagesList: ArrayList<String> = ArrayList()

    private var statusEventObserver: Disposable? = null

    private lateinit var binding: FragmentStatusDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userStatus = it.getParcelable<UserStatus>(Constants.DATA) as UserStatus
        }

        statusEventObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is StatusBottomSheetClosed -> {
                        binding.tvViewCount.isEnabled = true
                        binding.tvLikeCount.isEnabled = true
                        emitStatusProgress()
                        isStatusPaused = false
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setClickListeners()
        binding.llStatus.setOnTouchListener(onTouchListener)
    }

    private fun initUI() {
        mCurrentIndex = 0
        mCurrentProgress = 0
        imagesList.clear()
        for (item in userStatus.statusItem!!) {
            imagesList.add(item.getAttachmentImageUrl())
        }
        setImageStatusData()
        setProgressData()
    }

    private fun setClickListeners() {
        binding.userImage.loadImage(userStatus.profileImage, R.color.grey_feed, R.color.grey_feed)

        if (source == EventConstants.STATUS_SOURCE_HEADER) {
            if (userStatus.studentId != UserUtil.getStudentId()) {
                val isFollowing = userStatus.isFollowing ?: false
                if (isFollowing) {
                    binding.tvFollow.text = getString(R.string.following)
                } else {
                    binding.tvFollow.text = getString(R.string.follow)
                }
                binding.tvFollow.show()
            } else {
                binding.tvFollow.hide()
            }
            binding.tvFollow.setOnClickListener {
                followStatus(binding.tvFollow)
                it.isClickable = false
            }
        } else {
            binding.tvFollow.hide()
        }

        binding.ivClose.setOnClickListener {
            activity?.finish()
        }

        binding.ivMenu.setOnClickListener {
            showStatusActiomMenu(it)
        }

        binding.layoutLike.setOnClickListener {
            likeStatus(binding.tvLike)
        }

        binding.userImage.setOnClickListener {
            openUserProfile(it)
        }
    }

    private fun openUserProfile(profileView: View) {
        FragmentWrapperActivity.userProfile(
            profileView.context,
            userStatus.studentId.orEmpty(),
            "user_status"
        )
    }

    private fun likeStatus(likeView: TextView) {
        if (mCurrentIndex < userStatus.statusItem!!.size) {
            val status = userStatus.statusItem!![mCurrentIndex]

            val isLiked = status.isLiked ?: false
            status.isLiked = !isLiked
            if (status.isLiked!!) {
                likeView.text = getString(R.string.liked)
                likeView.isSelected = true
                val params = hashMapOf<String, Any>().apply {
                    put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                    put(EventConstants.LIKE_COUNT, status.likeCount ?: 0)
                    put(EventConstants.HEADER, pageNumber)
                    put(EventConstants.ITEM_POSITION, mCurrentIndex)
                    put(EventConstants.EVENT_NAME_ID, status.getStatusId())
                    put(EventConstants.VIEWER, UserUtil.getStudentId())
                }
                sendEvent(EventConstants.STATUS_LIKED, params)
            } else {
                likeView.text = getString(R.string.like)
                likeView.isSelected = false
            }

            val body = HashMap<String, Any>().apply {
                this["id"] = status.getStatusId()
                this["type"] = "like"
                this["value"] = !isLiked
            }
            DataHandler.INSTANCE.microService.get().postStoryAction(body.toRequestBody())
                .subscribeOn(Schedulers.io()).subscribe()
            DoubtnutApp.INSTANCE.bus()?.send(StatusLiked(source, pageNumber, status.getStatusId(), !isLiked))
        }
    }

    private fun followStatus(followView: TextView) {
        DataHandler.INSTANCE.teslaRepository.followUser(userStatus.studentId.orEmpty())
            .subscribeOn(Schedulers.io()).subscribe()
        followView.text = getString(R.string.following)
        userStatus.isFollowing = true
        if (mCurrentIndex < userStatus.statusItem!!.size) {
            val params = hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                put(EventConstants.HEADER, pageNumber)
                put(EventConstants.ITEM_POSITION, mCurrentIndex)
                put(EventConstants.EVENT_NAME_ID, userStatus.statusItem!![mCurrentIndex].getStatusId())
                put(EventConstants.CREATOR, userStatus.studentId.orEmpty())
                put(EventConstants.VIEWER, UserUtil.getStudentId())
            }
            sendEvent(EventConstants.STATUS_FOLLOW_CLICK, params)
        }
        DoubtnutApp.INSTANCE.bus()?.send(StatusFollowed(source, pageNumber))
    }

    private fun deleteStatus() {
        if (mCurrentIndex < userStatus.statusItem!!.size) {
            val status = userStatus.statusItem!![mCurrentIndex]
            DataHandler.INSTANCE.microService.get().deleteStory(status.getStatusId()).subscribeOn(Schedulers.io())
                .subscribe()
            ToastUtils.makeText(requireContext(), "Status has been deleted", Toast.LENGTH_SHORT)
                .show()
            val params = hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                put(EventConstants.HEADER, pageNumber)
                put(EventConstants.ITEM_POSITION, mCurrentIndex)
                put(EventConstants.EVENT_NAME_ID, status.getStatusId())
            }
            sendEvent(EventConstants.STATUS_DELETED, params)
            DoubtnutApp.INSTANCE.bus()?.send(StatusDeleted(mCurrentIndex))
            if (userStatus.statusItem!!.size > 1) {
                pauseStatus()
                userStatus.statusItem!!.removeAt(mCurrentIndex)
                initUI()
                startViewing()
            } else {
                pauseStatus()
                onStatusListFinshListener?.moveNext()
            }
        }
    }

    private fun showStatusActiomMenu(anchor: View) {
        if (mCurrentIndex < userStatus.statusItem!!.size) {
            popupMenu = PopupMenu(anchor.context, anchor)
            val menu = R.menu.menu_feed_post
            popupMenu!!.inflate(menu)

            val status = userStatus.statusItem!![mCurrentIndex]
            if (userStatus.studentId != UserUtil.getStudentId()) {
                popupMenu!!.menu.removeItem(R.id.itemDelete)
            } else {
                popupMenu!!.menu.removeItem(R.id.itemReport)
            }
            popupMenu!!.menu.removeItem(R.id.itemMute)
            popupMenu!!.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.itemReport -> {
                        DataHandler.INSTANCE.microService.get().reportStory(status.getStatusId())
                            .subscribeOn(Schedulers.io()).subscribe()
                        ToastUtils.makeText(
                            requireContext(),
                            "This status has been reported",
                            Toast.LENGTH_SHORT
                        ).show()
                        val params = hashMapOf<String, Any>().apply {
                            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                            put(EventConstants.HEADER, pageNumber)
                            put(EventConstants.ITEM_POSITION, mCurrentIndex)
                            put(EventConstants.EVENT_NAME_ID, status.getStatusId())
                            put(EventConstants.IMAGE_URL, status.attachment!![0])
                            put(EventConstants.CREATOR, userStatus.studentId.orEmpty())
                            put(EventConstants.REPORTER, UserUtil.getStudentId())
                        }
                        sendEvent(EventConstants.STATUS_REPORTED, params)
                    }
                    R.id.itemDelete -> {
                        deleteStatus()
                    }
                }
                true
            }
            popupMenu!!.setOnDismissListener {
                resumeStatus()
            }
            popupMenu!!.show()
            pauseStatus()
        }
    }

    private fun hideStatusActionMenu() {
        activity?.runOnUiThread {
            if (popupMenu != null) {
                popupMenu!!.dismiss()
                popupMenu = null
                if (isStatusPaused) {
                    resumeStatus()
                }
            }
        }
    }

    private fun setImageStatusData() {
        binding.llStatus.removeAllViews()
        imagesList.forEach { imageUrl ->
            val imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.hide()
            imageView.loadImage(imageUrl)
            binding.llStatus.addView(imageView)
        }
    }

    private fun setProgressData() {
        binding.llProgressBar.removeAllViews()
        binding.llProgressBar.weightSum = imagesList.size.toFloat()
        imagesList.forEachIndexed { index, progressData ->
            val progressBar = ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleHorizontal
            ) //horizontal progress bar
            val params = LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, 1.0f
            )
            params.height = Utils.convertDpToPixel(2f).toInt()
            if (index < imagesList.size - 1) {
                params.marginEnd = Utils.convertDpToPixel(4f).toInt()
            }
            progressBar.layoutParams = params
            progressBar.max = 40 // max progress i am using is 40 for
            //each progress bar you can modify it
            progressBar.progressDrawable.setTint(Color.WHITE)
            progressBar.indeterminateDrawable.setTint(
                Color.parseColor("#43FFFFFF")
            )
            progressBar.progress = 0 //initial progress
            binding.llProgressBar.addView(progressBar)
        }
    }

    private fun emitStatusProgress() {
        isStatusPaused = true
        mDisposable = Observable.intervalRange(
            mCurrentProgress,
            40 - mCurrentProgress,
            0,
            100,
            TimeUnit.MILLISECONDS
        )
            .observeOn(Schedulers.computation())
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                moveToNextStatus()
            }
            .subscribe({
                updateProgress(it)
            }, {
                it.printStackTrace()
            })
    }

    private fun moveToNextStatus() {
        hideStatusActionMenu()
        mCurrentProgress = 0
        if (mCurrentIndex < imagesList.size - 1) {
            activity?.runOnUiThread {
                try {
                    (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress =
                        100
                    binding.llStatus.getChildAt(mCurrentIndex)?.hide()
                    mCurrentIndex++
                    binding.llStatus.getChildAt(mCurrentIndex)?.show()
                    setStatusMetaData(mCurrentIndex)
                    if (mCurrentIndex <= imagesList.size - 1) {
                        emitStatusProgress()
                    }
                } catch (e: Exception) {
                }
            }
        } else {
            pauseStatus()
            activity?.runOnUiThread {
                onStatusListFinshListener?.moveNext()
            }
        }
    }

    private fun moveToPreviousStatus() {

        hideStatusActionMenu()
        activity?.runOnUiThread {
            if (mCurrentIndex != 0) {
                mCurrentProgress = 0
                (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                binding.llStatus.getChildAt(mCurrentIndex)?.hide()
                mCurrentIndex--
                binding.llStatus.getChildAt(mCurrentIndex)?.show()
                setStatusMetaData(mCurrentIndex)
                if (mCurrentIndex != imagesList.size - 1)
                    emitStatusProgress()
            } else {
                if (mCurrentProgress < 10) {
                    pauseStatus()
                    onStatusListFinshListener?.movePrevious()
                } else {
                    mCurrentIndex = 0
                    mCurrentProgress = 0
                    (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                    binding.llStatus.getChildAt(mCurrentIndex)?.show()
                    setStatusMetaData(mCurrentIndex)
                    emitStatusProgress()
                }
            }
        }
    }

    private fun updateProgress(progress: Long) {
        mCurrentProgress = progress
        activity?.runOnUiThread {
            isStatusPaused = false
            binding.llProgressBar.let {
                (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress =
                    progress.toInt()
            }

        }
    }

    private fun setStatusMetaData(index: Int) {
        val statusAttachment = userStatus.statusItem!![index]

        if (userStatus.studentId != UserUtil.getStudentId()) {
            binding.tvLikeCount.hide()
            binding.tvViewCount.hide()
            binding.layoutLike.show()
            val isLiked = statusAttachment.isLiked ?: false
            if (isLiked) {
                binding.tvLike.text = getString(R.string.liked)
            } else {
                binding.tvLike.text = getString(R.string.like)
            }
            if (source == EventConstants.STATUS_SOURCE_HEADER) {
                binding.tvFollow.show()
            }
        } else {
            binding.tvFollow.hide()
            binding.layoutLike.hide()
            binding.tvLikeCount.show()
            binding.tvViewCount.show()
            binding.tvLikeCount.text = "${statusAttachment.likeCount}"
            binding.tvViewCount.text = "${statusAttachment.viewCount}"

            binding.tvLikeCount.setOnClickListener {
                binding.tvLikeCount.isEnabled = false
                openBottomSheet(UserStatusActionType.LIKE)
            }
            binding.tvViewCount.setOnClickListener {
                binding.tvViewCount.isEnabled = false
                openBottomSheet(UserStatusActionType.VIEW)
            }
        }

        markStatusViewed(statusAttachment, mCurrentIndex)

        binding.tvCaption.text = "${statusAttachment.caption}"
        binding.tvUserName.text = "${statusAttachment.userName}"

        if (!statusAttachment.createdAt.isNullOrEmpty()) {
            binding.tvStatusCreationTime.text =
                Utils.formatTime(binding.tvStatusCreationTime.context, statusAttachment.createdAt)
        } else {
            binding.tvStatusCreationTime.text = ""
        }

    }

    private fun markStatusViewed(statusAttachment: StatusAttachment, index: Int) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.VIEW_COUNT, statusAttachment.viewCount ?: 0)
            put(EventConstants.HEADER, pageNumber)
            put(EventConstants.ITEM_POSITION, index)
            put(EventConstants.EVENT_NAME_ID, statusAttachment.getStatusId())
            put(EventConstants.VIEWER, UserUtil.getStudentId())
        }
        if (userStatus.studentId != UserUtil.getStudentId()) {
            val body = HashMap<String, Any>().apply {
                this["id"] = statusAttachment.getStatusId()
                this["type"] = "view"
                this["value"] = true
            }
            DataHandler.INSTANCE.microService.get().postStoryAction(body.toRequestBody())
                .subscribeOn(Schedulers.io()).subscribe()
            params.put(EventConstants.CREATOR, statusAttachment.studentId.orEmpty())
            sendEvent(EventConstants.OTHERS_STATUS_VIEW, params)
        } else {
            sendEvent(EventConstants.MY_STATUS_VIEW, params)
        }
        statusAttachment.isViewed = true
        DoubtnutApp.INSTANCE.bus()
            ?.send(StatusViewed(source, pageNumber, mCurrentIndex, statusAttachment.getStatusId()))
    }

    private fun sendEvent(eventName: String, params: HashMap<String, Any>) {

        params.put(EventConstants.SOURCE, source)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params = params,
                ignoreFirebase = false,
                ignoreApxor = false,
                ignoreSnowplow = true
            )
        )
    }

    private fun openBottomSheet(action: String) {
        pauseStatus()
        onStatusListFinshListener?.openStatusBottomSheet(mCurrentIndex, action)
    }

    fun startViewing() {
        binding.llStatus.getChildAt(mCurrentIndex)?.let {
            binding.llStatus.getChildAt(mCurrentIndex).show()
            setStatusMetaData(mCurrentIndex)
            emitStatusProgress()
        }
    }

    var startTime: Long = System.currentTimeMillis()

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                pauseStatus()
                return@OnTouchListener true
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - startTime > 500) {
                    resumeStatus()
                } else {
                    onSingleTapClicked(event.x)
                }
                startTime = 0
                return@OnTouchListener true
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                resumeStatus()
                return@OnTouchListener true
            }
        }
        false
    }

    fun pauseStatus() {
        mDisposable?.dispose()
        mDisposable = null
        isStatusPaused = true
    }

    fun resumeStatus() {
        if (isStatusPaused) {
            emitStatusProgress()
            isStatusPaused = false
        }
    }

    private fun onSingleTapClicked(x: Float) {
        if (x < requireActivity().getScreenWidth() / 2) {
            moveToPreviousStatus()
        } else {
            moveToNextStatus()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(source: String, userStatus: UserStatus) =
            StatusDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.SOURCE, source)
                    putParcelable(Constants.DATA, userStatus)
                }
            }
    }

    interface OnStatusListPositionChangeListener {
        fun moveNext()
        fun movePrevious()
        fun openStatusBottomSheet(position: Int, action: String)
    }

    override fun onResume() {
        super.onResume()
        startViewing()
    }

    override fun onPause() {
        super.onPause()
        pauseStatus()
    }

    override fun onDestroy() {
        pauseStatus()
        statusEventObserver?.dispose()
        super.onDestroy()
    }
}