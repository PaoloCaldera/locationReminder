package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    private val remindersList = mutableListOf(
        ReminderDataItem(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        ),
        ReminderDataItem(
            title = "Centre Court",
            description = "Remember to visit the centre court",
            location = "Esselunga",
            latitude = 51.433777128124554,
            longitude = -0.21440856010107215
        )
    )
    private val remindersListDTO = mutableListOf<ReminderDTO>()

    @Before
    fun setup() {
        for (item in remindersList) { remindersListDTO.add(fromReminderToReminderDTO(item)) }

        dataSource = FakeDataSource(remindersListDTO)
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun loadReminders_addReminder_listRefreshed() = runTest {
        // GIVEN: a new reminder is saved into the data source
        val newReminder = ReminderDataItem(
            title = "Gold Mary",
            description = "Look at the gold holy Mary",
            location = "Duomo di Milano",
            latitude = 45.46424806555846,
            longitude = 9.1919264967501
        )
        remindersList.add(newReminder)
        dataSource.saveReminder(fromReminderToReminderDTO(newReminder))

        // WHEN: retrieving all the reminders
        viewModel.loadReminders()

        // THEN: test that the LiveData value is updated
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(remindersList))
    }

    @Test
    fun clearReminderList_remindersList_toEmptyList() = runTest {
        // GIVEN: from a populated list of reminders

        // WHEN: reminders list is cleared
        viewModel.clearRemindersList()

        // THEN: the data source is an empty list
        assertThat(dataSource.getReminders(), `is`(Result.Success(mutableListOf())))

        // WHEN: LiveData is refreshed
        viewModel.loadReminders()

        // THEN: the LiveData value is an empty list
        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(listOf()))
    }

    private fun fromReminderToReminderDTO(r: ReminderDataItem) : ReminderDTO {
        return ReminderDTO(
            title = r.title,
            description = r.description,
            location = r.location,
            latitude = r.latitude,
            longitude = r.longitude,
            id = r.id
        )
    }

    private fun fromReminderDTOToReminder(rdto: ReminderDTO): ReminderDataItem {
        return ReminderDataItem(
            title = rdto.title,
            description = rdto.description,
            location = rdto.location,
            latitude = rdto.latitude,
            longitude = rdto.longitude,
            id = rdto.id
        )
    }

}