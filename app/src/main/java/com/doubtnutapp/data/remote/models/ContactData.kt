package com.doubtnutapp.data.remote.models

import android.content.Context

data class ContactData(
    var name: String = "",
    var mobileNumbers: List<String>? = null,
    var emails: List<String>? = null,
    var birthday: String = ""

) {

    class Builder(context: Context) {
        private var context: Context = context
        private var name: String = ""
        var mobileNumbers: List<String>? = null
        var emails: List<String>? = null
        private var birthday: String = ""

        fun name(name: String) = apply { this.name = name }
        fun mobileNumbers(mobileNumbers: List<String>??) =
            apply { this.mobileNumbers = mobileNumbers }

        fun emails(emails: List<String>?) = apply { this.emails = emails }
        fun birthday(birthday: String) = apply { this.birthday = birthday }

        fun build() = ContactData(
            name,
            mobileNumbers,
            emails,
            birthday
        )
    }
}
