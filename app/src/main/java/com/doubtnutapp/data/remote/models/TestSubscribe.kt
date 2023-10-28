package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
data class TestSubscribe(
    @SerializedName("test_subscription_id") val testSubscriptionId: Int?
)
