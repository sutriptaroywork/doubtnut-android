package com.doubtnutapp.ui.pdfviewer.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.pdfviewer.PdfViewerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
@Module
abstract class PdfViewerModule {

    @Binds
    @IntoMap
    @ViewModelKey(PdfViewerViewModel::class)
    abstract fun bindPdfViewerViewModel(pdfViewerViewModel: PdfViewerViewModel): ViewModel
}