package com.doubtnutapp.ui.downloadPdf.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.downloadPdf.DownloadNShareViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
@Module
abstract class DownloadPdfModule {

    @Binds
    @IntoMap
    @ViewModelKey(DownloadNShareViewModel::class)
    abstract fun bindDownloadNShareViewModel(downloadNShareViewModel: DownloadNShareViewModel): ViewModel
}