package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    private var permissionsAndDeviceLocationEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_reminders, container, false
        )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            if (permissionsAndDeviceLocationEnabled)
                navigateToAddReminder()
            else
                enablePermissionsAndDeviceLocation()
        }

        // Navigate to AuthenticationActivity if user is not logged-in
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this.activity, AuthenticationActivity::class.java))
        }

        enablePermissionsAndDeviceLocation()
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
        val adapter = RemindersListAdapter {}
        // Setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    /**
     * Make the user enable foreground and background permission if not already granted
     */
    @SuppressLint("MissingPermission")
    private fun enablePermissionsAndDeviceLocation() {
        if (arePermissionsGranted()) {
            checkDeviceLocationSettings()
        } else {
            var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE
                } else ONLY_FOREGROUND_PERMISSION_REQUEST_CODE
            ActivityCompat.requestPermissions(
                this.requireActivity().parent,
                permissionArray,
                resultCode
            )
        }
    }

    /**
     * Check if foreground and background location permissions are granted
     */
    private fun arePermissionsGranted(): Boolean {
        val foregroundPermissionApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else true

        return (foregroundPermissionApproved && backgroundPermissionApproved)
    }

    /**
     * Handle the result of the permission request: if not granted, go to the settings activity
     * and make the user grant them
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED ||
            requestCode == FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE &&
            grantResults[1] == PackageManager.PERMISSION_DENIED
        ) {

            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
                .show()
        } else checkDeviceLocationSettings()
    }

    /**
     * Check if the device location is turned on: if not, ask the user to turn it on
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())

        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener {
            if (it is ResolvableApiException && resolve) {
                try {
                    it.startResolutionForResult(
                        requireActivity().parent,
                        TURN_DEVICE_LOCATION_ON_REQUEST_CODE
                    )
                } catch (sendException: IntentSender.SendIntentException) {
                    Toast.makeText(
                        requireContext(),
                        "Error getting location settings resolution: ${sendException.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                permissionsAndDeviceLocationEnabled = true
            }
        }
    }

    /**
     * Handle the result of the turning-on location request: strongly suggest the user to turn
     * the device location on
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TURN_DEVICE_LOCATION_ON_REQUEST_CODE)
            checkDeviceLocationSettings(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                // TODO: add the logout implementation
                AuthUI.getInstance().signOut(requireContext())
                startActivity(Intent(this.activity, AuthenticationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE = 401
        private const val ONLY_FOREGROUND_PERMISSION_REQUEST_CODE = 402
        private const val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 403
    }
}