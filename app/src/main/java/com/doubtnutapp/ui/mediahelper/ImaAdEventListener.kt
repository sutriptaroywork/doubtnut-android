package com.doubtnutapp.ui.mediahelper

/**
Created by Sachin Saxena on 07/09/22.
 */
interface ImaAdEventListener {

    fun onAdTapped(adData: ImaAdData?) {}

    fun onCompleted(adData: ImaAdData?) {}

    fun onAllAdsCompleted(adData: ImaAdData?) {}

    fun onPaused(adData: ImaAdData?) {}

    fun onResumed(adData: ImaAdData?) {}

    fun onSkipped(adData: ImaAdData?) {}

    fun onStarted(adData: ImaAdData?) {}

    fun onIconTapped(adData: ImaAdData?) {}

    fun onLoaded(adData: ImaAdData?) {}

    fun onError(adData: ImaAdData?) {}

    fun onContentPauseRequested(adData: ImaAdData?) {}

    fun onContentResumeRequested(adData: ImaAdData?) {}
}