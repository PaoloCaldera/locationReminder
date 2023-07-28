package com.udacity.project4.locationreminders.reminderslist

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.utils.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var repository: FakeAndroidDataSource

    @Before
    fun setup() {
        repository = FakeAndroidDataSource()
        ServiceLocator.repository = repository
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    @Test
    fun reminderListFragment_reminderList_checkContent() = runBlockingTest {
        // GIVEN: instantiate a reminder and save it into the data source
        val reminder = ReminderDTO(
            title = "Apples",
            description = "Remember to buy apples",
            location = "Esselunga",
            latitude = 45.56251607979835,
            longitude = 9.080693912097328
        )
        repository.saveReminder(reminder)

        // WHEN: launch ReminderListFragment to display the list of reminders
        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        // THEN: check that data is displayed
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.title))))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.description))))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.location))))

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun reminderListFragment_noData_checkNoData() {
        // GIVEN: no data

        // WHEN: launch ReminderListFragment to display the list of reminders
        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        // THEN: check that the "no data" view is visible on screen

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.noDataTextView))
            .check(matches(withText(appContext.getString(R.string.no_data))))

    }

    @Test
    fun reminderListFragment_shouldReturnError() {
        // GIVEN: set the repository so that it returns a fake error
        repository.setReturnError(true)

        // WHEN: launch ReminderListFragment to display the list of reminders
        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        // THEN: assert that the LiveData variable for displaying an error contains the error message
        onView(withText("TestException"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun reminderListFragment_navigateToSaveReminderFragment() {
        // GIVEN: launch ReminderListFragment to display the list of reminders
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        // WHEN: instantiate the navigation controller and navigate to SaveReminderFragment
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // THEN: Verify that the navigation has been performed correctly
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}