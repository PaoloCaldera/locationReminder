package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var map: GoogleMap
    private lateinit var reminder: ReminderDataItem

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // Receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)
        // TODO: Add the implementation of the reminder details

        reminder = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        binding.reminderDataItem = reminder
        (binding.mapMarker as SupportMapFragment).getMapAsync(this)
        binding.lifecycleOwner = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0

        val reminderMapLocation = LatLng(reminder.latitude!!, reminder.longitude!!)

        map.addMarker(MarkerOptions().position(reminderMapLocation).title(reminder.description!!))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderMapLocation, 17f))
    }

    override fun onBackPressed() {
        navigateBackToRemindersList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBackToRemindersList()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateBackToRemindersList() {
        // Check if the main activity, RemindersActivity, is already present in the back stack
        val instance = supportFragmentManager.findFragmentByTag(RemindersActivity::class.java.name)

        if (instance != null && instance.isAdded)
            // If an instance of RemindersActivity is found, go back by simply popping off this activity
            supportFragmentManager.popBackStack()
        else {
            // In case of notification, no RemindersActivity
            val intent = Intent(this, RemindersActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}