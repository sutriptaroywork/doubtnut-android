package com.doubtnutapp.ui.mediahelper

/**
Created by Sachin Saxena on 07/09/22.
 */
class ImaAdData private constructor(
    val adId: String?, // the ad ID as specified in the VAST response.
    val adPosition: Int?, // the position of the ad within the pod. The value returned is one-based, for example, 1 of 2, 2 of 2, etc. If the ad is not part of a pod, this will return 1.
    val totalAd: Int?, // the total number of ads contained within this pod, including bumpers. If the ad is not part of a pod, this will return 1.
    val creativeId: String?, // the ID of the selected creative for the ad,
    val creativeAdId: String?, // the ISCI (Industry Standard Commercial Identifier) code for an ad. This is the Ad-ID of the selected creative in the VAST response.
    val contentType: String?, // the content type of the currently selected creative, or null if no creative is selected or the content type is unavailable. For linear ads, the content type is only going to be available after the START event, when the media file is selected.
    val width: Int?, // the width of the selected creative if non-linear, else returns 0.
    val height: Int?, // the height of the selected creative if non-linear, else returns 0.
    val title: String?, // the title of this ad from the VAST response.
    val description: String?, // the description of this ad from the VAST response.
    val duration: Double?, // the duration of the ad in seconds, -1 if not available.
    val skippable: Boolean?, // Indicates whether the ad can be skipped by the user.
    val skipTimeOffset: Double?, // the number of seconds of playback before the ad becomes skippable. -1 is returned for non-skippable ads or if this is unavailable.
    val errorMessage: String? // error message when ad fails to load
) {

    data class Builder(
        private var adId: String? = null,
        private var adPosition: Int? = null,
        private var totalAd: Int? = null,
        private var creativeId: String? = null,
        private var creativeAdId: String? = null,
        private var contentType: String? = null,
        private var width: Int? = null,
        private var height: Int? = null,
        private var title: String? = null,
        private var description: String? = null,
        private var duration: Double? = null,
        private var skippable: Boolean? = null,
        private var skipTimeOffset: Double? = null,
        private var errorMessage: String? = null,
    ) {

        fun adId(id: String): Builder = apply { this.adId = id }

        fun adPosition(position: Int?): Builder = apply { this.adPosition = position }

        fun totalAd(count: Int?): Builder = apply { this.totalAd = count }

        fun creativeId(id: String?): Builder = apply { this.creativeId = id }

        fun creativeAdId(id: String?): Builder = apply { this.creativeAdId = id }

        fun contentType(type: String?): Builder = apply { this.contentType = type }

        fun width(width: Int): Builder = apply { this.width = width }

        fun height(height: Int): Builder = apply { this.height = height }

        fun title(title: String?): Builder = apply { this.title = title }

        fun description(description: String?): Builder = apply { this.description = description }

        fun duration(duration: Double?): Builder = apply { this.duration = duration }

        fun isSkippable(isSkippable: Boolean?): Builder = apply { this.skippable = isSkippable }

        fun skipTimeOffset(skipTimeOffset: Double?): Builder =
            apply { this.skipTimeOffset = skipTimeOffset }

        fun errorMessage(message: String?): Builder = apply { this.errorMessage = message }

        fun build() = ImaAdData(
            adId = adId,
            adPosition = adPosition,
            totalAd = totalAd,
            creativeId = creativeId,
            creativeAdId = creativeAdId,
            contentType = contentType,
            width = width,
            height = height,
            title = title,
            description = description,
            duration = duration,
            skippable = skippable,
            skipTimeOffset = skipTimeOffset,
            errorMessage = errorMessage
        )
    }
}