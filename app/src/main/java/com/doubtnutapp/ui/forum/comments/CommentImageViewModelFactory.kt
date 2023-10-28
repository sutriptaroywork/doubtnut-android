package com.doubtnutapp.ui.forum.comments

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommentImageViewModelFactory(
    private val application: Application,
    private val imageUrl: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CommentImageViewModel(application, imageUrl) as T
}