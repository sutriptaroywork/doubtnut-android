package com.doubtnut.core.utils

import android.database.Cursor
import android.provider.ContactsContract
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.doubtnut.core.CoreApplication
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

object ContactUtils {

    const val LAST_UPDATED_CONTACTS_TIMESTAMP = "last_updated_contacts_timestamp"

    fun getMobileNumbers(): HashMap<Int, ContactData> {
        var cursor: Cursor? = null
        val contactDataHashMap = HashMap<Int, ContactData>()
        val lastUpdatedTimeStamp = defaultPreferences().getLong(LAST_UPDATED_CONTACTS_TIMESTAMP, 0L)

        try {
            cursor = CoreApplication.INSTANCE.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " ASC"
            )
            cursor?.let { _cursor ->
                if (_cursor.moveToFirst()) {
                    do {
                        val id = _cursor.getInt(0)
                        val displayName = _cursor.getString(1)
                        val number = _cursor.getString(2).replace(" ", "")
                        val updatedTimeStamp = _cursor.getString(3).toLong()

                        if (updatedTimeStamp <= lastUpdatedTimeStamp)
                            continue

                        if (contactDataHashMap[id] == null) {
                            val contactData = ContactData(
                                id = id,
                                name = displayName,
                                mobileNumber = null,
                                mobileNumbers = ArrayList<String?>().apply {
                                    add(number)
                                },
                                customer = null,
                                type = null,
                                title = null
                            )
                            contactDataHashMap[id] = contactData
                        } else {
                            if (contactDataHashMap[id]?.mobileNumbers?.contains(number) == false) {
                                contactDataHashMap[id]?.mobileNumbers?.add(number)
                            }
                        }
                    } while (_cursor.moveToNext())
                }
            }
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            cursor?.close()
        } finally {
            cursor?.close()
        }
        return contactDataHashMap
    }
}

@Keep
data class ContactDataRequest(
    @SerializedName("contacts")
    val contacts: List<ContactDataManipulated>
)

@Keep
data class ContactDataManipulated(
    @SerializedName("id")
    @Expose
    val id: Int?,
    @SerializedName("contact")
    @Expose
    val mobileNumbers: String?,
    @SerializedName("customer")
    val customer: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("title")
    val title: String?
)

@Keep
data class ContactData(
    @SerializedName("id")
    @Expose
    val id: Int?,
    @SerializedName("contact")
    @Expose
    val mobileNumber: String?,
    @SerializedName("contacts")
    @Expose
    val mobileNumbers: ArrayList<String?>?,
    @SerializedName("customer")
    val customer: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("title")
    val title: String?
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContactData>() {

            override fun areItemsTheSame(
                oldItem: ContactData,
                newItem: ContactData
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ContactData,
                newItem: ContactData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
