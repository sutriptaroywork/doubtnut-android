package com.doubtnutapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.core.content.edit
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.defaultPrefs
import io.branch.referral.Branch
import io.branch.referral.Branch.BranchReferralInitListener
import org.json.JSONObject

object BranchIOUtils {

    const val TAG = "BranchSDK"

    fun init(context: Context) {
        if (BuildConfig.DEBUG) {
            Branch.enableLogging()
        }
        Branch.getAutoInstance(context)
    }

    fun initSession(context: Activity, data: Uri?) {
        Branch.sessionBuilder(context).withCallback(branchReferralInitListener)
            .withData(data).init()
    }

    private val branchReferralInitListener =
        BranchReferralInitListener { referringParams, error ->
            if (error == null) {
                onSessionInitSuccess(referringParams)
            } else {
                onSessionInitError()
            }
        }

    fun reInitBranchSession(context: Activity) {
        Branch.sessionBuilder(context).withCallback(branchReferralInitListener).reInit()
    }

    @SuppressLint("HardwareIds")
    private fun onSessionInitSuccess(referringParams: JSONObject?) {
        referringParams?.put(
            Constants.KEY_UDID,
            Settings.Secure.getString(
                DoubtnutApp.INSTANCE.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )
        defaultPrefs().edit {
            putString(Constants.REFFERING_PARAMS, referringParams.toString())
        }
    }

    @SuppressLint("HardwareIds")
    private fun onSessionInitError() {
        val uid = Settings.Secure.getString(
            DoubtnutApp.INSTANCE.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val params = JSONObject()
        params.put(Constants.KEY_UDID, uid)
        params.put("+clicked_branch_link", false)
        defaultPrefs().edit {
            putString(Constants.REFFERING_PARAMS, params.toString())
        }
    }

    fun clearReferringParam(context: Context) {
        defaultPrefs(context).edit {
            putString(Constants.REFFERING_PARAMS, "")
        }
    }

    fun getReferringParam(context: Context) =
        defaultPrefs(context).getString(Constants.REFFERING_PARAMS, null)

//    fun createBranchLink(
//        context: Context,
//        title: String,
//        description: String,
//        dataMap: HashMap<String, String>,
//        callback: (String?, BranchError?) -> Unit
//    ) {
//        val branchUniversalObject = BranchUniversalObject()
//            .setTitle(title)
//            .setContentDescription(description)
//            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
//            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
//
//        val linkProperties = LinkProperties()
//            .setChannel("share")
//            .setFeature("content_share")
//            .setCampaign("content_share_app")
//
//        dataMap.forEach { (key, value) ->
//            linkProperties.addControlParameter(key, value)
//        }
//
//        branchUniversalObject.generateShortUrl(context, linkProperties) { url, error ->
//            callback(url, error)
//        }
//    }

    fun userCompletedAction(action: String, metadata: JSONObject?) {
        Branch.getInstance().userCompletedAction(
            action,
            metadata
        )
    }

    fun setIdentity(userId: String) {
        Branch.getInstance().setIdentity(userId)
    }
}