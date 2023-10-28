package com.doubtnutapp.downloadedVideos

import androidx.lifecycle.ViewModel
import com.doubtnutapp.CategorySearchActivity
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.liveclass.viewmodel.CategorySearchViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CategorySearchModule : BaseActivityModule<CategorySearchActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(CategorySearchViewModel::class)
    internal abstract fun bindCategorySearchViewModel(categorySearchViewModel: CategorySearchViewModel): ViewModel
}
