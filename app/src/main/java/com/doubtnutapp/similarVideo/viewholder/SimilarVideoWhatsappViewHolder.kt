package com.doubtnutapp.similarVideo.viewholder

import android.graphics.Color
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenWhatsapp
import com.doubtnutapp.databinding.ItemWhatsappFeedBinding
import com.doubtnutapp.similarVideo.model.SimilarVideoWhatsappViewItem
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 2019-07-08.
 */
class SimilarVideoWhatsappViewHolder(val binding: ItemWhatsappFeedBinding) : BaseViewHolder<SimilarVideoWhatsappViewItem>(binding.root) {

    override fun bind(similarVideo: SimilarVideoWhatsappViewItem) {
        binding.executePendingBindings()
        if (similarVideo.buttonBgColor.isNullOrBlank().not()) {
            var drawable = binding.root.context.resources.getDrawable(R.drawable.bg_capsule_whatsapp)
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable, Color.parseColor(similarVideo.buttonBgColor))
            binding.buttonWhatsapp.setCompoundDrawables(null, drawable, null, null)
        }

        if (similarVideo.buttonText.isNullOrBlank().not())
            binding.buttonWhatsapp.text = similarVideo.buttonText

        if (similarVideo.description.isNullOrBlank().not()) {
            binding.textViewTitle.text = Utils.getSpannableNumberString(similarVideo.description!!)
        }

        Glide.with(binding.root.context).load(similarVideo.imageUrl).into(binding.imageView)

        binding.buttonWhatsapp.setOnClickListener {
            val context = binding.root.context
            if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, context)) {
                performAction(OpenWhatsapp(similarVideo.actionData?.externalUrl!!))
            } else {
                ToastUtils.makeText(context, R.string.string_install_whatsApp, Toast.LENGTH_SHORT).show()
            }

        }
    }

}