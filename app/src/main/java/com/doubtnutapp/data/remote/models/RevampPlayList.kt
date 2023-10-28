package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by pradip on
 * 05, May, 2019
 **/
@Parcelize
data class RevampPlayList(
    @SerializedName("playlist") val playlist: ArrayList<QuestionMeta>,
    @SerializedName("header") val header: ArrayList<LibHeader>,
    @SerializedName("meta_info") val metaInfo: ArrayList<LibMetaInfo>,
    @SerializedName("library_playlist_id") val libraryPlayListId: String?

) : Parcelable {

    @Parcelize
    data class LibMetaInfo(

        @SerializedName("icon") val icon: String,
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String,
        @SerializedName("Button") val suggestionButtonText: String,
        @SerializedName("id") val suggestionId: String,
        @SerializedName("playlist_name") val suggestionName: String

    ) : Parcelable

    @Parcelize
    data class LibHeader(

        @SerializedName("id") val headerId: String,
        @SerializedName("name") val headerTitle: String,
        @SerializedName("image_url") val headerImageUrl: String,
        @SerializedName("is_last") val headerIsLast: Int,
        @SerializedName("description") val headerDescription: String

    ) : Parcelable
}
