package com.doubtnutapp.similarVideo.ui

interface OnItemClickListener {
    fun onItemClicked(qid: String, button: String)

    fun onItemAddToBookmarkClicked(qid: String)
    fun onShareItemClicked(_qid: String, question_thumbnail: String) {

    }

}