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
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.userstatus.StatusAttachment
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.FragmentStatusAdBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatusAdFragment : DaggerFragment() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    @Inject
    lateinit var deeplinkAction: DeeplinkAction
    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    private lateinit var userStatus: UserStatus

    var onStatusListFinshListener: StatusDetailFragment.OnStatusListPositionChangeListener? = null
    var pageNumber: Int = -1
    var popupMenu: PopupMenu? = null
    var source: String = ""

    var isStatusPaused = false

    private var mDisposable: Disposable? = null
    private var mCurrentProgress: Long = 0
    private var mCurrentIndex: Int = 0
    private var imagesList: ArrayList<String> = ArrayList()

    private var statusEventObserver: Disposable? = null

    private lateinit var binding: FragmentStatusAdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userStatus = it.getParcelable<UserStatus>(Constants.DATA) as UserStatus
        }

        statusEventObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is StatusBottomSheetClosed -> {
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
        binding = FragmentStatusAdBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        binding.llStatus.setOnTouchListener(onTouchListener)

        val statusAttachment = userStatus.statusItem!![mCurrentIndex]
        markStatusViewed(statusAttachment, mCurrentIndex)
    }

    private fun initUI() {
        mCurrentIndex = 0
        mCurrentProgress = 0
        imagesList.clear()
        for (item in userStatus.statusItem!!) {
            imagesList.add(item.imgUrl.orEmpty())
        }
        setImageStatusData()
        setProgressData()

        binding.ivClose.setOnClickListener {
            activity?.finish()
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
        mCurrentProgress = 0
        if (mCurrentIndex < imagesList.size - 1) {
            activity?.runOnUiThread {
                try {
                    (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress =
                        100
                    binding.llStatus.getChildAt(mCurrentIndex)?.hide()
                    setStatusMetaData(mCurrentIndex)
                    mCurrentIndex++
                    binding.llStatus.getChildAt(mCurrentIndex)?.show()
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
        activity?.runOnUiThread {
            if (mCurrentIndex != 0) {
                mCurrentProgress = 0
                (binding.llProgressBar.getChildAt(mCurrentIndex) as? ProgressBar)?.progress = 0
                binding.llStatus.getChildAt(mCurrentIndex)?.hide()
                setStatusMetaData(mCurrentIndex)
                mCurrentIndex--
                binding.llStatus.getChildAt(mCurrentIndex)?.show()
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


    private fun sendEvent(eventName: String, params: HashMap<String, Any>) {

        params.put(EventConstants.SOURCE, source)
        analyticsPublisher.publishEvent(
            StructuredEvent(
                EventConstants.CATEGORY_STATUS,
                eventName,
                eventParams = params
            )
        )
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

    private fun setStatusMetaData(index: Int) {

        val statusAttachment = userStatus.statusItem!![index]

        if(statusAttachment.shareMessage.isNullOrEmpty()) {
            binding.ivShare.hide()
        }else{
            binding.ivShare.show()
            binding.ivShare.setOnClickListener {
                whatsAppSharing.shareOnWhatsApp(
                    ShareOnWhatApp(
                        "",
                        featureType = "",
                        campaign = "",
                        imageUrl = "",
                        controlParams = null,
                        bgColor = "#000000",
                        sharingMessage = statusAttachment.shareMessage.orEmpty(),
                        questionId = ""
                    )
                )
                whatsAppSharing.startShare(requireContext())
            }
        }

        val actions = statusAttachment.adActions
        if(actions != null){
            actions.caption?.let {
                binding.tvCaption.text = actions.caption.ctaText
                binding.tvCaption.setTextColor(Utils.parseColor(actions.caption.ctaTextColor))
                if(!actions.caption.deeplink.isNullOrEmpty()){
                    binding.tvCaption.setOnClickListener {
                        deeplinkAction.performAction(requireContext(),actions.caption.deeplink)
                    }
                }
            }

            actions.buttons?.let{
                if(actions.buttons.isNullOrEmpty()){
                    binding.llActionButtons.hide()
                }else{
                    binding.llActionButtons.show()
                    val primaryAction = actions.buttons[0]
                    binding.adActionPrimary.show()
                    binding.adActionPrimary.text = primaryAction.ctaText
                    binding.adActionPrimary.setOnClickListener {
                        deeplinkAction.performAction(requireContext(),primaryAction.deeplink)
                        sendPrimaryCTAClickedEvent(statusAttachment, index, primaryAction.ctaText.orEmpty())
                    }
                    binding.adActionPrimary.setTextColor(Utils.parseColor(primaryAction.ctaTextColor))
                    binding.adActionPrimary.setBackgroundColor(Utils.parseColor(primaryAction.ctaBgColor))


                    if(actions.buttons.size > 1){
                        val secondaryAction = actions.buttons[1]
                        binding.adActionSecondary.show()
                        binding.adActionSecondary.text = secondaryAction.ctaText
                        binding.adActionSecondary.setOnClickListener {
                            sendSecondaryCTAClickedEvent(statusAttachment, index, primaryAction.ctaText.orEmpty())
                            deeplinkAction.performAction(requireContext(),secondaryAction.deeplink)
                        }
                        binding.adActionSecondary.setTextColor(Utils.parseColor(secondaryAction.ctaTextColor))
                        binding.adActionSecondary.setBackgroundColor(Utils.parseColor(secondaryAction.ctaBgColor))
                    }else{
                        binding.adActionSecondary.hide()
                    }
                }
            }
        }
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
            StatusAdFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.SOURCE, source)
                    putParcelable(Constants.DATA, userStatus)
                }
            }
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

    private fun markStatusViewed(statusAttachment: StatusAttachment, index: Int) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.VIEW_COUNT, 0)
            put(EventConstants.HEADER, pageNumber)
            put(EventConstants.ITEM_POSITION, index)
            put(EventConstants.EVENT_NAME_ID, statusAttachment.getStatusId())
            put(EventConstants.VIEWER, UserUtil.getStudentId())
        }
        val body = HashMap<String, Any>().apply {
            this["id"] = "ad_${statusAttachment.getStatusId()}"
            this["type"] = "view"
            this["value"] = true
        }
        DataHandler.INSTANCE.microService.get().postStoryAction(body.toRequestBody())
            .subscribeOn(Schedulers.io()).subscribe()
        sendEvent(EventConstants.STATUS_AD_VIEW, params)
        statusAttachment.isViewed = true
    }

    private fun sendPrimaryCTAClickedEvent(statusAttachment: StatusAttachment, index: Int, ctaText: String) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.ITEM_POSITION, index)
            put(EventConstants.EVENT_NAME_ID, "ad_${statusAttachment.getStatusId()}")
            put(EventConstants.CTA_TEXT, ctaText)
            put(EventConstants.CTA_TYPE, "PRIMARY")
            put(EventConstants.VIEWER, UserUtil.getStudentId())
        }
        sendEvent(EventConstants.STATUS_AD_CTA_CLICKED, params)
    }

    private fun sendSecondaryCTAClickedEvent(statusAttachment: StatusAttachment, index: Int, ctaText: String) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.ITEM_POSITION, index)
            put(EventConstants.EVENT_NAME_ID, "ad_${statusAttachment.getStatusId()}")
            put(EventConstants.CTA_TEXT, ctaText)
            put(EventConstants.CTA_TYPE, "SECONDARY")
            put(EventConstants.VIEWER, UserUtil.getStudentId())
        }
        sendEvent(EventConstants.STATUS_AD_CTA_CLICKED, params)
    }

}