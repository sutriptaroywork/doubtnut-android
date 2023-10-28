package com.doubtnutapp.sharing

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.sharing.IWhatsAppSharing
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.applyAnalyticsToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.whatsappsharing.interactor.GetBranchDeepLink
import com.doubtnutapp.domain.whatsappsharing.interactor.GetShareableImagePath
import com.doubtnutapp.domain.whatsappsharing.interactor.ShareOnWhatsAppInteractor
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.showToast
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class WhatsAppSharing @Inject constructor(
    private val shareOnWhatsAppInteractor: ShareOnWhatsAppInteractor,
    private val getBranchDeepLink: GetBranchDeepLink,
    private val getShareableImagePath: GetShareableImagePath,
    private val compositeDisposable: CompositeDisposable,
    private val gamificationEventManager: GamificationEventManager
) : IWhatsAppSharing {

    private val whiteColorBg: String = "#FFFFFF"
    private var progressDialog: ProgressDialog? = null
    var packageName: String? = null
    var appName: String? = null

    private val _whatsAppShareableData: MutableLiveData<Event<Triple<String?, String?, String?>>> =
        MutableLiveData()

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = _whatsAppShareableData

    private val _showWhatsAppProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val showWhatsAppProgressLiveData: LiveData<Boolean>
        get() = _showWhatsAppProgressLiveData

    private val ACTION_CHANNEL_MAPPING = mapOf(
        "video" to VIDEO_CHANNEL,
        "feed_post" to FEED_POST_CHANNEL
    )

    override fun shareOnWhatsApp(action: ShareOnWhatApp) {
        gamificationEventManager.onProfileShareClick(action.featureType ?: "")
        _showWhatsAppProgressLiveData.value = true

        packageName = action.packageName
        appName = action.appName

        if (action.skipBranch == true) {
            onDeepLinkSuccess(
                deepLink = "",
                imageUrl = action.imageUrl,
                sharingMessage = action.sharingMessage,
                bgColor = action.bgColor ?: whiteColorBg
            )
            return
        }

        val params = GetBranchDeepLink.Param(
            action.channel,
            action.campaign,
            action.featureType,
            action.controlParams
        )

        getBranchDeepLink.execute(params)
            .applyAnalyticsToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    onDeepLinkSuccess(
                        deepLink = it,
                        imageUrl = action.imageUrl,
                        sharingMessage = action.sharingMessage,
                        bgColor = action.bgColor ?: whiteColorBg
                    )
                },
                this::onBranchLinkError
            )

        shareOnWhatsAppInteractor.execute(action.questionId).applyIoToMainSchedulerOnCompletable()
            .subscribe()
    }

    fun shareOnWhatsAppFromDeeplink(
        actionDeeplink: String,
        imageUrl: String,
        sharingMessage: String,
        questionId: String?
    ) {

        val deeplinkUri = Uri.parse(actionDeeplink)
        val host = deeplinkUri.host ?: return

        // default channel for share
        var action = HOME_FEED_CHANNEL

        //if we have mapping for deeplink action to channel, use thm
        if (ACTION_CHANNEL_MAPPING.containsKey(host)) {
            action = ACTION_CHANNEL_MAPPING[host]!!
        }

        val controlParams = hashMapOf<String, String>()

        val deeplinkParams = deeplinkUri.queryParameterNames
        deeplinkParams?.forEach { param ->
            if (deeplinkUri.getQueryParameter(param) != null) {
                controlParams[param] = deeplinkUri.getQueryParameter(param)!!
            }
        }

        _showWhatsAppProgressLiveData.value = true
        val params = GetBranchDeepLink.Param(
            action,
            CoreConstants.CAMPAIGN,
            host,
            controlParams
        )

        getBranchDeepLink.execute(params)
            .applyAnalyticsToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    onDeepLinkSuccess(
                        deepLink = it,
                        imageUrl = imageUrl,
                        sharingMessage = sharingMessage,
                        bgColor = "#000000"
                    )
                },
                this::onBranchLinkError,
            )

        questionId?.let {
            shareOnWhatsAppInteractor.execute(it).applyIoToMainSchedulerOnCompletable().subscribe()
        }
    }

    fun shareOnWhatsAppFromDeeplinkWithChannelAndCampaign(
        actionDeeplink: String,
        imageUrl: String,
        sharingMessage: String,
        channelName: String,
        campaignName: String
    ) {

        val deeplinkUri = Uri.parse(actionDeeplink)
        val host = deeplinkUri.host ?: return

        val controlParams = hashMapOf<String, String>()

        val deeplinkParams = deeplinkUri.queryParameterNames
        deeplinkParams?.forEach { param ->
            if (deeplinkUri.getQueryParameter(param) != null) {
                controlParams[param] = deeplinkUri.getQueryParameter(param)!!
            }
        }

        _showWhatsAppProgressLiveData.value = true
        val params = GetBranchDeepLink.Param(
            channelName,
            campaignName,
            host,
            controlParams
        )

        getBranchDeepLink.execute(params)
            .applyAnalyticsToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    onDeepLinkSuccess(
                        deepLink = it,
                        imageUrl = imageUrl,
                        sharingMessage = sharingMessage,
                        bgColor = "#000000"
                    )
                },
                this::onBranchLinkError,
            )
    }

    private fun onBranchLinkError(throwable: Throwable) {
        _showWhatsAppProgressLiveData.value = false
        _whatsAppShareableData.value = Event(Triple(null, null, null))
    }

    private fun onDeepLinkSuccess(
        deepLink: String,
        imageUrl: String?,
        sharingMessage: String?,
        bgColor: String
    ) {
        if (imageUrl != null && imageUrl.isNotBlank()) {
            compositeDisposable + getShareableImagePath.execute(
                GetShareableImagePath.Param(
                    imageUrl,
                    bgColor,
                    BuildConfig.AUTHORITY
                )
            )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ path ->
                    _showWhatsAppProgressLiveData.value = false
                    _whatsAppShareableData.value = Event(Triple(deepLink, path, sharingMessage))
                }, {
                    _showWhatsAppProgressLiveData.value = false
                    _whatsAppShareableData.value = Event(Triple(deepLink, null, sharingMessage))
                })
        } else {
            _showWhatsAppProgressLiveData.value = false
            _whatsAppShareableData.value = Event(Triple(deepLink, null, sharingMessage))
        }
    }

    override fun startShare(context: Context) {

        _whatsAppShareableData.observeForever(
            object : Observer<Event<Triple<String?, String?, String?>>> {
                override fun onChanged(it: Event<Triple<String?, String?, String?>>?) {
                    _whatsAppShareableData.removeObserver(this)

                    it?.getContentIfNotHandled()?.let {
                        val (deepLink, imagePath, sharingMessage) = it

                        deepLink?.let {
                            shareOnWhatsApp(
                                context = context,
                                imageUrl = deepLink,
                                imageFilePath = imagePath,
                                sharingMessage = sharingMessage
                            )
                        }
                            ?: showBranchLinkError(context)
                    }

                }
            },
        )

        progressDialog = ProgressDialog.show(
            context,
            null,
            context.resources.getString(R.string.string_sharing_progress)
        )
        progressDialog?.setCancelable(true)
        _showWhatsAppProgressLiveData.observeForever(object : Observer<Boolean> {
            override fun onChanged(loading: Boolean) {
                if (!loading) {
                    if (progressDialog?.isShowing == true) {
                        progressDialog?.hide()
                    }
                    _showWhatsAppProgressLiveData.removeObserver(this)
                } else {
                    progressDialog?.show()
                }
            }
        })
    }

    fun clear() {
        compositeDisposable.clear()
    }

    override fun dispose() {
        compositeDisposable.dispose()
    }

    fun shareOnWhatsApp(
        context: Context,
        imageUrl: String,
        imageFilePath: String?,
        sharingMessage: String?
    ) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            `package` = if (packageName.isNullOrEmpty()) {
                "com.whatsapp"
            } else {
                packageName
            }

            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")

            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }

        }.also {
            if (AppUtils.isCallable(context, it)) {
                context.startActivity(it)
            } else {
                if (appName.isNullOrEmpty()) {
                    ToastUtils.makeText(
                        context,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    ToastUtils.makeText(
                        context,
                        String.format(
                            context.getString(R.string.string_install_s),
                            appName.orEmpty()
                        ),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }
        }
    }

    fun shareDoubtOnWhatsapp(context: Context, questionId: String?, message: String) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            `package` = if (packageName.isNullOrEmpty()) {
                "com.whatsapp"
            } else {
                packageName
            }

            putExtra(Intent.EXTRA_TEXT, "$questionId $message")
            type = "text/plain"
        }.also {
            if (AppUtils.isCallable(context, it)) {
                context.startActivity(it)
            } else {
                if (appName.isNullOrEmpty()) {
                    ToastUtils.makeText(
                        context,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    ToastUtils.makeText(
                        context,
                        String.format(
                            context.getString(R.string.string_install_s),
                            appName.orEmpty()
                        ),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }
        }
    }

    private fun showBranchLinkError(context: Context?) {
        context?.getString(R.string.error_branchLinkNotFound)?.let { msg ->
            showToast(context, msg)
        }
    }

}