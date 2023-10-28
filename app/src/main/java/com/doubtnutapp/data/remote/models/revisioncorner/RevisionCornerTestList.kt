package com.doubtnutapp.data.remote.models.revisioncorner

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class TestList(
    @SerializedName("test_meta_data") val testMetaData: TestMetaData?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("page") val nextPage: Int?,
)

@Keep
data class TestMetaData(
    @SerializedName("title") val title: String,
)

@Keep
data class Test(
    @SerializedName("heading") val heading: String,
    @SerializedName("subheading") val subheading: String,
    @SerializedName("subheading_color") val subheadingColor: String?,
    @SerializedName("deeplink") val deeplink: String,
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Test>() {
            override fun areItemsTheSame(oldItem: Test, newItem: Test): Boolean {
                return oldItem.heading == newItem.heading
            }

            override fun areContentsTheSame(oldItem: Test, newItem: Test): Boolean {
                return oldItem == newItem
            }
        }
    }
}
