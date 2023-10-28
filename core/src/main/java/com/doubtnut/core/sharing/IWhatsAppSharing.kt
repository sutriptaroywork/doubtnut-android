package com.doubtnut.core.sharing

import android.content.Context
import com.doubtnut.core.sharing.entities.ShareOnWhatApp

interface IWhatsAppSharing {

    fun shareOnWhatsApp(action: ShareOnWhatApp)

    fun startShare(context: Context)

    fun dispose()
}