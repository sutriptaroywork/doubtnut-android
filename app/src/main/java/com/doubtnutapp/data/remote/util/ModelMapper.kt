package com.doubtnutapp.data.remote.util

interface ModelMapper<in T, out R> {
    fun map(to: T): R
}
