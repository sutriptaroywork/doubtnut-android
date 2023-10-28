package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.similarVideo.entities.WhatsappActionData
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/**
 * Created by Anand Gaurav on 2019-07-09.
 */
@Keep
@Parcelize
data class SimilarVideoWhatsappViewItem(val id: String?,
                                        val keyName: String?,
                                        val imageUrl: String?,
                                        val description: String?,
                                        val buttonText: String?,
                                        val buttonBgColor: String?,
                                        val actionActivity: String?,
                                        val actionData: @RawValue WhatsappActionData?,
                                        val resourceType: String,
                                        override val viewType: Int) : Parcelable, RecyclerViewItem