package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.VideoDialog
import com.doubtnutapp.course.widgets.StoryWidgetItem
import com.doubtnutapp.utils.Utils
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_story_detail.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class StoryDetailFragment : DaggerFragment() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var userStatus: StoryWidgetItem
    private var mLastClickTime: Long = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userStatus = it.getParcelable<StoryWidgetItem>(Constants.DATA) as StoryWidgetItem
        }

        statusEventObserver = DoubtnutApp.INSTANCE
                .bus()?.toObservable()?.subscribe {
                    when (it) {
                        is StatusBottomSheetClosed -> {
                            tvViewCount.isEnabled = true
                            emitStatusProgress()
                            isStatusPaused = false
                        }
                    }
                }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_story_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setClickListeners()
        ll_status.setOnTouchListener(onTouchListener)
    }

    private fun initUI() {
        mCurrentIndex = 0
        mCurrentProgress = 0
        imagesList.clear()
        for (item in userStatus.storyList.orEmpty()) {
            imagesList.add(item.imageUrl.orEmpty())
        }
        setImageStatusData()
        setProgressData()
    }


    private fun setClickListeners() {
        userImage.loadImage(userStatus.avatarImageUrl, R.color.grey_feed, R.color.grey_feed)

        if (source == EventConstants.STATUS_SOURCE_HEADER) {
            /*     if (userStatus.studentId != UserUtil.getStudentId()) {
                     val isFollowing = userStatus.isFollowing ?: false
                     if (isFollowing) {
                         tvFollow.text = "Following"
                     } else {
                         tvFollow.text = "Follow"
                     }
                     tvFollow.show()
                 } else {
                     tvFollow.hide()
                 }*/

        }

        ivClose.setOnClickListener {
            activity?.finish()
        }
        /*userImage.setOnClickListener {
            openUserProfile(it)
        }*/
    }

    /*  private fun openUserProfile(profileView: View) {
          FragmentWrapperActivity.userProfile(profileView.context, userStatus.studentId, "user_status")
      }
  */
    /*private fun likeStatus(likeView: TextView) {
        if (mCurrentIndex < userStatus.statusItem!!.size) {
            val status = userStatus.statusItem!![mCurrentIndex]

            val isLiked = status.isLiked ?: false
            status.isLiked = !isLiked
            if (status.isLiked!!) {
                likeView.text = "Liked"
                likeView.isSelected = true
                val params = hashMapOf<String, Any>().apply {
                    put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                    put(EventConstants.LIKE_COUNT, status.likeCount)
                    put(EventConstants.HEADER, pageNumber)
                    put(EventConstants.ITEM_POSITION, mCurrentIndex)
                    put(EventConstants.EVENT_NAME_ID, status.id)
                    put(EventConstants.VIEWER, UserUtil.getStudentId())
                }
                sendEvent(EventConstants.STATUS_LIKED, params)
            } else {
                likeView.text = "like"
                likeView.isSelected = false
            }

            val body = HashMap<String, Any>().apply {
                this["id"] = status.id
                this["type"] = "like"
                this["value"] = !isLiked
            }
            DataHandler.INSTANCE.microService.postStoryAction(body.toRequestBody()).subscribeOn(Schedulers.io()).subscribe()
            DoubtnutApp.INSTANCE.bus()?.send(StatusLiked(source, pageNumber, status.id, !isLiked))
        }
    }
*/
    /* private fun followStatus(followView: TextView) {
         DataHandler.INSTANCE.teslaRepository.followUser(userStatus.studentId).subscribeOn(Schedulers.io()).subscribe()
         followView.text = "Following"
         userStatus.isFollowing = true
         if (mCurrentIndex < userStatus.statusItem!!.size) {
             val params = hashMapOf<String, Any>().apply {
                 put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                 put(EventConstants.HEADER, pageNumber)
                 put(EventConstants.ITEM_POSITION, mCurrentIndex)
                 put(EventConstants.EVENT_NAME_ID, userStatus.statusItem!![mCurrentIndex].id)
                 put(EventConstants.CREATOR, userStatus.studentId)
                 put(EventConstants.VIEWER, UserUtil.getStudentId())
             }
             sendEvent(EventConstants.STATUS_FOLLOW_CLICK, params)
         }
         DoubtnutApp.INSTANCE.bus()?.send(StatusFollowed(source, pageNumber))
     }
 */
    /* private fun deleteStatus() {
         if (mCurrentIndex < userStatus.statusItem!!.size) {
             val status = userStatus.statusItem!![mCurrentIndex]
             DataHandler.INSTANCE.microService.deleteStory(status.id).subscribeOn(Schedulers.io()).subscribe()
             ToastUtils.makeText(requireContext(), "Status has been deleted", Toast.LENGTH_SHORT).show()
             val params = hashMapOf<String, Any>().apply {
                 put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                 put(EventConstants.HEADER, pageNumber)
                 put(EventConstants.ITEM_POSITION, mCurrentIndex)
                 put(EventConstants.EVENT_NAME_ID, status.id)
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
 */
    /*  private fun showStatusActiomMenu(anchor: View) {
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
                      DataHandler.INSTANCE.microService.reportStory(status.id).subscribeOn(Schedulers.io()).subscribe()
                      ToastUtils.makeText(requireContext(), "This status has been reported", Toast.LENGTH_SHORT).show()
                      val params = hashMapOf<String, Any>().apply {
                          put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                          put(EventConstants.HEADER, pageNumber)
                          put(EventConstants.ITEM_POSITION, mCurrentIndex)
                          put(EventConstants.EVENT_NAME_ID, status.id)
                          put(EventConstants.IMAGE_URL, status.attachment[0])
                          put(EventConstants.CREATOR, userStatus.studentId)
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
  */
    /*  private fun hideStatusActionMenu() {
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
  */
    private fun setImageStatusData() {
        ll_status.removeAllViews()
        userStatus.storyList?.forEach { story ->
            val imageView: ImageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.hide()
            imageView.loadImage(story.imageUrl)
            ll_status.addView(imageView)
        }
        if (userStatus.storyList?.get(0)?.type == "video") {
            ivPlay.show()
            ivPlay.setOnClickListener {
                playVideo(userStatus.storyList?.get(0)?.videoResource?.resource.orEmpty(), requireContext())
            }
        } else {
            ivPlay.hide()
        }
        button.text = userStatus.ctaText
        button.setBackgroundColor(Utils.parseColor(userStatus.ctaBackground.orEmpty()))
        button.setTextColor(Utils.parseColor(userStatus.ctaTextColor.orEmpty()))
        button.textSize = userStatus.ctaTextSize?.toFloat() ?: 16f
    }

    private fun setProgressData() {
        ll_progress_bar.removeAllViews()
        ll_progress_bar.weightSum = imagesList.size.toFloat()
        imagesList.forEachIndexed { index, progressData ->
            val progressBar: ProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal) //horizontal progress bar
            val params = LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            params.height = Utils.convertDpToPixel(2f).toInt()
            if (index < imagesList.size - 1) {
                params.marginEnd = Utils.convertDpToPixel(4f).toInt()
            }
            progressBar.layoutParams = params
            progressBar.max = 40 // max progress i am using is 40 for
            //each progress bar you can modify it
            progressBar.progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
            progressBar.indeterminateDrawable.setColorFilter(Color.parseColor("#43FFFFFF"), PorterDuff.Mode.MULTIPLY);
            progressBar.progress = 0 //initial progress
            ll_progress_bar.addView(progressBar)
        }
    }

    private fun emitStatusProgress() {
        isStatusPaused = true
        mDisposable = Observable.intervalRange(mCurrentProgress, 40 - mCurrentProgress, 0, 100, TimeUnit.MILLISECONDS)
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
        //hideStatusActionMenu()
        mCurrentProgress = 0
        if (mCurrentIndex < imagesList.size - 1) {
            activity?.runOnUiThread {
                try {
                    (ll_progress_bar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 100
                    ll_status.getChildAt(mCurrentIndex)?.hide()
                    mCurrentIndex++
                    if (userStatus.storyList?.get(mCurrentIndex)?.type == "video") {
                        ivPlay.show()
                        ivPlay.setOnClickListener {
                            playVideo(userStatus.storyList?.get(mCurrentIndex)?.videoResource?.resource.orEmpty(), requireContext())
                        }
                    } else {
                        ivPlay.hide()
                    }
                    ll_status.getChildAt(mCurrentIndex)?.show()
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
        // hideStatusActionMenu()
        activity?.runOnUiThread {
            if (mCurrentIndex != 0) {
                mCurrentProgress = 0
                (ll_progress_bar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                ll_status.getChildAt(mCurrentIndex).hide()
                mCurrentIndex--
                if (userStatus.storyList?.get(mCurrentIndex)?.type == "video") {
                    ivPlay.show()
                    ivPlay.setOnClickListener {
                        playVideo(userStatus.storyList?.get(mCurrentIndex)?.videoResource?.resource.orEmpty(), requireContext())
                    }
                } else {
                    ivPlay.hide()
                }
                ll_status.getChildAt(mCurrentIndex).show()
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
                    (ll_progress_bar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                    ll_status.getChildAt(mCurrentIndex).show()
                    if (userStatus.storyList?.get(mCurrentIndex)?.type == "video") {
                        ivPlay.show()
                        ivPlay.setOnClickListener {
                            playVideo(userStatus.storyList?.get(mCurrentIndex)?.videoResource?.resource.orEmpty(), requireContext())
                        }
                    } else {
                        ivPlay.hide()
                    }
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
            ll_progress_bar?.let {
                (ll_progress_bar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = progress.toInt()
            }

        }
    }

    private fun playVideo(url: String, context: Context) {
        if (System.currentTimeMillis() - mLastClickTime < 1000) return
        mLastClickTime = System.currentTimeMillis()/*
        analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.FAQ_ITEM_VIDEO_PLAY,
                        hashMapOf<String, Any>(
                                EventConstants.ITEM_ID to faqItem?.id!!,
                                EventConstants.QUESTION_ID to faqItem?.question_id!!
                        ).apply {
                            putAll(extraParams)
                        }
                )
        )*/
        // if (faqItem.video_resources != null) {
        val videoDialog = VideoDialog.newInstance(url,
                "portrait", 0,
                "")
        videoDialog.show((context as AppCompatActivity).supportFragmentManager, "VideoDialog")
        //}
    }

    private fun setStatusMetaData(index: Int) {
        val statusAttachment = userStatus.storyList!![index]
/*        if (userStatus.studentId != UserUtil.getStudentId()) {
            tvLikeCount.hide()
            tvViewCount.hide()
            layoutLike.show()
            val isLiked = statusAttachment.isLiked ?: false
            if (isLiked) {
                tvLike.text = "Liked"
            } else {
                tvLike.text = "Like"
            }
            if (source == EventConstants.STATUS_SOURCE_HEADER) {
                tvFollow.show()
            }
        } else {
            tvFollow.hide()
            layoutLike.hide()
            tvLikeCount.show()
            tvViewCount.show()
            tvLikeCount.text = "${statusAttachment.likeCount}"
            tvViewCount.text = "${statusAttachment.viewCount}"

            tvLikeCount.setOnClickListener {
                tvLikeCount.isEnabled = false
                openBottomSheet(UserStatusActionType.LIKE)
            }
            tvViewCount.setOnClickListener {
                tvViewCount.isEnabled = false
                openBottomSheet(UserStatusActionType.VIEW)
            }
        }*/

        // markStatusViewed(statusAttachment, mCurrentIndex)

        /*   tvCaption.text = "${statusAttachment.caption}"
           tvUserName.text = "${statusAttachment.userName}"

           if (!statusAttachment.createdAt.isNullOrEmpty()) {
               tvStatusCreationTime.text = Utils.formatTime(tvStatusCreationTime.context, statusAttachment.createdAt)
           } else {
               tvStatusCreationTime.text = ""
           }*/

    }

    /*   private fun markStatusViewed(statusAttachment: StatusAttachment, index: Int) {
           val params = hashMapOf<String, Any>().apply {
               put(EventConstants.TIME_STAMP, System.currentTimeMillis())
               put(EventConstants.VIEW_COUNT, statusAttachment.viewCount)
               put(EventConstants.HEADER, pageNumber)
               put(EventConstants.ITEM_POSITION, index)
               put(EventConstants.EVENT_NAME_ID, statusAttachment.id)
               put(EventConstants.VIEWER, UserUtil.getStudentId())
           }
           if (userStatus.studentId != UserUtil.getStudentId()) {
               val body = HashMap<String, Any>().apply {
                   this["id"] = statusAttachment.id
                   this["type"] = "view"
                   this["value"] = true
               }
               DataHandler.INSTANCE.microService.postStoryAction(body.toRequestBody()).subscribeOn(Schedulers.io()).subscribe()
               params.put(EventConstants.CREATOR, statusAttachment.studentId)
               sendEvent(EventConstants.OTHERS_STATUS_VIEW, params)
           } else {
               sendEvent(EventConstants.MY_STATUS_VIEW, params)
           }
           statusAttachment.isViewed = true
           DoubtnutApp.INSTANCE.bus()?.send(StatusViewed(source, pageNumber, mCurrentIndex, statusAttachment.id))
       }
   */
    private fun sendEvent(eventName: String, params: HashMap<String, Any>) {

        params.put(EventConstants.SOURCE, source)
        analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                eventName,
                eventParams = params)
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))
    }

    /* private fun openBottomSheet(action: String) {
         pauseStatus()
         onStatusListFinshListener?.openStatusBottomSheet(mCurrentIndex, action)
     }*/

    fun startViewing() {
        ll_status.getChildAt(mCurrentIndex)?.let {
            ll_status.getChildAt(mCurrentIndex).show()
            setStatusMetaData(mCurrentIndex)
            emitStatusProgress()
        }
    }

    var startTime: Long = System.currentTimeMillis()
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
        fun newInstance(source: String, userStatus: StoryWidgetItem) =
                StoryDetailFragment().apply {
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