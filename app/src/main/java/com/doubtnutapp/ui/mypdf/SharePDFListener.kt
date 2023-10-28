package com.doubtnutapp.ui.mypdf

interface SharePDFListener {
    fun sharePDF(filePath: String) {}
    fun downloadAndShare(url: String) {}

    fun savePdf(file : Any?) {}
}