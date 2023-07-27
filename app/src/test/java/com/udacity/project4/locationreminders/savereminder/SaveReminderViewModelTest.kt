package com.udacity.project4.locationreminders.savereminder

import com.udacity.project4.R
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

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
        for (item in remindersList) {
            remindersListDTO.add(fromReminderToReminderDTO(item))
        }

        dataSource = FakeDataSource(remindersListDTO)
        viewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }


    @Test
    fun onClear_clearLiveData_nullLiveData() {
        // GIVEN: LiveData variables having a value
        viewModel.reminderTitle.value = "Gold Mary"
        viewModel.reminderDescription.value = "Look at the gold holy Mary"
        viewModel.reminderSelectedLocationStr.value = "Duomo di Milano"
        //viewModel.selectedPOI.value = null
        viewModel.selectedPOI.value = PointOfInterest(
            LatLng(45.46424806555846, 9.1919264967501),
            "ID",
            "Duomo di Milano"
        )
        viewModel.latitude.value = 45.46424806555846
        viewModel.longitude.value = 9.1919264967501

        // WHEN: calling the function to make all the entries empty
        viewModel.onClear()

        // THEN: check that LiveData variables are actually null
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(viewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValue(), nullValue())
    }


    @Test
    fun fillReminderLocationParameters_locationEntries_populatedLiveData() {
        // GIVEN: selected location entries
        val location = "Duomo di Milano"
        val latitude = 45.46424806555846
        val longitude = 9.1919264967501
        val pointOfInterest = PointOfInterest(
            LatLng(latitude, longitude), "ID", "Duomo di Milano"
        )

        // WHEN: filling the location LiveData variables
        viewModel.fillReminderLocationParameters(location, pointOfInterest, latitude, longitude)

        // THEN: check that the LiveData values are exactly the one provided in the entries
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(location))
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), `is`(pointOfInterest))
        assertThat(viewModel.latitude.getOrAwaitValue(), `is`(latitude))
        assertThat(viewModel.longitude.getOrAwaitValue(), `is`(longitude))
    }

    @Test
    fun validateAndSaveReminder_reminderEntry_savedOnLocalDataSource() = runTest {
        // GIVEN: reminder entry with all the LiveData initialized
        val newReminder = ReminderDataItem(
            title = "Gold Mary",
            description = "Look at the gold holy Mary",
            location = "Duomo di Milano",
            latitude = 45.46424806555846,
            longitude = 9.1919264967501
        )

        // WHEN: save the reminder entry into the local data source
        viewModel.validateAndSaveReminder(newReminder)

        // THEN: check that the reminder has been saved into the repo
        assertThat(
            dataSource.getReminder(newReminder.id),
            `is`(Result.Success(fromReminderToReminderDTO(newReminder)))
        )
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(viewModel.app.getString(R.string.reminder_saved))
        )
        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }


    private fun fromReminderToReminderDTO(r: ReminderDataItem): ReminderDTO {
        return ReminderDTO(
            title = r.title,
            description = r.description,
            location = r.location,
            latitude = r.latitude,
            longitude = r.longitude,
            id = r.id
        )
    }

}