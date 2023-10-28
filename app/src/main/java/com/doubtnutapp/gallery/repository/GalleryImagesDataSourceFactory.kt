package com.doubtnutapp.gallery.repository

import androidx.paging.DataSource
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by devansh on 29/10/20.
 */

class GalleryImagesDataSourceFactory(private val compositeDisposable: CompositeDisposable,
                                     private val bucketId: String? = null,
                                     private val alwaysLoadFromBeginning: Boolean = true
) : DataSource.Factory<Int, GalleryImageViewItem>() {

    override fun create(): DataSource<Int, GalleryImageViewItem> =
            GalleryImagesDataSource(compositeDisposable, bucketId, alwaysLoadFromBeginning)
}