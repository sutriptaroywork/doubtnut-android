package com.doubtnutapp.feed.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.adapter.ViewPagerAdapter2
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.databinding.ActivityImagesPagerBinding
import com.doubtnutapp.ui.base.BaseActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class ImagesPagerActivity : BaseActivity() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val images: List<String>
        get() = intent.getStringArrayListExtra("images").orEmpty()

    private val isFromFeed
        get() = intent.getBooleanExtra("isFromFeed", false)

    private val source: String?
        get() = intent.getStringExtra(EventConstants.SOURCE)

    private var startTime: Long = 0

    private lateinit var binding: ActivityImagesPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityImagesPagerBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupViewPager()
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        if (isFromFeed) {
            val screenTime = (System.currentTimeMillis() - startTime) / 1000
            val event = StructuredEvent(
                EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_VIEW_IMAGE,
                label = null,
                property = null,
                value = screenTime.toDouble(),
                eventParams = HashMap()
            )
            if (!source.isNullOrEmpty()) {
                event.apply {
                    eventParams[Constants.SOURCE] = source!!
                }
            }
            analyticsPublisher.publishEvent(event)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter2(this, images.map { ImagePagerFragment.newInstance(it) })
        binding.pager.adapter = adapter
        binding.pager.currentItem = intent.getIntExtra("position", 0)
        binding.tvImageCount.text = "${binding.pager.currentItem + 1}/${images.size}"
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvImageCount.text = "${position + 1}/${images.size}"
            }
        })
    }

    companion object {
        fun getIntent(
            context: Context,
            images: List<String>,
            currentIndex: Int = 0,
            isFromFeed: Boolean = false,
            source: String? = null
        ) = Intent(context, ImagesPagerActivity::class.java).apply {
            putStringArrayListExtra("images", ArrayList(images))
            putExtra("position", currentIndex)
            putExtra("isFromFeed", isFromFeed)
            putExtra(EventConstants.SOURCE, source)
        }
    }

}