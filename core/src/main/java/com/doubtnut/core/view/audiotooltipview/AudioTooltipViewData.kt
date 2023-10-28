package com.doubtnut.core.view.audiotooltipview

/**
Created by Sachin Saxena on 14/07/22.
 */
class AudioTooltipViewData private constructor(
    val audioUrl: String?,
    val muteImageUrl: String?,
    val unMuteImageUrl: String?,
    val tooltipText: String?,
    val screenName: String?
) {

    data class Builder(
        private var audioUrl: String? = null,
        private var muteImageUrl: String? = null,
        private var unMuteImageUrl: String? = null,
        private var tooltipText: String? = null,
        private var screenName: String? = null
    ) {

        fun audioUrl(url: String): Builder = apply { this.audioUrl = url }

        fun muteImageUrl(imageUrl: String?): Builder = apply { this.muteImageUrl = imageUrl }

        fun unMuteImageUrl(imageUrl: String?): Builder = apply { this.unMuteImageUrl = imageUrl }

        fun tooltipText(text: String?): Builder = apply { this.tooltipText = text }

        fun screenName(name: String): Builder = apply { this.screenName = name }

        fun build() = AudioTooltipViewData(
            audioUrl = audioUrl,
            muteImageUrl = muteImageUrl,
            unMuteImageUrl = unMuteImageUrl,
            tooltipText = tooltipText,
            screenName = screenName
        )
    }
}