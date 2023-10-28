package com.doubtnutapp.base.ui

import android.view.ViewGroup

interface ViewHolderFactory<T> {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): T
}
