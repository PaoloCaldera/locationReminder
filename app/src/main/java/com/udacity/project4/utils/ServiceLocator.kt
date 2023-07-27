package com.udacity.project4.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository

object ServiceLocator {

    // Repository can be accessed by multiple threads
    @Volatile
    var repository: ReminderDataSource? = null
        @VisibleForTesting set

    var database: RemindersDatabase? = null

    fun getRepository(context: Context): ReminderDataSource {
        synchronized(this) {
            return repository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): ReminderDataSource {
        val newRepo = RemindersLocalRepository(createRemindersDao(context))
        repository = newRepo
        return newRepo
    }

    fun createRemindersDao(context: Context): RemindersDao {
        val database = database ?: createDatabase(context)
        return database.reminderDao()
    }

    private fun createDatabase(context: Context): RemindersDatabase {
        val db = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build()
        database = db

        return db
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock = Any()) {
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            repository = null
        }
    }

}
