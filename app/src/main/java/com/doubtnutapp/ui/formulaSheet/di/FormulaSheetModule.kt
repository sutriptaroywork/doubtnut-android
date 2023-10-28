package com.doubtnutapp.ui.formulaSheet.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.formulaSheet.FormulaSheetViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-19.
 */
@Module
abstract class FormulaSheetModule {

    @Binds
    @IntoMap
    @ViewModelKey(FormulaSheetViewModel::class)
    abstract fun bindFormulaSheetViewModel(formulaSheetViewModel: FormulaSheetViewModel): ViewModel
}