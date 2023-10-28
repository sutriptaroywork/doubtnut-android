package com.doubtnutapp.video

/**
 * Created by Anand Gaurav on 01/11/20.
 */

typealias PlayFailedListener = (videoUrl: String, error: String?) -> Unit
typealias PlayerTypeOrMediaTypeChangedListener = (playerType: String, mediaType: String) -> Unit
typealias SwitchToTimeShiftListener = (position: Long) -> Unit
typealias GoLiveListener = () -> Unit
typealias OpenWebViewOnVideoFail = () -> Unit