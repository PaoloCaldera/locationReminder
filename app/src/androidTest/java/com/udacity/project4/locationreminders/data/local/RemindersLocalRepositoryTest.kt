package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before         // Initialize database and repository
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After          // Clean database resources
    fun reset() = database.close()

    @Test
    fun saveReminder_retrieveReminder_machingProperties() = runBlocking {
        // GIVEN: save a reminder into the database using the repository function
        val saved = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        repository.saveReminder(saved)

        // WHEN: retrieve the same reminder from the db using the repo function
        val loaded = repository.getReminder(saved.id)

        // THEN: check that the saved and loaded reminders are the same
        assertThat(loaded, notNullValue())
        loaded as Result.Success
        assertThat(loaded.data.title, `is`(saved.title))
        assertThat(loaded.data.description, `is`(saved.description))
        assertThat(loaded.data.location, `is`(saved.location))
        assertThat(loaded.data.latitude, `is`(saved.latitude))
        assertThat(loaded.data.longitude, `is`(saved.longitude))
    }

    @Test
    fun saveReminder_clearAll_nullList() = runBlocking {
        // GIVEN: save a reminder into the database using the repository function
        val saved = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        repository.saveReminder(saved)

        // WHEN: cancel all the reminders from the db using the repo function and retrieve the result
        repository.deleteAllReminders()
        val remindersList = repository.getReminders()


        // THEN: check that the retrieved list is an empty list
        assertThat(remindersList, notNullValue())
        remindersList as Result.Success
        assertThat(remindersList.data, `is`(listOf()))
    }
}