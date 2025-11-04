package com.runanywhere.startup_hackathon20.data.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

data class ApiError(
    val success: Boolean = false,
    val message: String
)

