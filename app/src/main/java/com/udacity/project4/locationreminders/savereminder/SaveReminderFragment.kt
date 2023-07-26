package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            if (title.isNullOrEmpty() || description.isNullOrEmpty() || location.isNullOrEmpty() ||
                latitude == null || longitude == null
            ) {
                Toast.makeText(
                    requireContext(),
                    "Fill all the entries before saving",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Create a reminder with the selected parameters
                val reminder = ReminderDataItem(title, description, location, latitude, longitude)

                // Add a geofence associated to the reminder
                // Give to the geofence request the same id of the reminder
                addReminderGeofence(reminder)
            }
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    /**
     * Add the geofence associated to the created reminder
     */
    @SuppressLint("MissingPermission")
    private fun addReminderGeofence(reminder: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                _viewModel.latitude.value!!,
                _viewModel.longitude.value!!,
                GEOFENCE_RADIUS_M
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(
            geofencingRequest,
            (activity as RemindersActivity).geofencePendingIntent
        ).run {
            addOnSuccessListener {
                // Save the reminder to the local db
                _viewModel.validateAndSaveReminder(reminder)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Geofence adding request failed: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private const val GEOFENCE_RADIUS_M = 100f
    }
}