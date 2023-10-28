package com.doubtnutapp.feed

object UserStatusTypes {

    const val RANDOM = "RANDOM"
    const val FOLLOWING = "FOLLOWING"
}

object UserStatusActionType {
    const val VIEW = "view"
    const val LIKE = "like"

    fun getActionLabel(action: String, count: Int): String {
        when (action) {
            VIEW -> if (count == 1) {
                return "1 View"
            } else {
                return "$count Views"
            }
            LIKE -> if (count == 1) {
                return "1 Like"
            } else {
                return "$count Likes"
            }
        }
        return ""
    }
}