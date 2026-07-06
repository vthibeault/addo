package com.addo.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE parentId = :parentId ORDER BY createdAt ASC")
    fun observeSubtasks(parentId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE reminderAt IS NOT NULL AND done = 0 AND reminderAt > :nowMillis")
    suspend fun withPendingReminders(nowMillis: Long): List<Task>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET done = :done, completedAt = :completedAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE done = 1 AND parentId IS NULL AND completedAt < :beforeMillis")
    suspend fun purgeDoneBefore(beforeMillis: Long)
}
