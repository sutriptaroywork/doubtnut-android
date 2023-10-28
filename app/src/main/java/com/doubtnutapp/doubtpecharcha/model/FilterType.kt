package com.doubtnutapp.doubtpecharcha.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class FilterType : Parcelable

@Parcelize
class SubjectFilter() : FilterType()

@Parcelize
class ClassesFilter() : FilterType()

@Parcelize
class LanguageFilter() : FilterType()