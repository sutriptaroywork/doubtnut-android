package com.doubtnutapp.data.remote.models

data class Library(
    val title: String,
    val playlist_id: String,
    val list: ArrayList<LibraryQuestion>,
    val image_url: String
)
