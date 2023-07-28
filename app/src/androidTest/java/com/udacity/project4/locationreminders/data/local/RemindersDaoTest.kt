package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before         // Initialize the database
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After          // Clean up the database
    fun resetDatabase() = database.close()


    @Test
    fun saveTask_retrieveTask_matchingProperties() = runTest {
        // GIVEN: save a reminder into the database
        val saved = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        database.reminderDao().saveReminder(saved)

        // WHEN: retrieve the reminder having the same id of the one saved
        val loaded = database.reminderDao().getReminderById(saved.id)

        // THEN: the loaded reminder is equal to the saved one
        assertThat(loaded, notNullValue())
        assertThat(loaded, `is`(saved))
    }

    @Test
    fun saveTask_updateTask_matchingProperties() = runTest {
        // GIVEN: save a reminder into the database
        val saved = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        database.reminderDao().saveReminder(saved)

        // WHEN: cancel all the reminders in the database and then retrieve all
        database.reminderDao().deleteAllReminders()
        val remindersList = database.reminderDao().getReminders()

        // THEN: the loaded list of reminders is an empty list
        assertThat(remindersList, `is`(listOf()))
    }

}