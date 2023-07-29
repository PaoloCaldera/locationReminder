package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders, container, false
        )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setTitle(getString(R.string.app_name))

        // Get the updated reminders list when refreshing
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            // Navigate to SaveReminderFragment
            navigateToAddReminder()
        }

        // Navigate to AuthenticationActivity if user is not logged-in
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this.activity, AuthenticationActivity::class.java))
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the ui
        _viewModel.loadReminders()
    }

    /**
     * Navigate to the add-reminder fragment to add a reminder
     */
    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

    /**
     * Setup the recycler view for the list fragment
     */
    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            val intent = ReminderDescriptionActivity.newIntent(requireContext(), it)
            startActivity(intent)
        }
        // Setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                // When clearing the list, remove all the reminders from the local db but also
                // all the pending geofencing requests
                _viewModel.clearRemindersList()
                geofencingClient.removeGeofences(
                    (activity as RemindersActivity).geofencePendingIntent
                )
            }
            R.id.logout -> {
                // User logout implementation
                AuthUI.getInstance().signOut(requireContext())
                startActivity(Intent(this.activity, AuthenticationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}