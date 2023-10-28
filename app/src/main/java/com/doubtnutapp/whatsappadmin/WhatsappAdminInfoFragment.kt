package com.doubtnutapp.whatsappadmin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ShowWhatsappAdminForm
import com.doubtnutapp.data.remote.models.whatsappadmin.WhatsappAdminInfo
import com.doubtnutapp.databinding.FragmentWhatsappAdminInfoBinding
import com.doubtnutapp.databinding.ItemWhatsappAdminDescriptionBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import javax.inject.Inject

class WhatsappAdminInfoFragment :
    BaseBindingFragment<DummyViewModel, FragmentWhatsappAdminInfoBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var actionPerformer: ActionPerformer? = null
    private var infoFragmentData: WhatsappAdminInfo? = null

    private var source: String? = ""

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWhatsappAdminInfoBinding =
        FragmentWhatsappAdminInfoBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.info = infoFragmentData

        if (!infoFragmentData?.offerBanner.isNullOrEmpty()) {
            binding.offerBanner.loadImage(infoFragmentData!!.offerBanner)
            binding.offerBanner.show()
        } else {
            binding.offerBanner.hide()
        }

        infoFragmentData?.offerText?.let {
            binding.tvOfferText.text = HtmlCompat.fromHtml(
                infoFragmentData!!.offerText!!,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        infoFragmentData?.descriptionText?.let {
            binding.rvDescription.setHasFixedSize(true)
            binding.rvDescription.adapter =
                DescriptionAdapter(infoFragmentData!!.descriptionText)
        }

        binding.btnApply.setOnClickListener {
            val eventParams = hashMapOf<String, Any>()
            eventParams[Constants.SOURCE] = source.orEmpty()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.WHATSAPP_ADMIN_APPLY_CTA_CLICKED,
                    eventParams
                )
            )
            actionPerformer?.performAction(ShowWhatsappAdminForm())
        }
    }

    companion object {
        const val TAG = "WhatsappAdminInfoFragment"

        @JvmStatic
        fun newInstance(
            actionPerformer: ActionPerformer,
            infoFragmentData: WhatsappAdminInfo?,
            source: String?
        ) =
            WhatsappAdminInfoFragment().apply {
                this.actionPerformer = actionPerformer
                this.infoFragmentData = infoFragmentData
                this.source = source
            }
    }

    class DescriptionAdapter(val descriptionPoints: ArrayList<String>) :
        RecyclerView.Adapter<DescriptionItemVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionItemVH {

            val binding = ItemWhatsappAdminDescriptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

            return DescriptionItemVH(binding)
        }

        override fun onBindViewHolder(holder: DescriptionItemVH, position: Int) {
            holder.bind(descriptionPoints[position])
        }

        override fun getItemCount(): Int {
            return descriptionPoints.size
        }

    }

    class DescriptionItemVH(private val binding: ItemWhatsappAdminDescriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(descriptionPoint: String) {
            binding.tvText.text = descriptionPoint
        }
    }
}