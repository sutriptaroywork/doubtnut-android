package com.doubtnutapp.likeDislike

import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.domain.likeDislike.interactor.LikeDisLikeVideo
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class LikeDislikeVideo @Inject constructor(
        private val likeVideo: LikeDisLikeVideo,
        private val compositeDisposable: CompositeDisposable)  {



    fun likeDislikeVideo(videoId: String, screenName : String, isliked : Boolean){
        compositeDisposable + likeVideo.execute(LikeDisLikeVideo.Param(videoId, screenName, isliked)).applyIoToMainSchedulerOnCompletable().subscribe({

        }, {
            println()
        })
    }


    fun dispose() {
        compositeDisposable.dispose()
    }

}