package com.doubtnutapp.gallery.repository

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.database.getStringOrNull
import androidx.paging.PositionalDataSource
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.plus
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by devansh on 28/10/20.
 */

/**
 * PositionalDataSource to get images in a paginated fashion,
 * sorted according to time from devices's external storage
 *
 * @param compositeDisposable Composite disposable to dispose requests
 * @param bucketId [MediaStore.Images.Media.BUCKET_ID] of the bucket from where to get the images
 * @param alwaysLoadFromBeginning Force initial load from position 0 if set to true, else load initial
 * data from the position provided by [PositionalDataSource.LoadInitialParams.requestedStartPosition]
 */
class GalleryImagesDataSource(
    private val compositeDisposable: CompositeDisposable,
    private val bucketId: String? = null,
    private val alwaysLoadFromBeginning: Boolean = false
) : PositionalDataSource<GalleryImageViewItem>() {

    companion object {
        const val PAGE_SIZE = 20
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<GalleryImageViewItem>
    ) {
        val startPosition = if (alwaysLoadFromBeginning) 0 else params.requestedStartPosition
        getGalleryImages(startPosition, params.requestedLoadSize, true) {
            callback.onResult(it, startPosition)
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<GalleryImageViewItem>
    ) {
        getGalleryImages(params.startPosition, params.loadSize) {
            callback.onResult(it)
        }
    }

    private fun getGalleryImages(
        offset: Int, limit: Int, addDemoQuestion: Boolean = false,
        onSuccess: (List<GalleryImageViewItem>) -> Unit
    ) {
        compositeDisposable + getRecentImagesFromStorage(
            DoubtnutApp.INSTANCE,
            limit,
            offset,
            bucketId
        ).applyIoToMainSchedulerOnSingle()
            .map { mapImageUriListToGalleryViewItems(it, addDemoQuestion) }
            .subscribeToSingle(onSuccess)
    }

    private fun mapImageUriListToGalleryViewItems(
        imageUriList: List<String>,
        addDemoQuestion: Boolean
    ): List<GalleryImageViewItem> {
        val galleryImageViewItemsList = mutableListOf<GalleryImageViewItem>()

        if (addDemoQuestion) {
            val demoShowCount =
                defaultPrefs().getInt(CameraActivity.GALLERY_DEMO_QUESTION_SHOW_COUNT, 0)
            val maxDemoShowCount = CameraActivity.MAX_GALLERY_DEMO_QUESTION_SHOW_COUNT

            // <= check as the min value of demoShowCount will always be 1 if read here as it has already been
            // read and incremented when showing horizontal gallery image list on camera
            if (demoShowCount <= maxDemoShowCount) {
                val demoQuestionItem = GalleryImageViewItem(
                    uri = defaultPrefs().getString(Constants.DEMO_QUESTION_URL, "").orEmpty(),
                    isDemoQuestion = true
                )
                galleryImageViewItemsList.add(demoQuestionItem)
            }
        }

        return imageUriList.mapTo(galleryImageViewItemsList) { GalleryImageViewItem(it) }
    }


    private fun getRecentImagesFromStorage(
        context: Context, limit: Int, offset: Int = 0,
        bucketId: String? = null
    ): Single<List<String>> = Single.fromCallable {
        val listOfAllImages = mutableListOf<String>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media.BUCKET_ID
        )
        val (selectionClause, selectionArgs) = if (bucketId != null) {
            Pair("${MediaStore.Images.Media.BUCKET_ID} = ?", arrayOf(bucketId))
        } else {
            Pair(null, null)
        }

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(
                uri, projection,
                createSelectionBundle(selectionClause, selectionArgs, limit, offset), null
            )
        } else {
            context.contentResolver.query(
                uri,
                projection,
                selectionClause,
                selectionArgs,
                "${MediaStore.Images.ImageColumns.DATE_MODIFIED} DESC LIMIT $limit OFFSET $offset"
            )
        }

        cursor?.use {
            val columnIndexData = it.getColumnIndex(MediaStore.MediaColumns.DATA)
            if (columnIndexData != -1) {
                while (cursor.moveToNext()) {
                    cursor.getStringOrNull(columnIndexData)?.let { path ->
                        listOfAllImages.add(path)
                    }
                }
            }
        }
        listOfAllImages
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSelectionBundle(
        whereCondition: String?,
        selectionArgs: Array<String>?,
        limit: Int = 20,
        offset: Int = 0
    ): Bundle = Bundle().apply {
        // Limit & Offset
        putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        // Sort function
        putStringArray(
            ContentResolver.QUERY_ARG_SORT_COLUMNS,
            arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
        )
        putInt(
            ContentResolver.QUERY_ARG_SORT_DIRECTION,
            ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
        )
        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, whereCondition)
        putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
    }

}