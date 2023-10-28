package com.doubtnutapp.data.remote.models

interface LiveChatModel {
    @LiveChatViewTypes.ViewType
    var viewType: Int
}
