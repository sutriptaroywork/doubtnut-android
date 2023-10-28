package com.doubtnutapp.gallery.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.doubtnutapp.R

/**
 * Created by devansh on 08/10/20.
 */

@Keep
data class GalleryShowMoreViewItem(
        @StringRes val showMoreTextRes: Int,
        override val viewType: Int = R.layout.item_gallery_show_more
) : CameraScreenGalleryViewItem