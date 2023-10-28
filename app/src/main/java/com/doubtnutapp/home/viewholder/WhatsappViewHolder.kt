package com.doubtnutapp.home.viewholder

import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenWhatsapp
import com.doubtnutapp.databinding.ItemWhatsappFeedBinding
import com.doubtnutapp.home.model.WhatsappFeedViewItem
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 2019-07-05.
 */
class WhatsappViewHolder(val binding: ItemWhatsappFeedBinding) : BaseViewHolder<WhatsappFeedViewItem>(binding.root), View.OnClickListener {

    private val regex = Regex("([a-z])")

    override fun bind(homeFeedModel: WhatsappFeedViewItem) {
        binding.whatsappFeed = homeFeedModel
        binding.executePendingBindings()
        binding.buttonWhatsapp.setOnClickListener(this)

        if (homeFeedModel.buttonBgColor.isNullOrBlank().not()) {
            var drawable = binding.root.context.resources.getDrawable(R.drawable.bg_capsule_whatsapp)
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable, Color.parseColor(homeFeedModel.buttonBgColor))
            binding.buttonWhatsapp.setCompoundDrawables(null, drawable, null, null)
        }

        if (homeFeedModel.buttonText.isNullOrBlank().not())
            binding.buttonWhatsapp.text = homeFeedModel.buttonText

        if (homeFeedModel.description.isNullOrBlank().not()) {
            binding.textViewTitle.text = Utils.getSpannableNumberString(homeFeedModel.description!!)
        }

        Glide.with(binding.root.context).load(homeFeedModel.imageUrl).into(binding.imageView)
    }


    override fun onClick(v: View?) {
        binding.whatsappFeed?.let {
            when (v) {
                binding.buttonWhatsapp -> {
                    val context = binding.root.context
                    if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, context)) {
                        performAction(OpenWhatsapp(it.actionData?.externalUrl!!))
                    } else {
                        ToastUtils.makeText(context, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }
}