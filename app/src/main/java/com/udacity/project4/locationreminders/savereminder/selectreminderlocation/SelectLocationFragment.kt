package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.util.Locale

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val REQUEST_LOCATION_PERMISSION = 1

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
        onLocationSelected()
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

            _viewModel.reminderSelectedLocationStr.value = locationString
            _viewModel.latitude.value = it.latitude
            _viewModel.longitude.value = it.longitude
        }
    }

    private fun setMapPoiClick(map: GoogleMap) {
        // Remove all previous markers from the app
        map.clear()

        map.setOnPoiClickListener {
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name))

            _viewModel.reminderSelectedLocationStr.value = it.name
            _viewModel.selectedPOI.value = it
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.longitude.value = it.latLng.longitude
        }
    }

    private fun isPermissionGranted(): Boolean {
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        return ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                .addOnSuccessListener {
                    if (it != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 18f
                        ))
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity().parent,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }

    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
    }

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