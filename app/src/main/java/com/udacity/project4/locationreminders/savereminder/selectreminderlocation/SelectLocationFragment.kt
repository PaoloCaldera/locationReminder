package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE = 1
    private val ONLY_FOREGROUND_PERMISSION_REQUEST_CODE = 2
    private val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // TODO: add the map setup implementation
        // TODO: zoom to the user location after taking his permission
        // TODO: add style to the map
        // TODO: put a marker to location that the user selected

        (binding.map as SupportMapFragment).getMapAsync(this)


        // TODO: call this function after the user confirms on the selected location
        // onLocationSelected()
        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setMapStyle(map)
        setMapClick(map)
        setMapPoiClick(map)

        enableMyLocation()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        } catch (e: Resources.NotFoundException) {
            Log.e("SelectLocationFragment", "Can't find style. Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SelectLocationFragment", "Cannot apply map style. Error: ${e.message}")
        }
    }

    private fun setMapClick(map: GoogleMap) {
        // Remove all previous markers from the map
        map.clear()

        map.setOnMapClickListener {
            val locationString = String.format(
                Locale.getDefault(),
                "Lat %1$.3f - Long %2$.3f",
                it.latitude,
                it.longitude
            )

            map.addMarker(MarkerOptions().position(it).title(locationString))

            _viewModel.fillReminderLocationParameters(
                locationString, null, it.latitude, it.longitude
            )
        }
    }

    private fun setMapPoiClick(map: GoogleMap) {
        // Remove all previous markers from the app
        map.clear()

        map.setOnPoiClickListener {
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name))

            _viewModel.fillReminderLocationParameters(
                it.name, it, it.latLng.latitude, it.latLng.longitude
            )
        }
    }

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

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (arePermissionsGranted()) {
            checkDeviceLocationSettings()
            /*map.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude), 18f
                            )
                        )
                    }
                }*/
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
            if (it.isSuccessful) {...}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TURN_DEVICE_LOCATION_ON_REQUEST_CODE)
            checkDeviceLocationSettings(false)
    }

    /*
    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
    } */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}