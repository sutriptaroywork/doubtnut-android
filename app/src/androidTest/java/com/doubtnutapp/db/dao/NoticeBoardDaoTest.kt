package com.doubtnutapp.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.doubtnut.database.dao.NoticeBoardDao
import com.doubtnut.database.entity.NoticeBoard
import com.doubtnutapp.db.DoubtnutDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testing Class for NoticeBoardDao
 */

/**
 *  Note: Test functions follow the naming convention subjectUnderTest_actionOrInput_resultState
 *  for eg. getCount_insertItem_ItemInserted
 */

/**
 * The test case should be structured like
 * Given - What is given Object (an Item, a List etc.) or create the given object
 * When - What is the action/operation we are performing with the given object
 * Then - What is the result we are expecting after the action is performed
 */

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class NoticeBoardDaoTest {

    /** makes sure that all the background tasks related to
    architectural components happen in the same thread important if observing live data,
    can be skipped here because the Daos under testing don't have any functions which are returning
    live data but in general this will not be the case*/
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: DoubtnutDatabase
    private lateinit var dao: NoticeBoardDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DoubtnutDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.noticeBoardDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // method under test is getCount
    // action or input is itemId
    // result expected is that the function returns Count of rows with that Id
    @Test
    fun getCount_itemId_returnsCountOfRowsWithThatId() = runTest(UnconfinedTestDispatcher()) {
        dao.insert(NoticeBoard("010"))

        assertThat(dao.getCount("010")).isEqualTo(1)
        assertThat(dao.getCount("011")).isEqualTo(0)
    }

    @Test
    fun getCount_validInput_returnSuccess() = runTest(UnconfinedTestDispatcher()) {
        assertThat(dao.getCount()).isEqualTo(0)

        dao.insert(NoticeBoard("010"))
        dao.insert(NoticeBoard("abc"))
        dao.insert(NoticeBoard("0z2"))

        assertThat(dao.getCount("010")).isEqualTo(1)
        assertThat(dao.getCount("abc")).isEqualTo(1)
        assertThat(dao.getCount("0z2")).isEqualTo(1)

        assertThat(dao.getCount()).isEqualTo(3)
    }

    @Test
    fun getCount_inValidInput_returnFailure() = runTest(UnconfinedTestDispatcher()) {
        dao.insert(NoticeBoard("010"))

        assertThat(dao.getCount("020")).isEqualTo(0)
    }
}
