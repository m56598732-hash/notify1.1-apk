package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppControlState
import com.example.data.model.DailySkipRecord
import com.example.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

  @Query("SELECT * FROM notifications ORDER BY hour ASC, minute ASC")
  fun observeAllNotifications(): Flow<List<NotificationItem>>

  @Query("SELECT * FROM notifications WHERE isEnabled = 1")
  suspend fun getEnabledNotifications(): List<NotificationItem>

  @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
  suspend fun getNotificationById(id: Long): NotificationItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(item: NotificationItem): Long

  @Update
  suspend fun updateNotification(item: NotificationItem)

  @Delete
  suspend fun deleteNotification(item: NotificationItem)

  @Query("DELETE FROM notifications WHERE id = :id")
  suspend fun deleteNotificationById(id: Long)

  @Query("UPDATE notifications SET isEnabled = :isEnabled WHERE id = :id")
  suspend fun setEnabled(id: Long, isEnabled: Boolean)

  // App Control (Stop / Restart / Scheduled Resume)
  @Query("SELECT * FROM app_control WHERE id = 1 LIMIT 1")
  fun observeAppControl(): Flow<AppControlState?>

  @Query("SELECT * FROM app_control WHERE id = 1 LIMIT 1")
  suspend fun getAppControl(): AppControlState?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setAppControl(control: AppControlState)

  // Daily Skip Operations
  @Query("SELECT * FROM daily_skips WHERE dateString = :dateString LIMIT 1")
  suspend fun getDailySkip(dateString: String): DailySkipRecord?

  @Query("SELECT * FROM daily_skips WHERE dateString = :dateString LIMIT 1")
  fun observeDailySkip(dateString: String): Flow<DailySkipRecord?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDailySkip(record: DailySkipRecord)

  @Query("DELETE FROM daily_skips WHERE dateString = :dateString")
  suspend fun removeDailySkip(dateString: String)
}
