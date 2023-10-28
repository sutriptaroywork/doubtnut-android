package com.doubtnutapp.data.base

interface Mapper<in Src, out Des> {
    fun map(srcObject: Src): Des
}
