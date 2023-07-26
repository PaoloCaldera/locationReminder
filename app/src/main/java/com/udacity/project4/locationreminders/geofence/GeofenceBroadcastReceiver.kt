package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check that the intent action is actually that of a geofence event
        if (intent.action != RemindersActivity.ACTION_GEOFENCE_EVENT) {
            return
        }

        // Return if the retrieved geofencing event is null
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        // Check the goodness of the geofencing event
        if (
            geofencingEvent.hasError() ||
            geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofencingEvent.triggeringGeofences.isNullOrEmpty()
        ) {
            Log.e("GeofenceBroadcastReceiver", "Incorrect geofencing event triggered")
            return
        }

        // If the geofencing event has been successfully checked, enqueue the work
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}