package com.doubtnut.scholarship.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import com.doubtnut.scholarship.R
import org.junit.Rule
import org.junit.Test

/**
 * Test Class for ScholarshipActivity
 * make sure to change the settings of your physical device/emulator as per the 'Set up your test environment' in
 * https://developer.android.com/training/testing/espresso/setup#set-up-environment
 * before running espresso tests.
 * */

class ScholarshipActivityTest {

    /**
     * to automatically call launch the activity before each test, and close at test teardown.
     * */
    @get:Rule
    var activityScenarioRule = activityScenarioRule<ScholarshipActivity>()

    @Test
    fun test_toolbarDisplayed() {

        // get the instance of the launched activity
        val scenario = activityScenarioRule.scenario

        // checking that toolbar in the activity is displayed when the activity is launched
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun test_stickyWidgetsRecyclerViewDisplayed() {
        onView(withId(R.id.rvStickyWidgets)).check(matches(isDisplayed()))
    }

    @Test
    fun test_widgetsRecyclerViewDisplayed() {
        onView(withId(R.id.rvWidgets)).check(matches(isDisplayed()))
    }

    @Test
    fun test_footerWidgetsRecyclerViewDisplayed() {
        onView(withId(R.id.rv_footer_widgets)).check(matches(isDisplayed()))
    }

    @Test
    fun test_bottomWidgetsRecyclerViewDisplayed() {
        onView(withId(R.id.rvWidgetsBottom)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}
