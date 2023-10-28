package com.doubtnutapp.leaderboard.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.EventBus.VideoSeekEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.DialogLeaderboardHelpBottomSheetBinding
import com.doubtnutapp.databinding.ItemHelpBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.leaderboard.data.entity.LeaderboardHelp
import com.doubtnutapp.leaderboard.data.entity.LeaderboardHelpItem
import com.doubtnutapp.libraryhome.coursev3.ui.VideoHolderActivity
import com.doubtnutapp.model.Video
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.model.VideoResource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class LeaderboardHelpBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var binding: DialogLeaderboardHelpBottomSheetBinding

    private val leaderboardHelp: LeaderboardHelp
        get() = requireArguments().getParcelable(KEY_LEADERBOARD_HELP)!!

    private val assortmentId by lazy {
        requireArguments().getString(Constants.ASSORTMENT_ID).orEmpty()
    }

    private val testId by lazy {
        requireArguments().getString(Constants.TEST_ID).orEmpty()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLeaderboardHelpBottomSheetBinding.inflate(
            inflater,
            container,
            false
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initListeners()
    }

    fun init() {
        binding.ivHeading.loadImage(leaderboardHelp.headingImage)
        binding.tvHeading.text = leaderboardHelp.heading
        binding.rvHelp.adapter = LeaderboardHelpAdapter(leaderboardHelp.items.orEmpty())

        leaderboardHelp.videoButtonCtaText
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                binding.ivPlay.show()
                binding.tvPlayVideo.show()
                binding.tvPlayVideo.text = it
                binding.ivPlay.setOnClickListener { binding.tvPlayVideo.performClick() }
                binding.tvPlayVideo.setOnClickListener(
                    object : DebouncedOnClickListener(800) {
                        override fun onDebouncedClick(v: View?) {
                            if (leaderboardHelp.videoInfo != null) {
                                if (!leaderboardHelp.videoInfo?.videoResources.isNullOrEmpty()) {
                                    val video = Video(
                                        leaderboardHelp.videoInfo?.questionId,
                                        true,
                                        leaderboardHelp.videoInfo?.viewId,
                                        leaderboardHelp.videoInfo?.videoResources?.map { apiResource ->
                                            VideoResource(
                                                resource = apiResource.resource,
                                                drmScheme = apiResource.drmScheme,
                                                drmLicenseUrl = apiResource.drmLicenseUrl,
                                                mediaType = apiResource.mediaType,
                                                isPlayed = false,
                                                dropDownList = null,
                                                timeShiftResource = null,
                                                offset = apiResource.offset
                                            )
                                        },
                                        0,
                                        leaderboardHelp.videoInfo?.page,
                                        false,
                                        VideoFragment.DEFAULT_ASPECT_RATIO
                                    )
                                    DoubtnutApp.INSTANCE.bus()?.send(PlayAudioEvent(true))
                                    val intent = VideoHolderActivity.getStartIntent(
                                        requireContext(),
                                        video
                                    )
                                    startActivity(intent)
                                } else {
                                    DoubtnutApp.INSTANCE.bus()?.send(
                                        VideoSeekEvent(
                                            leaderboardHelp.videoInfo?.position?.toLongOrNull()
                                                ?: 0
                                        )
                                    )
                                }
                            }

                        }

                    }
                )
            }

        binding.btnAction.text = leaderboardHelp.buttonCtaText
        binding.btnAction.setOnClickListener {
            deeplinkAction.performAction(requireContext(), leaderboardHelp.deeplink)
            analyticsPublisher.publishEvent(
                hashMapOf<String, Any>(
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.ASSORTMENT_ID to assortmentId,
                    EventConstants.TEST_ID to testId
                ).let {
                    AnalyticsEvent(
                        EventConstants.TEST_LEADERBOARD_GOTO_TESTS_CLICK,
                        it, ignoreSnowplow = true
                    )
                }
            )
        }
    }

    fun initListeners() {

        binding.ivClose.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "LeaderboardHelpBottomSheetDialogFragment"

        const val KEY_LEADERBOARD_HELP = "key_leaderboard_help"

        fun newInstance(
            leaderboardHelp: LeaderboardHelp,
            assortmentId: String?,
            testId: String?
        ) =
            LeaderboardHelpBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_LEADERBOARD_HELP, leaderboardHelp)
                    putString(Constants.ASSORTMENT_ID, assortmentId)
                    putString(Constants.TEST_ID, testId)
                }
            }
    }
}

class LeaderboardHelpAdapter(
    val items: List<LeaderboardHelpItem>
) :
    RecyclerView.Adapter<LeaderboardHelpAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHelpBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemHelpBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val item = items[bindingAdapterPosition]
            binding.tvIndex.text = "${bindingAdapterPosition + 1}."
            binding.tvHelp.text = item.title
        }
    }
}

