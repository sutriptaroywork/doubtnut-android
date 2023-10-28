package com.doubtnutapp.ui.forum.comments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityCommentImageViewBinding

class CommentImageFullView : AppCompatActivity() {

    private lateinit var viewmodel: CommentImageViewModel

    companion object {
        const val INTENT_EXTRA_COMMENT_IMAGE_URL = "comment_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding = DataBindingUtil.setContentView<ActivityCommentImageViewBinding>(this, R.layout.activity_comment_image_view)

        val imageUrl = intent.getStringExtra(INTENT_EXTRA_COMMENT_IMAGE_URL) ?: ""

        viewmodel = ViewModelProviders.of(this, CommentImageViewModelFactory(application, imageUrl)).get(CommentImageViewModel::class.java)
        dataBinding.viewmodel = viewmodel
    }

}
