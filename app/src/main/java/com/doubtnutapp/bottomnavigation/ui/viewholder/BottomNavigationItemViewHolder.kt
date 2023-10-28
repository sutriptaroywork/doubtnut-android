package com.doubtnutapp.bottomnavigation.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.BottomNavigationItemClicked
import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.databinding.ItemBottomNavigationBinding
import com.doubtnutapp.loadImageEtx

/**
 * Created by devansh on 21/1/21.
 */

class BottomNavigationItemViewHolder(containerView: View) :
    BaseViewHolder<BottomNavigationItemData>(containerView) {

    val binding = ItemBottomNavigationBinding.bind(containerView)

    override fun bind(data: BottomNavigationItemData) {
        binding.apply {
            tvNavTitle.text = data.title.orEmpty()
            ivNavIcon.loadImageEtx(data.icon.orEmpty())

            root.isSelected = data.isSelected

            root.setOnClickListener {
                actionPerformer?.performAction(BottomNavigationItemClicked(data, layoutPosition))
            }
        }
    }
}
