package com.doubtnutapp.gallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.gallery.repository.GalleryImagesDataSourceFactory
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by devansh on 31/10/20.
 */

class GalleryFragmentViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    var galleryImageListLiveData: LiveData<PagedList<GalleryImageViewItem>>? = null

    fun getGalleryImageItemsList(bucketId: String?, alwaysLoadFromBeginning: Boolean) {
        galleryImageListLiveData = GalleryImagesDataSourceFactory(compositeDisposable, bucketId, alwaysLoadFromBeginning)
                .toLiveData(Config(pageSize = 100, enablePlaceholders = false)
        )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}