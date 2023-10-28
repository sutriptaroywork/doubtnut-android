package com.doubtnutapp.gamification.settings.profilesetting.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OnSettingOptionClicked
import com.doubtnutapp.databinding.ItemProfileSettingBinding
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingsItems

/**
 * Created by shrreya on 29/6/19.
 */
class ProfileSettingAdapter(private val actionsPerformer: ActionPerformer) :
    RecyclerView.Adapter<ProfileSettingAdapter.ViewHolder>() {

    private var items: List<ProfileSettingsItems>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_profile_setting, parent, false
            ), actionsPerformer
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class ViewHolder constructor(
        var binding: ItemProfileSettingBinding,
        var actionsPerformer: ActionPerformer
    ) : BaseViewHolder<ProfileSettingsItems>(binding.root) {

        override fun bind(data: ProfileSettingsItems) {
            binding.settingData = data
            binding.executePendingBindings()
            binding.itemImageView.setImageResource(data.image)
            binding.profileSettingsList.setOnClickListener {
                actionsPerformer.performAction(OnSettingOptionClicked(data.settingOptionType))
            }
        }

    }

    fun updateData(items: List<ProfileSettingsItems>) {
        this.items = items
        notifyDataSetChanged()
    }
}