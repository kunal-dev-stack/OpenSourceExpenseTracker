package com.zenith.expensetracker.data.local.dao

import androidx.room.*
import com.zenith.expensetracker.data.model.ReminderSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder_settings WHERE id = 1 LIMIT 1")
    fun getReminderSettings(): Flow<ReminderSetting?>

    @Query("SELECT * FROM reminder_settings WHERE id = 1 LIMIT 1")
    suspend fun getReminderSettingsSync(): ReminderSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: ReminderSetting)
}
