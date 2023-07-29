package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        // Enqueue the work that must be processed at a later time
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    /**
     * Handle the work that was previously enqueued
     */
    override fun onHandleWork(intent: Intent) {
        sendNotification(GeofencingEvent.fromIntent(intent)!!.triggeringGeofences!!)
    }

    /**
     * Recover the reminder data and send the notification
     */
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        for (geofence in triggeringGeofences) {

            // Get the request id of the current geofence
            val requestId = geofence.requestId

            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()

//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                // Get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)

                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    // Send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}