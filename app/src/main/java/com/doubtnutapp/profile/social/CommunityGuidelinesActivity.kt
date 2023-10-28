package com.doubtnutapp.profile.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_community_guidelines.*
import org.json.JSONObject
import javax.inject.Inject

class CommunityGuidelinesActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var startTime: Long = 0
    private var source: String? = null

    companion object {
        const val SOURCE = "source"
        fun getIntent(context: Context, source: String?) = Intent(context, CommunityGuidelinesActivity::class.java).putExtra(Constants.SOURCE, source)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        statusbarColor(this, R.color.colorSecondaryDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_guidelines)

        source = intent.getStringExtra(Constants.SOURCE)

        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewGuidelines1.setOnClickListener {
            tvGuidelineDetail1.show()
        }
        viewGuidelines2.setOnClickListener {
            tvGuidelineDetail2.show()
        }
        viewGuidelines3.setOnClickListener {
            tvGuidelineDetail3.show()
        }
        viewGuidelines4.setOnClickListener {
            tvGuidelineDetail4.show()
        }

        try {
            val guidelinesData = JSONObject(RemoteConfigUtils.getCommunityGuidelines())
            tvGuidelinesMain.text = Html.fromHtml(formattedString(guidelinesData.getString("main_text")))
            tvGuidelineTitle1.text = guidelinesData.getJSONObject("guideline_1").getString("title")
            tvGuidelineTitle2.text = guidelinesData.getJSONObject("guideline_2").getString("title")
            tvGuidelineTitle3.text = guidelinesData.getJSONObject("guideline_3").getString("title")
            tvGuidelineTitle4.text = guidelinesData.getJSONObject("guideline_4").getString("title")
            tvGuidelineDetail1.text = Html.fromHtml(formattedString(guidelinesData.getJSONObject("guideline_1").getString("text")))
            tvGuidelineDetail2.text = Html.fromHtml(formattedString(guidelinesData.getJSONObject("guideline_2").getString("text")))
            tvGuidelineDetail3.text = Html.fromHtml(formattedString(guidelinesData.getJSONObject("guideline_3").getString("text")))
            tvGuidelineDetail4.text = Html.fromHtml(formattedString(guidelinesData.getJSONObject("guideline_4").getString("text")))
        } catch (e: Exception) {
            Log.e(e)
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    //firebase remote config doesnt support newline character directly, so we use some special characters
    private fun formattedString(input: String): String {
        // use _br for line breaks
        var formattedString = input.replace("_br", "<br><br>")
        // use __b for closing bold tag
        formattedString = formattedString.replace("__b", "</b>")
        // use _b for opening bold tag
        formattedString = formattedString.replace("_b", "<b>")
        return formattedString
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        val screenTime = (System.currentTimeMillis() - startTime)/1000
        val event = StructuredEvent(EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_COMMUNITY_GUIDELINE,
                label = null,
                property = null,
                value = screenTime.toDouble(),
                eventParams = HashMap())
        if (!source.isNullOrEmpty()) {
            event.apply {
                eventParams[FeedViewModel.SOURCE] = source!!
            }
        }
        analyticsPublisher?.publishEvent(event)
    }
}