package com.doubtnutapp.resourcelisting.ui.viewholder

import android.graphics.Color
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenWhatsapp
import com.doubtnutapp.databinding.ItemWhatsappResourceBinding
import com.doubtnutapp.resourcelisting.model.WhatsappModel
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ResourceWhatsappViewHolder(val binding: ItemWhatsappResourceBinding) :
    BaseViewHolder<WhatsappModel>(binding.root) {

    override fun bind(homeFeedModel: WhatsappModel) {
        binding.executePendingBindings()
        if (homeFeedModel.buttonBgColor.isNullOrBlank().not()) {
            var drawable =
                binding.root.context.resources.getDrawable(R.drawable.bg_capsule_whatsapp)
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

        binding.buttonWhatsapp.setOnClickListener {
            val context = binding.root.context
            if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, context)) {
                performAction(OpenWhatsapp(homeFeedModel.actionData?.externalUrl!!))
            } else {
                ToastUtils.makeText(context, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}