package com.doubtnut.core.sharing.event

class Share(
    val message: String,
    val imageUrl: String,
    val appName: String,
    val packageName: String,
    val skipBranch: Boolean
)