package com.doubtnutapp.ui.ask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.doAsyncPost
import org.json.JSONObject

class OnMatchNotificationDismissReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "NotificationDismiss"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.extras?.getSerializable("data") as HashMap<String, String>
        handleData(data)
    }

    private fun handleData(data: HashMap<String, String>?) {
        val extraData: JSONObject?

        if (data == null || data["data"] == null) return
        extraData = JSONObject(data["data"])

        when (data[Constants.NOTIFICATION_EVENT]) {

            Constants.MATCH_NOTIFICATION -> {

                doAsyncPost(handler = {
                    DataHandler.INSTANCE.matchesByFileRepository.deleteMatches(
                        extraData.getString(
                            Constants.QUESTION_ID
                        )
                    )
                }, postHandler = {

                }).execute()
            }

            Constants.MATCH_OCR_NOTIFICATION -> {
                doAsyncPost(handler = {
                    DoubtnutApp.INSTANCE.getDatabase()?.offlineOcrDao()
                        ?.deleteOcr(extraData.getLong(Constants.NOTIFICATION_ID))
                }, postHandler = {}).execute()
            }
        }
    }
}