package com.runanywhere.startup_hackathon20.data.models

import com.google.gson.annotations.SerializedName

enum class TaskStatus {
    @SerializedName("to-do")
    TODO,
    @SerializedName("in-progress")
    IN_PROGRESS,
    @SerializedName("blocked")
    BLOCKED,
    @SerializedName("done")
    DONE;
    
    fun toApiString(): String {
        return when (this) {
            TODO -> "to-do"
            IN_PROGRESS -> "in-progress"
            BLOCKED -> "blocked"
            DONE -> "done"
        }
    }
    
    companion object {
        fun fromString(value: String): TaskStatus {
            return when (value) {
                "to-do" -> TODO
                "in-progress" -> IN_PROGRESS
                "blocked" -> BLOCKED
                "done" -> DONE
                else -> TODO
            }
        }
    }
}

data class Task(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("_id")
    val _id: String? = null,
    val title: String,
    val description: String,
    val status: String = "to-do",
    val blockReason: String? = null,
    val projectId: String? = null,
    val assignedTo: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // Helper property to get the ID from either field
    val taskId: String
        get() = id ?: _id ?: ""
}

data class CreateTaskRequest(
    val title: String,
    val description: String,
    val projectId: String,
    val status: String = "to-do",
    val blockReason: String? = null,
    val assignedTo: String? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val blockReason: String? = null,
    val assignedTo: String? = null
)

data class TaskResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Task? = null
)

data class TaskListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Task>? = null
)

