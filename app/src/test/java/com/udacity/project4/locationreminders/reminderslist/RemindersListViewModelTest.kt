package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel


    @Before
    fun setup() {
        dataSource = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun reset() {
        stopKoin()
    }

    @Test
    fun loadReminders_initialEntries_listRefreshed() = mainCoroutineRule.runBlockingTest {
        // GIVEN: the data source has already two reminder entries
        val reminder1 = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        dataSource.saveReminder(reminder1)
        val reminder2 = ReminderDTO(
            title = "Centre Court",
            description = "Remember to visit the centre court",
            location = "Wimbledon Centre Court",
            latitude = 51.433777128124554,
            longitude = -0.21440856010107215
        )
        dataSource.saveReminder(reminder2)

        // WHEN: all the reminders are retrieved with the viewModel function
        viewModel.loadReminders()
        val list = viewModel.remindersList.getOrAwaitValue()

        // THEN: test that the LiveData value contains that exists in the data source
        assertThat(list, notNullValue())
        // First list item
        val listItem1 = list.find { it.id == reminder1.id }!!
        assertThat(listItem1.title, `is`(reminder1.title))
        assertThat(listItem1.description, `is`(reminder1.description))
        assertThat(listItem1.location, `is`(reminder1.location))
        assertThat(listItem1.latitude, `is`(reminder1.latitude))
        assertThat(listItem1.longitude, `is`(reminder1.longitude))
        // Second list item
        val listItem2 = list.find { it.id == reminder2.id }!!
        assertThat(listItem2.title, `is`(reminder2.title))
        assertThat(listItem2.description, `is`(reminder2.description))
        assertThat(listItem2.location, `is`(reminder2.location))
        assertThat(listItem2.latitude, `is`(reminder2.latitude))
        assertThat(listItem2.longitude, `is`(reminder2.longitude))
        // ShowNoData LiveData variable must be false
        assertThat(viewModel.showNoData.value, `is`(false))
    }

    @Test
    fun clearReminderList_remindersList_toEmptyList() = mainCoroutineRule.runBlockingTest {
        // GIVEN: the data source has already two reminder entries
        val reminder1 = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        dataSource.saveReminder(reminder1)
        val reminder2 = ReminderDTO(
            title = "Centre Court",
            description = "Remember to visit the centre court",
            location = "Wimbledon Centre Court",
            latitude = 51.433777128124554,
            longitude = -0.21440856010107215
        )
        dataSource.saveReminder(reminder2)
        viewModel.loadReminders()

        // WHEN: the not empty reminders list is cleared
        viewModel.clearRemindersList()

        // THEN: the data source is an empty list
        val clearedList = dataSource.getReminders()
        assertThat(clearedList, notNullValue())
        clearedList as Result.Success
        assertThat(clearedList.data, `is`(listOf()))

        // WHEN: refresh again the LiveData after clearing the list
        viewModel.loadReminders()

        // THEN: list in the viewModel is an empty list and the showNoData LiveData variable is true
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(listOf()))
        assertThat(viewModel.showNoData.value, `is`(true))
    }
}