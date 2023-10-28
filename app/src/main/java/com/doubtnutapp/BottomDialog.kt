package com.doubtnutapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.databinding.FragmentBottomDialogBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.json.JSONObject

class BottomDialog : BaseBindingBottomSheetDialogFragment<DummyViewModel, FragmentBottomDialogBinding>() {

    companion object {
        const val TAG = "BottomDialog"
        fun newInstance(event: AppEvent): BottomDialog {
            val fragment = BottomDialog()
            val args = Bundle()
            args.putParcelable("event", event)
            fragment.arguments = args
            return fragment
        }
    }

    private var mBehavior: BottomSheetBehavior<*>? = null

    private fun sendClevertapEventByQid(
        @Suppress("SameParameterValue") eventName: String,
        qid: String?,
        page: String?
    ) {
        dialog?.context?.apply {
            (dialog!!.context.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(dialog!!.context).toString())
                .addStudentId(getStudentId())
                .addEventParameter(Constants.PAGE, page ?: "")
                .addEventParameter(Constants.QUESTION_ID, qid ?: "")
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .cleverTapTrack()
        }
    }

    override fun onStart() {
        try {
            super.onStart()
            mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        } catch (e: Exception) {
            // https://console.firebase.google.com/u/0/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/c0fcfd1584a89c9e99e573a793ec259e?time=last-seven-days&sessionEventKey=614AB3B202470001027FD5BB596B69EA_1589059945534015119
        }
    }

    private fun setInAppDialogShowing(isShowing: Boolean) {
        DoubtnutApp.INSTANCE.isInAppDialogShowing = isShowing
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setInAppDialogShowing(false)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBottomDialogBinding {
        return FragmentBottomDialogBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setInAppDialogShowing(true)

        mBehavior = BottomSheetBehavior.from(binding.root.parent as View)

        if (arguments != null) {

            val event: AppEvent = requireArguments().getParcelable("event")!!

            binding.root.findViewById<TextView>(R.id.title).text = event.title
            binding.root.findViewById<TextView>(R.id.message).text = event.message
            binding.root.findViewById<Button>(R.id.btnAction).text = event.button_text

            DoubtnutApp.INSTANCE.runOnDifferentThread {
                getDatabase(context)?.eventsDao()?.delete(event)
            }

            if (event.image.isNotEmpty()) {

                // todo wrong import, change

                // dialog.progress_image.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(event.image)
                    .apply(
                        RequestOptions.placeholderOf(R.drawable.ic_profilefragment_profileplaceholder)
                            .error(R.drawable.ic_profilefragment_profileplaceholder)
                    )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //  dialog.progress_image.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //    dialog.progress_image.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.root.findViewById(R.id.image))
            }

            var data: JSONObject? = null

            if (event.data != null && event.data != "") {
                data = JSONObject(event.data!!)
            }

            binding.root.findViewById<Button>(R.id.btnAction).setOnClickListener {
                Log.d("event", event.event)
                when (event.event) {
                    "video" -> {
                        ApxorUtils.logAppEvent(
                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                            Attributes().apply {
                                putAttribute(
                                    EventConstants.QUESTION_ID,
                                    data?.getString("qid") ?: ""
                                )
                                putAttribute(EventConstants.SOURCE, TAG)
                            }
                        )

                        // Send this event to Branch
                        BranchIOUtils.userCompletedAction(
                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                            JSONObject().apply {
                                put(EventConstants.QUESTION_ID, data?.getString("qid") ?: "")
                                put(EventConstants.SOURCE, TAG)
                            }
                        )

                        if (data!!.has(Constants.NOTIFICATION_RESOURCE_TYPE) && data.getString(
                                Constants.NOTIFICATION_RESOURCE_TYPE
                            ) == SOLUTION_RESOURCE_TYPE_TEXT
                        ) {
                            val intent = TextSolutionActivity.startActivity(
                                requireActivity(),
                                data.getString("qid"),
                                "",
                                "",
                                data.getString("page"),
                                "",
                                false,
                                "",
                                "",
                                false
                            )
                            sendClevertapEventByQid(
                                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                                data.getString("qid"),
                                data.getString("page")
                            )
                            requireActivity().startActivity(intent)
                            setInAppDialogShowing(false)
                        } else {
                            val intent = VideoPageActivity.startActivity(
                                requireActivity(),
                                data.getString("qid"),
                                "",
                                "",
                                data.getString("page"),
                                "",
                                false,
                                "",
                                "",
                                false
                            )
                            sendClevertapEventByQid(
                                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                                data.getString("qid"),
                                data.getString("page")
                            )
                            requireActivity().startActivity(intent)
                            setInAppDialogShowing(false)
                        }
                    }
                    else -> {
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.action = Constants.NAVIGATE_LIBRARY
                        requireActivity().startActivity(intent)
                        setInAppDialogShowing(false)
                    }
                }
            }
        }
    }
}
