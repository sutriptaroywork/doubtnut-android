package com.doubtnutapp.db

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import com.doubtnutapp.data.homefeed.model.db.AnnouncementEntity
import com.doubtnutapp.downloadedVideos.OfflineMediaStatus
import com.doubtnutapp.scheduledquiz.di.model.QuizImageList
import com.doubtnutapp.scheduledquiz.di.model.QuizSubject
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object TypeConverter {

    private val gson = Gson()

    @SuppressLint("SimpleDateFormat")
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    @JvmStatic
    @TypeConverter
    fun toDate(value: String): Date? {
        return simpleDateFormat.parse(value)
    }

    @JvmStatic
    @TypeConverter
    fun fromDate(date: Date): String {
        return simpleDateFormat.format(date)
    }

    @TypeConverter
    @JvmStatic
    fun toAnnouncementEntity(value: String?): AnnouncementEntity? {
        return gson.fromJson(
            value,
            object : TypeToken<AnnouncementEntity>() {
            }.type
        )
    }

    @TypeConverter
    @JvmStatic
    fun fromAnnouncementEntity(value: AnnouncementEntity?): String? {
        return gson.toJson(value)
    }

    @JvmStatic
    @TypeConverter
    fun toJsonArray(value: String): JsonArray {
        return JsonArray(2)
    }

    @JvmStatic
    @TypeConverter
    fun fromJsonArray(jsonArray: JsonArray): String {
        return jsonArray.toString()
    }

    @JvmStatic
    @TypeConverter
    fun fromOfflineVideoStatusInt(status: Int): OfflineMediaStatus {
        return when (status) {
            1 -> OfflineMediaStatus.DOWNLOADED
            0 -> OfflineMediaStatus.DOWNLOADING
            2 -> OfflineMediaStatus.EXPIRED
            3 -> OfflineMediaStatus.INITIAL
            else -> OfflineMediaStatus.FAILURE
        }
    }

    @JvmStatic
    @TypeConverter
    fun toOfflineVideoStatusToInt(status: OfflineMediaStatus) = status.code

    @TypeConverter
    @JvmStatic
    fun fromQuizImageList(values: List<QuizImageList>?): String? {
        if (values == null)
            return null
        val type = object : TypeToken<List<QuizImageList?>?>() {}.type
        return gson.toJson(values, type)
    }

    @TypeConverter
    @JvmStatic
    fun toQuizImageList(optionValuesString: String?): List<QuizImageList?>? {
        if (optionValuesString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<QuizImageList?>?>() {}.type
        return gson.fromJson(optionValuesString, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromQuizSubjectList(values: List<QuizSubject>?): String? {
        if (values == null)
            return null
        val type = object : TypeToken<List<QuizSubject?>?>() {}.type
        return gson.toJson(values, type)
    }

    @TypeConverter
    @JvmStatic
    fun toQuizSubjectList(optionValuesString: String?): List<QuizSubject?>? {
        if (optionValuesString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<QuizSubject?>?>() {}.type
        return gson.fromJson(optionValuesString, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromQuizString(values: List<String>?): String? {
        if (values == null)
            return null
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.toJson(values, type)
    }

    @TypeConverter
    @JvmStatic
    fun toQuizString(optionValuesString: String?): List<String?>? {
        if (optionValuesString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(optionValuesString, type)
    }
}
