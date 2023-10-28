package com.doubtnutapp.utils

import android.content.Context
import com.doubtnutapp.*
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.FreshchatConfig

object ChatUtil {

    private var isFreshWorksInitialized: Boolean = false

    @Synchronized
    fun initializeFreshWorks(applicationContext: Context) {
        if (isFreshWorksInitialized) return
        val config = FreshchatConfig(
            "79998186-e047-4176-acfb-fa6fbcda9a76",
            "25a8bc45-19d6-40fc-9ca9-1067187e8663"
        )
        config.domain = "msdk.in.freshchat.com"
        Freshchat.getInstance(applicationContext).init(config)
        isFreshWorksInitialized = true

        sendToken(applicationContext, defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID,"").orDefaultValue())
    }

    fun startConversation(context: Context) {
        initializeFreshWorks(context.applicationContext)
        restoreUser(context)
        Freshchat.showConversations(context)
    }

    fun sendToken(context: Context, token: String) {
        initializeFreshWorks(context.applicationContext)
        Freshchat.getInstance(context).setPushRegistrationToken(token)
    }

    fun setUser(
        context: Context,
        assortmentId: String?,
        variantId: String,
        courseName: String,
        duration: String,
        source: String = "",
    ) {
        initializeFreshWorks(context.applicationContext)
        // Get the user object for the current installation
        val freshchatUser = Freshchat.getInstance(context).user
        freshchatUser.firstName = UserUtil.getStudentName()
        freshchatUser.setPhone("+91", UserUtil.getPhoneNumber())

        // Call setUser so that the user information is synced with Freshchat's servers
        Freshchat.getInstance(context).user = freshchatUser

        val userMeta: MutableMap<String, String> = HashMap()
        userMeta["class"] = UserUtil.getStudentClass()
        userMeta["student id"] = UserUtil.getStudentId()
        userMeta["language"] = UserUtil.getUserLanguage()
        userMeta["board"] = UserUtil.getUserBoard()
        userMeta["ccm_id"] = UserUtil.getUserCcmId()
        userMeta["assortmentId"] = assortmentId.orEmpty()
        userMeta["variantId"] = variantId
        userMeta["course Name"] = courseName
        userMeta["course Duration"] = duration
        userMeta["chat_source"] = source

        //Call setUserProperties to sync the user properties with Freshchat's servers
        Freshchat.getInstance(context).setUserProperties(userMeta)
    }

    fun resetUser(context: Context) {
        initializeFreshWorks(context.applicationContext)
        Freshchat.resetUser(context)
    }

    fun isChatEnabled(context: Context): Boolean {
        return FeaturesManager.isFeatureEnabled(context, Features.FRESHCHAT_CHAT)
    }

    fun isChatEnabledForHamburger(context: Context): Boolean {
        return FeaturesManager.isFeatureEnabled(context, Features.FRESHCHAT_CHAT_HAMBURGER)
    }

    private fun restoreUser(context: Context) {
        initializeFreshWorks(context.applicationContext)
        val externalId: String = UserUtil.getStudentId()//  You app's external id

        val restoreId: String = UserUtil.getUserRestoreId() //  Get restore id from storage

        if (restoreId == null || restoreId.isEmpty()) {
            Freshchat.getInstance(context).identifyUser(externalId, null)
        } else {
            Freshchat.getInstance(context).identifyUser(externalId, restoreId)
        }
    }

}