package com.doubtnut.core.view.mediaplayer

/**
Created by Sachin Saxena on 14/07/22.
 */
enum class MediaPlayerState {
    IDLE, // object of media player is just created
    INITIALIZED, // data source has been set
    PREPARING, // preparing or prepareAsync() is called
    PREPARED, // ready to play or prepareAsync() has been completed
    STARTED, // media url starts playing
    PAUSED, // paused or mediaPlayer.pause() is called
    COMPLETED, // playing of data source has been completed or got callback from onCompleteListener of Media player
    STOPPED, // stopped playing or mediaPlayer.stop() is called.
    UNINITIALIZED, // released resources associated with this MediaPlayer object, have to initialize again
}