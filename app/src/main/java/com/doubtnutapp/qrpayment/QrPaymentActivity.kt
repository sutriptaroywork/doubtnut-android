package com.doubtnutapp.qrpayment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PaymentCompletedEvent
import com.doubtnutapp.EventBus.QrPaymentSuccess
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityQrPaymentBinding
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.QrPayment
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.qrpayment.viewmodel.QrPaymentViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.NetworkUtils
import com.google.zxing.BarcodeFormat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_qr_payment.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.graphics.Color
import com.doubtnut.referral.ui.UiHelper

import com.google.zxing.common.BitMatrix

import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.lang.IllegalArgumentException

class QrPaymentActivity : BaseBindingActivity<QrPaymentViewModel, ActivityQrPaymentBinding>() {

    private var qrPayment: QrPayment? = null
    private var paymentStartBody: PaymentStartBody? = null
    private var bitmap: Bitmap? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var disposable: CompositeDisposable

    companion object {
        const val TAG = "QrPaymentActivity"

        const val PAYMENT_LINK_DATA = "payment_link_data"
        fun getStartIntent(
            context: Context,
            paymentStartBody: PaymentStartBody?
        ) =
            Intent(context, QrPaymentActivity::class.java).apply {
                putExtra(PAYMENT_LINK_DATA, paymentStartBody)
            }
    }

    override fun provideViewBinding(): ActivityQrPaymentBinding {
        return ActivityQrPaymentBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): QrPaymentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        paymentStartBody = intent.getParcelableExtra(PAYMENT_LINK_DATA)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QR_PAYMENT_PAGE_OPEN,
                ignoreSnowplow = true
            )
        )

        if (paymentStartBody == null) {
            finish()
        } else {
            setUpObserver()
            viewModel.fetchQrPaymentInitialData(paymentStartBody!!)
        }

        imageViewClose.setOnClickListener {
            onBackPressed()
        }

        imageViewQrBlocker.setOnClickListener {
            val orderId = qrPayment?.orderId ?: return@setOnClickListener
            viewModel.fetchQrPaymentData(orderId)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.QR_RETRY_CLICK,
                    ignoreSnowplow = true
                )
            )
        }
    }

    private fun setUpObserver() {
        viewModel.qrPaymentData.observeK(
            this,
            this::onQrPaymentFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    private fun onQrPaymentFetched(taxation: Taxation) {

        qrPayment = taxation.qrPayment

        if (qrPayment == null) return

        if (qrPayment?.paymentStatus == "SUCCESS") {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.QR_PAYMENT_SUCCESS))

            when (qrPayment?.assortmentType) {
                "course", "resource_pdf", "resource_video" -> {
                    showPaymentSuccessBottomSheet(
                        qrPayment?.assortmentType,
                        qrPayment?.assortmentId
                    )
                }
                else -> {
                    toast("Payment Done")
                    DoubtnutApp.INSTANCE.bus()?.send(PaymentCompletedEvent(QrPaymentSuccess))
                    finish()
                }
            }
        } else {
            handleTimer(qrPayment?.ttl)
            updateDataToUi(qrPayment)
        }
    }

    private fun showPaymentSuccessBottomSheet(assortmentType: String?, assortmentId: String?) {
        val dialog = PaymentSuccessfulBottomSheet.newInstance(assortmentType, assortmentId)
        dialog.show(supportFragmentManager, PaymentSuccessfulBottomSheet.TAG)
        supportFragmentManager.executePendingTransactions()
        dialog.dialog?.setOnDismissListener {
            dialog.dismiss()
            DoubtnutApp.INSTANCE.bus()?.send(PaymentCompletedEvent(QrPaymentSuccess))
            finish()
        }
    }

    private fun updateDataToUi(qrPayment: QrPayment?) {

        if (qrPayment == null) return

        textViewTitle.text = qrPayment.header
        textViewSubTitle.text = qrPayment.title
        textViewDescription.text = qrPayment.description
        imageViewQrType.loadImageEtx(qrPayment.footerImageUrl.orEmpty())
        if (!qrPayment.upiIntentLink.isNullOrBlank()) {
            bitmap = getBitmapQRforLink(qrPayment.upiIntentLink!!)
            if (bitmap != null) {
                imageViewQr.setImageBitmap(bitmap)
            } else {
                Toast.makeText(
                    applicationContext, "Error!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        btnShare.text = qrPayment.ctaText
        btnShare.setOnClickListener {
            shareImageOnWhatsApp(bitmap, qrPayment.shareMessage)
        }
    }

    private fun getBitmapQRforLink(upiLink: String): Bitmap? {
        val result: BitMatrix = try {
            MultiFormatWriter().encode(
                upiLink,
                BarcodeFormat.QR_CODE, 512, 512, null
            )
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            toast(getString(R.string.somethingWentWrong))
            return null
        }
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, 512, 0, 0, w, h)
        if (bitmap != null) {
            return bitmap
        } else {
            return null
        }
    }

    private fun shareImageOnWhatsApp(bitmap: Bitmap?, text: String?) {
        if(bitmap==null){
            ToastUtils.makeText(
                this,
                R.string.somethingWentWrong,
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            val uri = UiHelper.getImageUri(this,bitmap,"DoubtnutQR.png")
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "image/*"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, uri)
            }.also {
                if (AppUtils.isCallable(this, it)) {
                    startActivity(it)
                } else {
                    ToastUtils.makeText(
                        this,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleTimer(count: Long?) {

        if (count == null) return

        imageViewQrBlocker.hide()
        disposable.add(
            getTimerObservable(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        imageViewQrBlocker.show()
                        disposable.clear()
                    }

                    override fun onNext(t: Long) {
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

}
