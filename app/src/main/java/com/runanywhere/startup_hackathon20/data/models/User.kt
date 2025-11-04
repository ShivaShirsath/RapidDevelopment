package com.runanywhere.startup_hackathon20.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("_id")
    val _id: String? = null,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String? = null
) {
    // Helper property to get the ID from either field
    val userId: String
        get() = id ?: _id ?: ""
}

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "developer"
)

data class AuthData(
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AuthData? = null,
    val user: User? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class UserListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<User>? = null
)

