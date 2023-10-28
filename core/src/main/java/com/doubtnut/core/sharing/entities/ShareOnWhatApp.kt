package com.doubtnut.core.sharing.entities

import com.doubtnut.core.constant.CoreConstants

class ShareOnWhatApp(
    @JvmField val channel: String,
    @JvmField val featureType: String?,
    @JvmField val imageUrl: String?,
    @JvmField val controlParams: HashMap<String, String>?,
    @JvmField val bgColor: String? = null,
    @JvmField val sharingMessage: String?,
    @JvmField val questionId: String,
    @JvmField val campaign: String = CoreConstants.CAMPAIGN,
    @JvmField val packageName: String? = null,
    @JvmField val appName: String? = null,
    @JvmField val skipBranch: Boolean? = null,
)