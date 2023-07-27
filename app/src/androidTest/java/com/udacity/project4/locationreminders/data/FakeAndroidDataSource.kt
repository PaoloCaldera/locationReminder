package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidDataSource(private var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (reminders == null)
            Result.Error("Reminders list not found")
        else
            Result.Success(reminders!!)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders?.find { it.id == id }

        return if (reminder == null)
            Result.Error("Reminder not found!")
        else
            Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}