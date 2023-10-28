package com.doubtnutapp.ui.forum.comments

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel

class CommentImageViewModel(app: Application, imageUrl: String) : AndroidViewModel(app) {
    val imageUrl = ObservableField(imageUrl)

}