package com.runanywhere.startup_hackathon20.data.repository

import android.util.Log
import com.runanywhere.startup_hackathon20.data.api.RetrofitClient
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import com.runanywhere.startup_hackathon20.data.models.CreateTaskRequest
import com.runanywhere.startup_hackathon20.data.models.Task
import com.runanywhere.startup_hackathon20.data.models.UpdateTaskRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class TaskResult {
    data class Success(val tasks: List<Task>) : TaskResult()
    data class SingleSuccess(val task: Task) : TaskResult()
    data class Error(val message: String) : TaskResult()
}

class TaskRepository(private val tokenManager: TokenManager) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getTasks(projectId: String): TaskResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TaskRepository", "Fetching tasks for project: $projectId")
                val response = apiService.getTasks(projectId)

                Log.d("TaskRepository", "Tasks response code: ${response.code()}")
                Log.d("TaskRepository", "Tasks response body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val taskResponse = response.body()!!
                    
                    if (taskResponse.success && taskResponse.data != null) {
                        Log.d("TaskRepository", "Found ${taskResponse.data.size} tasks")
                        TaskResult.Success(taskResponse.data)
                    } else {
                        Log.d("TaskRepository", "No tasks found, returning empty list")
                        TaskResult.Success(emptyList())
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("TaskRepository", "API error: $errorMessage")
                    TaskResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Network error: ${e.message}", e)
                TaskResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun createTask(
        title: String,
        description: String,
        projectId: String,
        status: String = "to-do",
        blockReason: String? = null,
        assignedTo: String? = null
    ): TaskResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TaskRepository", "Creating task with title: $title")
                val response = apiService.createTask(
                    CreateTaskRequest(title, description, projectId, status, blockReason, assignedTo)
                )

                Log.d("TaskRepository", "Create task response code: ${response.code()}")
                Log.d("TaskRepository", "Create task response body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val taskResponse = response.body()!!
                    
                    if (taskResponse.success && taskResponse.data != null) {
                        Log.d("TaskRepository", "Task created successfully")
                        TaskResult.SingleSuccess(taskResponse.data)
                    } else {
                        Log.d("TaskRepository", "Failed to create task")
                        TaskResult.Error(taskResponse.message ?: "Failed to create task")
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("TaskRepository", "API error: $errorMessage")
                    TaskResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Network error: ${e.message}", e)
                TaskResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun updateTask(
        taskId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        blockReason: String? = null,
        assignedTo: String? = null
    ): TaskResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TaskRepository", "Updating task with id: $taskId")
                val response = apiService.updateTask(
                    taskId,
                    UpdateTaskRequest(title, description, status, blockReason, assignedTo)
                )

                Log.d("TaskRepository", "Update task response code: ${response.code()}")
                Log.d("TaskRepository", "Update task response body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val taskResponse = response.body()!!
                    
                    if (taskResponse.success && taskResponse.data != null) {
                        Log.d("TaskRepository", "Task updated successfully")
                        TaskResult.SingleSuccess(taskResponse.data)
                    } else {
                        Log.d("TaskRepository", "Failed to update task")
                        TaskResult.Error(taskResponse.message ?: "Failed to update task")
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("TaskRepository", "API error: $errorMessage")
                    TaskResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Network error: ${e.message}", e)
                TaskResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun deleteTask(taskId: String): TaskResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("TaskRepository", "Deleting task with id: $taskId")
                val response = apiService.deleteTask(taskId)

                Log.d("TaskRepository", "Delete task response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("TaskRepository", "Task deleted successfully")
                    TaskResult.Success(emptyList())
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("TaskRepository", "API error: $errorMessage")
                    TaskResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Network error: ${e.message}", e)
                TaskResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
}

