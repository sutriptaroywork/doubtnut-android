package com.doubtnutapp.widgets.countrycodepicker.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.widgets.countrycodepicker.CountryCodePickerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 21-09-2021
 */

@Module
abstract class CountryCodePickerModule {

    @Binds
    @IntoMap
    @ViewModelKey(CountryCodePickerViewModel::class)
    abstract fun bindCountryCodePicker(countryCodePickerViewModel: CountryCodePickerViewModel): ViewModel
}