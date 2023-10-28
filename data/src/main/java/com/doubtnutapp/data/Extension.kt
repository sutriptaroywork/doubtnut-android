package com.doubtnutapp.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject

inline fun <reified T> Gson.fromJson(string: String): T =
    this.fromJson<T>(string, object : TypeToken<T>() {}.type)

fun Map<String, Any>.toRequestBody(): RequestBody {
    return RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        JSONObject(this).toString()
    )
}

/**
 * Use this method when there is a need to send custom POJO as JSON in request body.
 * Older method does not have support for sending custom POJOs.
 */
fun Map<String, Any>.toRequestBody(gson: Gson): RequestBody {
    return RequestBody.create(
        "application/json; charset=utf-8".toMediaTypeOrNull(),
        gson.toJson(this)
    )
}
