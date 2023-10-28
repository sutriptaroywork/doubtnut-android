package com.doubtnutapp.data.remote.models

data class AppEvents(
    var event: String = "",
    var status: String = "",
    var title: String = "",
    var message: String = "",
    var image: String = "",
    var button_text: String = "",
    var data: String? = "",
    var sub_title: String? = "",
    var trigger: String = "",
    var deeplink_url:String="")
