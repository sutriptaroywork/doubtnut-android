package com.doubtnutapp.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.NotificationClickAction
import com.doubtnutapp.data.remote.models.NotificationCenterData
import com.doubtnutapp.databinding.ItemNotificationCenterBinding
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.splash.SplashActivity
import com.google.gson.Gson
import org.json.JSONObject

class NotificationAdapter(
    val eventTracker: Tracker?, val context: Context,
    val deeplinkAction: DeeplinkAction,
    val actionPerformer: ActionPerformer
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private val notificationList = mutableListOf<NotificationCenterData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NotificationViewHolder(
            ItemNotificationCenterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
            .apply {
                actionPerformer = this@NotificationAdapter.actionPerformer
            }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        if (notificationList.isEmpty()) return

        if (notificationList[position].title.isNullOrEmpty()) {
            holder.binding.tvNotificationTitle.visibility = View.GONE
        } else {
            holder.binding.tvNotificationTitle.visibility = View.VISIBLE
            holder.binding.tvNotificationTitle.text = notificationList[position].title
        }
        if (notificationList[position].message.isNullOrEmpty()) {
            holder.binding.tvNotificationMsg.visibility = View.GONE
        } else {
            holder.binding.tvNotificationMsg.visibility = View.VISIBLE
            holder.binding.tvNotificationMsg.text = notificationList[position].message
        }

        if (notificationList[position].isClicked == 0) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.purple_eee7f3
                )
            )
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }
        if (!notificationList[position].imageUrl.isNullOrEmpty()) {
            holder.binding.notificationImage.loadImage(notificationList[position].imageUrl)
        }
        holder.binding.notificationCard.setOnClickListener {
            holder.actionPerformer.performAction(
                NotificationClickAction(
                    notificationList[position].id, notificationList[position].event,
                    position, notificationList[position].isClicked
                )
            )
            if (!notificationList[position].deeplink.isNullOrEmpty()) {
                deeplinkAction.performAction(
                    holder.itemView.context,
                    notificationList[position].deeplink
                )
            } else if (!notificationList[position].event.isNullOrEmpty()) {
                val events = listOf(
                    NotificationConstants.COURSE_NOTIFICATION,
                    NotificationConstants.GENERIC_STICKY_TIMER
                )
                if (events.contains(notificationList[position].event) && notificationList[position].data?.get(
                        "deeplink_banner"
                    ).isNotNullAndNotEmpty()
                ) {
                    deeplinkAction.performAction(
                        holder.itemView.context,
                        notificationList[position].data?.get("deeplink_banner").orEmpty()
                    )
                } else {
                    val dataMap: MutableMap<String, String> =
                        notificationList[position].data?.toMutableMap() ?: mutableMapOf()
                    val dataString =
                        if (notificationList[position].data == null) "" else Gson().toJson(dataMap)
                    dataMap["event"] = notificationList[position].event!!
                    dataMap["data"] = dataString
                    handleData(HashMap(dataMap))
                }
            }

            if (notificationList[position].isClicked == 0) {
                notificationList[position].isClicked = 1
                notifyItemChanged(position)
            }
        }
        holder.binding.tvNotificationTime.text = notificationList[position].sentAt
    }

    private fun handleData(data: HashMap<String, String>?) {
        val extraData: JSONObject?

        if (data == null || data["data"].isNullOrEmpty()) {
            context.startActivity(
                Intent(context, SplashActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            return
        }
        extraData = JSONObject(data["data"]!!)

        val appAction = AppActions.fromString(data[Constants.NOTIFICATION_EVENT])

        if (appAction != null) {
            var deeplink = AppActions.getDeeplink(appAction)
            val deeplinkUriBuilder = Uri.parse(deeplink).buildUpon()
            extraData.keys().forEach {
                deeplinkUriBuilder.appendQueryParameter(it, extraData.getString(it))
            }
            if (!extraData.has("page"))
                deeplinkUriBuilder.appendQueryParameter("page", "notification")
            deeplink = deeplinkUriBuilder.build().toString()

//            val source =
//                if (data.containsKey(Constants.SOURCE)) data[Constants.SOURCE] else "notification"
            val deeplinkHandled = deeplinkAction.performAction(context, deeplink)
            if (!deeplinkHandled) {
                context.startActivity(
                    Intent(
                        context,
                        SplashActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        } else {
            context.startActivity(
                Intent(
                    context,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    fun updateList(list: List<NotificationCenterData>?) {
        if (list == null) {
            return
        }
        val startingPosition = notificationList.size
        notificationList.addAll(list)
        notifyItemRangeInserted(startingPosition, list.size)
    }

}

class NotificationViewHolder(val binding: ItemNotificationCenterBinding) :
    RecyclerView.ViewHolder(binding.root) {
    lateinit var actionPerformer: ActionPerformer
}
