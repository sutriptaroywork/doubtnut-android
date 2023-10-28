package com.doubtnutapp.ui.main.demoanimation.viewholder

import android.animation.Animator
import android.content.res.AssetManager
import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SendEventOfDemoAnimation
import com.doubtnutapp.databinding.ItemDemoAnimationV2Binding
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity


/**
 * Created by Sachin Saxena on 2020-04-10.
 */
class DemoAnimationViewHolderV2(
        private val binding: ItemDemoAnimationV2Binding,
        private val positionToPlay: Int
) : BaseViewHolder<DemoAnimationEntity?>(binding.root) {

    override fun bind(data: DemoAnimationEntity?) {

        binding.title.text = data?.title

        binding.footer.text = data?.footer

        val am: AssetManager = binding.root.context.getAssets()
        binding.demoVideo.setAnimation(data?.zipFileName)
        binding.demoVideo.imageAssetsFolder = data?.imageFolderName

        if (adapterPosition == positionToPlay) {
            binding.playButton.visibility = View.GONE
            binding.demoVideo.playAnimation()
        }

        binding.demoVideo.addAnimatorListener(object : Animator.AnimatorListener{

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                binding.playButton.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {
                binding.playButton.visibility = View.VISIBLE
            }

            override fun onAnimationStart(animation: Animator?) {
                binding.playButton.visibility = View.GONE
            }
        })

        binding.demoVideo.setOnClickListener {
            if (binding.demoVideo.isAnimating) {
                binding.playButton.visibility = View.VISIBLE
                binding.demoVideo.pauseAnimation()
            } else {
                binding.playButton.visibility = View.GONE
                binding.demoVideo.clearAnimation()
                binding.demoVideo.playAnimation()
            }
            actionPerformer?.performAction(SendEventOfDemoAnimation(adapterPosition, data?.isPlaying == true))
        }
        binding.executePendingBindings()
    }
}