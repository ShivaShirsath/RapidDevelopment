package com.runanywhere.startup_hackathon20.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.Task
import com.runanywhere.startup_hackathon20.data.repository.TaskRepository
import com.runanywhere.startup_hackathon20.data.repository.TaskResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TaskState {
    object Idle : TaskState()
    object Loading : TaskState()
    data class Success(val message: String) : TaskState()
    data class Error(val message: String) : TaskState()
}

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    
    private val _taskState = MutableStateFlow<TaskState>(TaskState.Idle)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadTasks(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _taskState.value = TaskState.Loading
            
            when (val result = taskRepository.getTasks(projectId)) {
                is TaskResult.Success -> {
                    _tasks.value = result.tasks
                    _taskState.value = TaskState.Idle
                    _isLoading.value = false
                }
                is TaskResult.Error -> {
                    _taskState.value = TaskState.Error(result.message)
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun createTask(
        title: String,
        description: String,
        projectId: String,
        status: String = "to-do",
        blockReason: String? = null,
        assignedTo: String? = null
    ) {
        viewModelScope.launch {
            _taskState.value = TaskState.Loading
            
            when (val result = taskRepository.createTask(title, description, projectId, status, blockReason, assignedTo)) {
                is TaskResult.SingleSuccess -> {
                    _taskState.value = TaskState.Success("Task created successfully")
                    loadTasks(projectId) // Refresh the list
                }
                is TaskResult.Error -> {
                    _taskState.value = TaskState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun updateTask(
        taskId: String,
        projectId: String,
        title: String? = null,
        description: String? = null,
        status: String? = null,
        blockReason: String? = null,
        assignedTo: String? = null
    ) {
        viewModelScope.launch {
            _taskState.value = TaskState.Loading
            
            when (val result = taskRepository.updateTask(taskId, title, description, status, blockReason, assignedTo)) {
                is TaskResult.SingleSuccess -> {
                    _taskState.value = TaskState.Success("Task updated successfully")
                    loadTasks(projectId) // Refresh the list
                }
                is TaskResult.Error -> {
                    _taskState.value = TaskState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun deleteTask(taskId: String, projectId: String) {
        viewModelScope.launch {
            _taskState.value = TaskState.Loading
            
            when (val result = taskRepository.deleteTask(taskId)) {
                is TaskResult.Success -> {
                    _taskState.value = TaskState.Success("Task deleted successfully")
                    loadTasks(projectId) // Refresh the list
                }
                is TaskResult.Error -> {
                    _taskState.value = TaskState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun resetTaskState() {
        _taskState.value = TaskState.Idle
    }
}

