package com.doubtnutapp.gallery.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.gallery.viewmodel.GalleryFragmentViewModel
import com.doubtnutapp.ui.main.di.CameraActivityBindModule
import com.doubtnutapp.ui.main.di.CameraActivityProvideModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 31/10/20.
 */

@Module(includes = [CameraActivityBindModule::class, CameraActivityProvideModule::class])
abstract class GalleryFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(GalleryFragmentViewModel::class)
    abstract fun bindGalleryFragmentViewModel(galleryFragmentViewModel: GalleryFragmentViewModel): ViewModel

}