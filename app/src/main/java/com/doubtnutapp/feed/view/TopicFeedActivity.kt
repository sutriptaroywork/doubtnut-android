package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.doubtnutapp.R
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.windowBackground
import com.google.android.material.tabs.TabLayout
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_topic_feed.*
import javax.inject.Inject

class TopicFeedActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {
        const val TOPIC = "topic"

        fun getStartIntent(context: Context, topic: String, start: Boolean = false): Intent {
            val intent = Intent(context, TopicFeedActivity::class.java)
            intent.putExtra(TOPIC, topic)
            return intent.also { if (start) context.startActivity(it) }
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private lateinit var topic: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_feed)
        windowBackground(this, R.drawable.post_topic_background_no_rounded)

        topic = intent.getStringExtra(TOPIC).orEmpty()

        toolBar.title = topic
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupTabs()
    }

    private fun setupTabs() {
        replaceFragment(FeedFragment.newTopicFeedInstance(FeedViewModel.TOPIC_POPULAR, topic))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    replaceFragment(FeedFragment.newTopicFeedInstance(FeedViewModel.TOPIC_POPULAR, topic))
                } else if (tab.position == 1) {
                    replaceFragment(FeedFragment.newTopicFeedInstance(FeedViewModel.TOPIC_RECENT, topic))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun replaceFragment(fragment: FeedFragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}