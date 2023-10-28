package com.doubtnut.noticeboard.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBadRequestDialog
import com.doubtnut.core.utils.*
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.R
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import com.doubtnut.noticeboard.data.entity.NoticeBoardItem
import com.doubtnut.noticeboard.databinding.FragmentNoticeBoardProfileBinding
import com.doubtnut.noticeboard.databinding.ItemNoticeBoardProfileExternalTitleBinding
import com.doubtnut.noticeboard.databinding.ItemNoticeBoardProfileImageTitleBinding
import com.doubtnut.noticeboard.databinding.ItemNoticeBoardVideoThumbnailTitleBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class NoticeBoardProfileFragment : DaggerFragment() {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentNoticeBoardProfileBinding
    private lateinit var viewModel: NoticeBoardProfileFragmentVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoticeBoardProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity ?: return

        init()
        initListener()
        initObserver()

        getNotices()
    }

    fun getNotices() {
        viewModel.getNotices()
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name = EventConstants.DN_NB_PROFILE_SECTION_VISIBLE,
                params = hashMapOf(
                    EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                    EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                    EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                ),
                ignoreFacebook = true
            )
        )
    }

    private fun initListener() {

    }

    private fun initObserver() {
        viewModel.noticesLiveData.observeK2(
            viewLifecycleOwner,
            ::onNoticeFetched,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.noticeWidgetLiveData.observe(viewLifecycleOwner) {
            if (!it.widgets.isNullOrEmpty()) {
                binding.rvWidgets.visible()
                val widgetAdapter = CoreApplication.INSTANCE.getWidgetLayoutAdapter(
                    context = requireContext(),
                    source = "PROFILE"
                )
                binding.rvWidgets.adapter = widgetAdapter
                widgetAdapter.addWidgets(it.widgets!!)
                binding.tvEmpty.gone()
            } else {
                binding.rvWidgets.gone()
            }
        }
    }

    private fun onNoticeFetched(data: NoticeBoardData) {
        updateProgressBarState(false)
        if (data.items.isNullOrEmpty()) {
            binding.rvNotices.gone()
            binding.tvEmpty.visible()
            binding.tvEmpty.text = data.emptyMessage
        } else {
            binding.rvNotices.visible()
            binding.tvEmpty.gone()
            binding.rvNotices.adapter = NoticeBoardProfileAdapter(
                requireContext(),
                data.items.orEmpty(),
                deeplinkAction,
                analyticsPublisher
            )
        }
        viewModel.getTodaySpecialGroups()
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast2(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = CoreBadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireContext()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(@Suppress("UNUSED_PARAMETER") state: Boolean) {}

    companion object {
        fun newInstance() = NoticeBoardProfileFragment()
    }
}

class NoticeBoardProfileAdapter(
    private val context: Context,
    val items: List<NoticeBoardItem>,
    val deeplinkAction: IDeeplinkAction,
    val analyticsPublisher: IAnalyticsPublisher
) :
    androidx.recyclerview.widget.ListAdapter<NoticeBoardItem, RecyclerView.ViewHolder>(
        DIFF_UTILS
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_IMAGE_TITLE_INT -> ImageTitleVH(
                ItemNoticeBoardProfileImageTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_VIDEO_THUMBNAIL_TITLE_INT -> ViewThumbnailTitleVH(
                ItemNoticeBoardVideoThumbnailTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_EXTERNAL_TITLE_INT -> ExternalTitleVH(
                ItemNoticeBoardProfileExternalTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ImageTitleVH(
                ItemNoticeBoardProfileImageTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageTitleVH -> holder.bind()
            is ViewThumbnailTitleVH -> holder.bind()
            is ExternalTitleVH -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            TYPE_IMAGE_TITLE -> TYPE_IMAGE_TITLE_INT
            TYPE_VIDEO_THUMBNAIL_TITLE -> TYPE_VIDEO_THUMBNAIL_TITLE_INT
            TYPE_EXTERNAL_TITLE -> TYPE_EXTERNAL_TITLE_INT
            else -> TYPE_IMAGE_TITLE_INT
        }
    }

    inner class ImageTitleVH(val binding: ItemNoticeBoardProfileImageTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvCta.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DN_NB_PROFILE_ELEMENT_CLICKED,
                        params = hashMapOf(
                            EventConstants.TYPE to items[bindingAdapterPosition].type.orEmpty(),
                            NoticeBoardConstants.NB_ID to items[bindingAdapterPosition].id.orEmpty(),
                            EventConstants.CTA_TEXT to items[bindingAdapterPosition].ctaText.orEmpty(),
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                            EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
                deeplinkAction.performAction(context, items[bindingAdapterPosition].deepLink)
            }
        }

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return

            val notice = items[bindingAdapterPosition]
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_PROFILE_ELEMENT_VIEWED,
                    params = hashMapOf(
                        EventConstants.TYPE to notice.type.orEmpty(),
                        NoticeBoardConstants.NB_ID to notice.id.orEmpty(),
                        EventConstants.CTA_TEXT to notice.ctaText.orEmpty(),
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                        EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )

            binding.tvTitle.text = notice.title
            binding.tvTitle.isVisible = !notice.title.isNullOrEmpty()

            if (!notice.imageLink.isNullOrEmpty()) {
                binding.ivImage.loadImage2(notice.imageLink)
            }
            binding.ivImage.isVisible = !notice.imageLink.isNullOrEmpty()

            binding.tvCta.text = notice.ctaText
            binding.tvCta.isVisible = !notice.ctaText.isNullOrEmpty()

        }
    }

    inner class ViewThumbnailTitleVH(val binding: ItemNoticeBoardVideoThumbnailTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DN_NB_PROFILE_ELEMENT_CLICKED,
                        params = hashMapOf(
                            EventConstants.TYPE to items[bindingAdapterPosition].type.orEmpty(),
                            NoticeBoardConstants.NB_ID to items[bindingAdapterPosition].id.orEmpty(),
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                            EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
                deeplinkAction.performAction(context, items[bindingAdapterPosition].deepLink)
            }
        }

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val notice = items[bindingAdapterPosition]
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_PROFILE_ELEMENT_VIEWED,
                    params = hashMapOf(
                        EventConstants.TYPE to notice.type.orEmpty(),
                        NoticeBoardConstants.NB_ID to notice.id.orEmpty(),
                        EventConstants.CTA_TEXT to notice.ctaText.orEmpty(),
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                        EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            if (!notice.imageLink.isNullOrEmpty()) {
                binding.ivImage.loadImage2(notice.imageLink)
            }
            binding.root.isVisible = !notice.imageLink.isNullOrEmpty()
        }
    }

    inner class ExternalTitleVH(val binding: ItemNoticeBoardProfileExternalTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvCta.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DN_NB_PROFILE_ELEMENT_CLICKED,
                        params = hashMapOf(
                            EventConstants.TYPE to items[bindingAdapterPosition].type.orEmpty(),
                            NoticeBoardConstants.NB_ID to items[bindingAdapterPosition].id.orEmpty(),
                            EventConstants.CTA_TEXT to items[bindingAdapterPosition].ctaText.orEmpty(),
                            EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                            EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                            EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )

                )
                deeplinkAction.performAction(context, items[bindingAdapterPosition].deepLink)
            }
        }

        fun bind() {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return
            val notice = items[bindingAdapterPosition]
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_PROFILE_ELEMENT_VIEWED,
                    params = hashMapOf(
                        EventConstants.TYPE to notice.type.orEmpty(),
                        NoticeBoardConstants.NB_ID to notice.id.orEmpty(),
                        EventConstants.CTA_TEXT to notice.ctaText.orEmpty(),
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        EventConstants.STUDENT_CLASS to CoreUserUtils.getStudentClass(),
                        EventConstants.BOARD to CoreUserUtils.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            binding.tvTitle.text = notice.title
            binding.tvTitle.isVisible = !notice.title.isNullOrEmpty()

            binding.tvSubtitle.text = notice.title
            binding.tvSubtitle.isVisible = !notice.subtitle.isNullOrEmpty()

            binding.tvCta.text = notice.ctaText
            binding.tvCta.isVisible = !notice.ctaText.isNullOrEmpty()
        }
    }

    companion object {
        const val TYPE_NO_INFO = "no_info"

        const val TYPE_IMAGE_TITLE = "image_title"
        private const val TYPE_IMAGE_TITLE_INT = 0

        const val TYPE_VIDEO_THUMBNAIL_TITLE = "video_thumbnail_title"
        private const val TYPE_VIDEO_THUMBNAIL_TITLE_INT = 1

        const val TYPE_EXTERNAL_TITLE = "external_title"
        private const val TYPE_EXTERNAL_TITLE_INT = 2

        val DIFF_UTILS = object : DiffUtil.ItemCallback<NoticeBoardItem>() {
            override fun areContentsTheSame(oldItem: NoticeBoardItem, newItem: NoticeBoardItem) =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: NoticeBoardItem, newItem: NoticeBoardItem) =
                oldItem.id == newItem.id
        }
    }

}