package com.example.yourassistantyora.repository

import com.example.yourassistantyora.models.TaskModel
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeMyTasks(): Flow<List<TaskModel>>
    suspend fun addTask(task: TaskModel)
    suspend fun updateStatus(taskId: String, status: Int)
    suspend fun deleteTask(taskId: String)
}
