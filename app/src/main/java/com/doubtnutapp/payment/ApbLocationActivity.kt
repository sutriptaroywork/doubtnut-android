package com.doubtnutapp.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.IntentUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityApbLocationBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.apb_location_items.view.*
import javax.inject.Inject

class ApbLocationActivity :
    BaseBindingActivity<ApbLocationViewModel, ActivityApbLocationBinding>() {


    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var data: ApbLocationData? = null

    private var lat: String? = null
    private var long: String? = null

    override fun provideViewBinding(): ActivityApbLocationBinding =
        ActivityApbLocationBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ApbLocationViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        initUI()
    }

    companion object {
        private const val TAG = "ApbLocationActivity"
        private const val INTENT_EXTRA_LATITUDE = "lat"
        private const val INTENT_EXTRA_LONGITUDE = "long"

        fun getStartIntent(context: Context, lat: String?, long: String?): Intent {
            return Intent(context, ApbLocationActivity::class.java).apply {
                putExtra(INTENT_EXTRA_LATITUDE, lat.orEmpty())
                putExtra(INTENT_EXTRA_LONGITUDE, long.orEmpty())
            }
        }
    }

    private fun initUI() {
        setUpObserver()
        getIntentData()
        viewModel.getApbLocationData(lat.orEmpty(), long.orEmpty())
        setAppbar()
    }

    private fun getIntentData() {
        lat = intent.getStringExtra(INTENT_EXTRA_LATITUDE)
        long = intent.getStringExtra(INTENT_EXTRA_LONGITUDE)
    }

    private fun setData() {

        if (data?.locationsList.isNullOrEmpty()) {
            binding.clNoStoresAvailable.visibility = View.VISIBLE
            binding.clStoresAvailable.visibility = View.GONE
        } else {
            binding.clNoStoresAvailable.visibility = View.GONE
            binding.clStoresAvailable.visibility = View.VISIBLE
        }

        //when nearby location available
        binding.couponCodeText.text = data?.couponText
        binding.tvCouponCode.text = data?.couponCode
        binding.tvDescription.text = data?.description
        binding.tvPointsCount.text = data?.locationCountText
        binding.tvPointsDescription.text = data?.pointsDescriptionText
        setUpRecyclerView()

        //when nearby locations not available
        binding.ivLocation.loadImageEtx(data?.locationImageUrl.orEmpty())
        binding.tvLocationTitle.text = data?.locationTitle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            binding.tvLocationDescription.text =
                Html.fromHtml(data?.locationDescription, Html.FROM_HTML_MODE_LEGACY)
        else
            binding.tvLocationDescription.text = Html.fromHtml(data?.locationDescription)
        binding.btnLocationUrl.text = data?.airtelFallBackButtonText
        binding.btnLocationUrl.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.NO_RESULT_REDIRECT_AIRTEL_WEBSITE_CLICKED))
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data?.airtelFallBackUrl)))
        }
        binding.btnGetStoreList.text = data?.sendSMSButtonText
        binding.btnGetStoreList.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.NO_RESULT_SEND_SMS_CLICKED))
            startActivity(
                Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("smsto:" + data?.sendSMSTo.orEmpty())
                )
            )
        }
    }

    private fun setUpObserver() {
        viewModel.apbLocationLiveData.observeK(
            this,
            this::onApbLocationSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        this.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        supportFragmentManager.beginTransaction()
            .add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun onApbLocationSuccess(data: ApbLocationData) {
        this.data = data
        setData()
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpRecyclerView() {
        binding.locationsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.locationsRecyclerView.adapter =
            ApbLocationAdapter(data?.locationsList.orEmpty(), analyticsPublisher)
    }
}

class ApbLocationAdapter(
    val items: List<ApbLocationListItem>,
    val analyticsPublisher: AnalyticsPublisher
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.apb_location_items, parent, false)
        return ApbLocationViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = items[position]
        (holder as ApbLocationViewHolder).bindData(data, analyticsPublisher)
    }

    class ApbLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindData(data: ApbLocationListItem, analyticsPublisher: AnalyticsPublisher) {
            itemView.ivShop.loadImageEtx(data.imageUrl.orEmpty())
            itemView.tvDistance.text = data.distance
            itemView.tvDescription.text = data.address.orEmpty() + "\n" + data.city.orEmpty()
            if (data.mobileNumber.isNullOrEmpty()) {
                itemView.btnMobile.visibility = View.GONE
                itemView.btnMobile.isClickable = false
            } else {
                itemView.btnMobile.visibility = View.VISIBLE
                itemView.btnMobile.isClickable = true
                itemView.btnMobile.text = data.mobileNumber
            }
            itemView.btnMobile.setOnClickListener {
                try {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.SHOP_NUMBER_CLICKED,
                            hashMapOf(EventConstants.SHOP_PHONE_NUMBER to data.mobileNumber.orEmpty())
                        )
                    )
                    itemView.context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:" + data.mobileNumber.orEmpty())
                        )
                    )
                } catch (e: Exception) {
                    // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                    IntentUtils.showCallActionNotPerformToast(
                        itemView.context,
                        data.mobileNumber.orEmpty()
                    )
                }
            }
        }
    }
}

@Keep
data class ApbLocationData(
    @SerializedName("title") val title: String?,
    @SerializedName("coupon_text") val couponText: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("coupon_description") val description: String?,
    @SerializedName("location_image_url") val locationImageUrl: String?,
    @SerializedName("location_description") val locationDescription: String?,
    @SerializedName("location_title") val locationTitle: String?,
    @SerializedName("points_text") val locationCountText: String?,
    @SerializedName("points_description") val pointsDescriptionText: String?,
    @SerializedName("airtel_fallback_url") val airtelFallBackUrl: String?,
    @SerializedName("airtel_fallback_buttonText") val airtelFallBackButtonText: String?,
    @SerializedName("sms_button_text") val sendSMSButtonText: String?,
    @SerializedName("airtel_number") val sendSMSTo: String?,
    @SerializedName("airtel_banks") val locationsList: List<ApbLocationListItem>?
)

@Keep
data class ApbLocationListItem(
    @SerializedName("shopName") val shopName: String?,
    @SerializedName("mobileNumber") val mobileNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("pincode") val pinCode: String?,
    @SerializedName("distance") val distance: String?,
    @SerializedName("imageUrl") val imageUrl: String?
)
