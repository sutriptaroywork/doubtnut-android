package com.doubtnutapp.payment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants.NEAREST_STORE_CLICKED
import com.doubtnut.analytics.EventConstants.PERMISSION_ALLOW
import com.doubtnut.analytics.EventConstants.PERMISSION_DENY
import com.doubtnut.analytics.EventConstants.SEND_SMS_CLICKED
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.Constants.MY_PERMISSIONS_REQUEST_LOCATION
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityApbCashPaymentBinding
import com.doubtnutapp.liveclass.viewmodel.ApbCashPaymentViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.apb_registration_item_1.view.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class ApbCashPaymentActivity :
    BaseBindingActivity<ApbCashPaymentViewModel, ActivityApbCashPaymentBinding>() {


    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher


    private var mediaPlayer: MediaPlayer? = null

    private var data: ApbCashPaymentData? = null

    private var lat: String? = null
    private var long: String? = null

    private var timer: Timer? = null


    companion object {
        private const val TAG = "ApbCashPaymentActivity"

        fun getStartIntent(context: Context) = Intent(context, ApbCashPaymentActivity::class.java)

    }

    override fun provideViewBinding(): ActivityApbCashPaymentBinding =
        ActivityApbCashPaymentBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ApbCashPaymentViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        initUI()
    }




    private fun initUI() {

        setAppbar()
        setUpObserver()
        viewModel.getApbCashPaymentData()
    }


    private fun setData() {

        setUpRecyclerView()

        binding.couponCodeText.text = data?.couponText
        binding.tvCouponCode.text = data?.couponCode
        binding.airtelImage.loadImageEtx(data?.airtelImageUrl.orEmpty())
        binding.btnSearchNearestStore.text = data?.searchButtonText
        binding.btnGetStoreList.text = data?.smsButtonText
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.description.text = Html.fromHtml(data?.description, Html.FROM_HTML_MODE_LEGACY)
            binding.btnSendSMS.text =
                Html.fromHtml(data?.smsDescriptionText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.description.text = Html.fromHtml(data?.description)
            binding.btnSendSMS.text = Html.fromHtml(data?.smsDescriptionText)
        }
        binding.tvRecyclerViewTitle.text = data?.heading
        binding.btnVoiceInstructions.text = data?.voiceTitle
        binding.btnVoiceInstructions.setIconResource(R.drawable.ic_volume)
        binding.btnVoiceInstructions.iconTint =
            AppCompatResources.getColorStateList(this, R.color.blue_273DE9)

        if (data?.showVoiceInstructions == true) {
            binding.btnVoiceInstructions.visibility = View.VISIBLE
            binding.btnVoiceInstructions.isClickable = true

            val timeLag: Long = if (data?.voiceUrlLagTime.isNullOrEmpty())
                0
            else
                data?.voiceUrlLagTime?.toLong()!!

            timer = Timer()
            timer!!.schedule(delay = timeLag) {
                if (mediaPlayer == null) {
                    setupMediaPlayer()
                } else {
                    handleMediaPlayer()
                }
            }

        } else {
            binding.btnVoiceInstructions.visibility = View.GONE
            binding.btnVoiceInstructions.isClickable = false
        }

        binding.btnSearchNearestStore.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(NEAREST_STORE_CLICKED, ignoreSnowplow = true))
            getUserLocation()
        }

        binding.btnGetStoreList.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(SEND_SMS_CLICKED))
            startActivity(
                Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("smsto:" + data?.sendSmsTo.orEmpty())
                )
            )
        }

        binding.btnVoiceInstructions.setOnClickListener {
            if (mediaPlayer == null) {
                setupMediaPlayer()
            } else {
                handleMediaPlayer()
            }
        }
    }

    private fun setUpObserver() {
        viewModel.apbCashPaymentLiveData.observeK(this,
                this::onApbCashPaymentSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)
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
        supportFragmentManager.beginTransaction().add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun onApbCashPaymentSuccess(data: ApbCashPaymentData) {
        this.data = data
        setData()
    }

    private fun setAppbar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolBar.setNavigationOnClickListener {
            releaseMediaPlayer()
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        releaseMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
        releaseMediaPlayer()
    }

    private fun handleMediaPlayer() {

        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            runOnUiThread {
                binding.btnVoiceInstructions.apply {
                    setIconResource(R.drawable.ic_volume_off)
                    iconTint = AppCompatResources.getColorStateList(context, R.color.blue_273DE9)
                }
            }
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            runOnUiThread {
                binding.btnVoiceInstructions.apply {
                    setIconResource(R.drawable.ic_volume)
                    iconTint = AppCompatResources.getColorStateList(context, R.color.blue_273DE9)
                }
            }
        } else {
            releaseMediaPlayer()
            setupMediaPlayer()
        }
    }

    private fun setupMediaPlayer() {

        if (data?.voiceUrl.isNullOrBlank())
            return

        mediaPlayer = null
        mediaPlayer = MediaPlayer().apply {
            setDataSource(data?.voiceUrl)
            prepareAsync()
        }

        mediaPlayer?.setOnPreparedListener {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location: Location?
                when {
                    locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                        location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        useLocation(location)
                    }
                    locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                        location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        useLocation(location)
                    }
                    else -> {
                        //redirect user to turn on location
                        showLocationSettingsDialog()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "getUserLocation: ${e.stackTrace}")
            }
        } else {
            requestPermission()
        }
    }

    private fun useLocation(location: Location?) {
        if (location != null) {
            lat = location.latitude.toString()
            long = location.longitude.toString()
            val intent = ApbLocationActivity.getStartIntent(this, lat.orEmpty(), long.orEmpty())
            startActivity(intent)
        } else {
            val intent = ApbLocationActivity.getStartIntent(this, "", "")
            startActivity(intent)
        }
    }

    private fun showLocationSettingsDialog() {

        val builder = AlertDialog.Builder(this)
                .setTitle(data?.dialogTitle.orEmpty())
                .setMessage(data?.dialogDescription.orEmpty())
                .setCancelable(false)
                .setPositiveButton(data?.dialogPositiveButtonText.orEmpty()) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(data?.dialogNegativeButtonText.orEmpty()) { dialog, _ ->
                    dialog.cancel()
                }
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
            } else {
                showToast(this, "Please enable location permission to use this feature")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
                analyticsPublisher.publishEvent(AnalyticsEvent(PERMISSION_ALLOW))
            } else {
                showToast(this, "Please enable location permission to use this feature")
                analyticsPublisher.publishEvent(AnalyticsEvent(PERMISSION_DENY))
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.processRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.processRecyclerView.adapter = ApbCashPaymentAdapter(data?.onBoardingList.orEmpty())
    }


}

class ApbCashPaymentAdapter(val items: List<ApbCashPaymentListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.apb_registration_item_1, parent, false)
            return ApbCashPaymentViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.apb_registration_item_2, parent, false)
            return ApbCashPaymentViewHolder(view)
        }

    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return position % 2
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = items[position]
        if (holder.itemViewType == 0) {
            (holder as ApbCashPaymentViewHolder).bindDataItem1(data)
        } else {
            (holder as ApbCashPaymentViewHolder).bindDataItem2(data)
        }
    }

    class ApbCashPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindDataItem1(data: ApbCashPaymentListItem) {
            itemView.image.loadImageEtx(data.imageUrl.orEmpty())
            itemView.tvStep.text = data.step.orEmpty()
            itemView.tvTitle.text = data.title.orEmpty()
            itemView.tvSubTitle.text = data.subTitle.orEmpty()
        }

        fun bindDataItem2(data: ApbCashPaymentListItem) {
            itemView.image.loadImageEtx(data.imageUrl.orEmpty())
            itemView.tvStep.text = data.step.orEmpty()
            itemView.tvTitle.text = data.title.orEmpty()
            itemView.tvSubTitle.text = data.subTitle.orEmpty()
        }
    }
}

@Keep
data class ApbCashPaymentData(
        @SerializedName("title") val title: String?,
        @SerializedName("airtel_image_url") val airtelImageUrl: String?,
        @SerializedName("coupon_text") val couponText: String?,
        @SerializedName("coupon_code") val couponCode: String?,
        @SerializedName("coupon_description") val description: String?,
        @SerializedName("search_button_text") val searchButtonText: String?,
        @SerializedName("sms_button_text") val smsButtonText: String?,
        @SerializedName("send_sms_text") val smsDescriptionText: String?,
        @SerializedName("airtel_number") val sendSmsTo: String?,
        @SerializedName("heading") val heading: String?,
        @SerializedName("show_voice_instructions") val showVoiceInstructions: Boolean?,
        @SerializedName("voice_title") val voiceTitle: String?,
        @SerializedName("voice_url") val voiceUrl: String?,
        @SerializedName("voice_url_lag_time") val voiceUrlLagTime: String?,
        @SerializedName("steps") val onBoardingList: List<ApbCashPaymentListItem>?,
        @SerializedName("dialog_title") val dialogTitle: String?,
        @SerializedName("dialog_description") val dialogDescription: String?,
        @SerializedName("dialog_neg_botton") val dialogNegativeButtonText: String?,
        @SerializedName("dialog_pos_button") val dialogPositiveButtonText: String?
)

@Keep
data class ApbCashPaymentListItem(
        @SerializedName("step") val step: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("sub_title") val subTitle: String?,
        @SerializedName("image_url") val imageUrl: String?
)



