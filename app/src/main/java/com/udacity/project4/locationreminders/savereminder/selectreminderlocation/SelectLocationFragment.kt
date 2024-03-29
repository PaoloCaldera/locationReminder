package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setMapStyle()               // Style the map
        setMapClick()               // Handle a generic click on the map
        setMapPoiClick()            // Handle the click on a POI

        // Move the device to the current location
        moveToCurrentLocation()
    }


    /**
     * Set the style of the map with the map_style.json file in resources folder
     */
    private fun setMapStyle() {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
    }

    /**
     * Add a marker on the map when the user taps on and save the marker location
     */
    private fun setMapClick() {

        map.setOnMapClickListener {
            // Remove previous markers from the map
            map.clear()

            // Add new marker
            val locationString = String.format(
                Locale.getDefault(),
                "Lat %1$.3f, Long %2$.3f",
                it.latitude,
                it.longitude
            )
            map.addMarker(MarkerOptions().position(it).title(locationString))

            _viewModel.fillReminderLocationParameters(
                locationString, null, it.latitude, it.longitude
            )
        }
    }

    /**
     * Add a marker to the selected POI and save its location
     */
    private fun setMapPoiClick() {

        map.setOnPoiClickListener {
            // Remove previous markers from the map
            map.clear()

            // Add new marker
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name))

            _viewModel.fillReminderLocationParameters(
                it.name, it, it.latLng.latitude, it.latLng.longitude
            )
        }
    }

    /**
     * Move to the user current location if all the conditions are met
     */
    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        map.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener {
                if (it != null) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 18f
                        )
                    )
                }
            }
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}