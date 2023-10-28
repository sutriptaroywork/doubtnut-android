package com.doubtnutapp.data.dictionary

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DictionaryResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("word_details") val wordDetails: List<WordDetail>?,
    @SerializedName("powered_by_text") val poweredByText: String?,
    @SerializedName("language_array") val languageArray: ArrayList<Language>?
)

@Keep
data class WordDetail(
    @SerializedName("word") val word: Word?,
    @SerializedName("meanings") val meanings: List<WordMeaning>?
)

@Keep
data class Word(
    @SerializedName("text") val text: String?,
    @SerializedName("localized") val localized: String?,
    @SerializedName("phonetic") val phonetic: String?,
    @SerializedName("audio_url") val audioUrl: String?
)

@Keep
data class WordMeaning(
    @SerializedName("partOfSpeech") val partOfSpeech: String?,
    @SerializedName("definitions") val definitions: List<WordDefinition>?
)

@Keep
data class WordDefinition(
    @SerializedName("definition") val definition: String?,
    @SerializedName("localized_text") val localizedText: String?,
    @SerializedName("example") val example: String?,
    @SerializedName("synonyms") val synonyms: List<String>?,
    @SerializedName("antonyms") val antonyms: List<String>?
)

@Keep
data class Language(
    @SerializedName("locale") val locale: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("is_selected") val isSelected: Boolean?
)
