package com.doubtnutapp.db.dao

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
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Class for testing BaseDao
 * Note - Data Class NoticeBoard has been provided to BaseDao as the type parameter
 */

/**
 *  Note: Test functions follow the naming convention subjectUnderTest_actionOrInput_resultState
 *  for eg. getCount_insertItem_ItemInserted
 */

/**
 * The body of a test case should be structured like
 * Given - What is given Object (an Item, a List etc.) or create the given object
 * When - What is the action/operation we are performing with the given object
 * Then - What is the result we are expecting after the action is performed
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BaseDaoTest {

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

    @Test
    fun insertItem_item_itemInserted() = runTest(UnconfinedTestDispatcher()) {
        val nItem = NoticeBoard("010")
        dao.insert(nItem)

        val count = dao.getCount("010")
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun insertWithReplace_item_replaceItemIfAlreadyPresent() =
        runTest(UnconfinedTestDispatcher()) {
            val item = NoticeBoard("010")
            dao.insert(item)
            dao.insertWithReplace(item)
            dao.insertWithReplace(item)

            assertThat(dao.getCount("010")).isEqualTo(1)
        }

    @Test
    fun insertAllWithReplace_listOfItems_duplicateItemsAddedOnce() =
        runTest(UnconfinedTestDispatcher()) {
            val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("011"))
            dao.insertAllWithReplace(items)

            assertThat(dao.getCount("011")).isEqualTo(1)
            assertThat(dao.getCount()).isEqualTo(2)
        }

    @Test
    fun insertWithIgnore_item_itemNotInsertedIfAlreadyPresent() =
        runTest(UnconfinedTestDispatcher()) {
            val nItem = NoticeBoard("010")
            dao.insert(nItem)
            dao.insertWithIgnore(nItem)

            val count = dao.getCount("010")
            assertThat(count).isEqualTo(1)
        }

    @Test
    fun insertAllWithIgnore_listOfItems_itemNotInsertedIfAlreadyPresent() =
        runTest(UnconfinedTestDispatcher()) {
            val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("011"))
            dao.insertAllWithIgnore(items)

            assertThat(dao.getCount("011")).isEqualTo(1)
            assertThat(dao.getCount("012")).isEqualTo(1)
            assertThat(dao.getCount("013")).isEqualTo(0)
            assertThat(dao.getCount()).isEqualTo(2)
        }

    @Test
    fun updateWithIgnore_listOfItems_UpdatesItemIfAlreadyPresent() =
        runTest(UnconfinedTestDispatcher()) {
            val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("011"))
            items.forEach {
                dao.updateWithIgnore(it)
            }
            assertThat(dao.getCount("011")).isEqualTo(0)
            assertThat(dao.getCount("012")).isEqualTo(0)
            assertThat(dao.getCount("013")).isEqualTo(0)
            assertThat(dao.getCount()).isEqualTo(0)
        }

    @Test
    fun updateAllWithIgnore_listOfItems_UpdatesItemIfAlreadyPresent() =
        runTest(UnconfinedTestDispatcher()) {
            val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("011"))
            dao.updateAllWithIgnore(items)

            assertThat(dao.getCount("011")).isEqualTo(0)
            assertThat(dao.getCount("012")).isEqualTo(0)
            assertThat(dao.getCount("013")).isEqualTo(0)
            assertThat(dao.getCount()).isEqualTo(0)
        }

    @Test
    fun delete_item_deleteSelectedItem() = runTest(UnconfinedTestDispatcher()) {
        val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("013"))
        dao.insertAllWithIgnore(items)

        assertThat(dao.getCount("011")).isEqualTo(1)
        assertThat(dao.getCount("012")).isEqualTo(1)
        assertThat(dao.getCount("013")).isEqualTo(1)
        assertThat(dao.getCount()).isEqualTo(3)

        dao.delete(NoticeBoard("012"))

        assertThat(dao.getCount("011")).isEqualTo(1)
        assertThat(dao.getCount("012")).isEqualTo(0)
        assertThat(dao.getCount("013")).isEqualTo(1)
        assertThat(dao.getCount()).isEqualTo(2)
    }

    @Test
    fun deleteAll_listOfItems_deletesTheListOfItems() = runTest(UnconfinedTestDispatcher()) {
        val items = listOf(NoticeBoard("011"), NoticeBoard("012"), NoticeBoard("013"))
        dao.insertAllWithIgnore(items)

        assertThat(dao.getCount("011")).isEqualTo(1)
        assertThat(dao.getCount("012")).isEqualTo(1)
        assertThat(dao.getCount("013")).isEqualTo(1)
        assertThat(dao.getCount()).isEqualTo(3)

        dao.deleteAll(items)

        assertThat(dao.getCount("011")).isEqualTo(0)
        assertThat(dao.getCount("012")).isEqualTo(0)
        assertThat(dao.getCount("013")).isEqualTo(0)
        assertThat(dao.getCount()).isEqualTo(0)
    }
}
