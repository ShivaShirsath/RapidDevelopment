package com.runanywhere.startup_hackathon20.data.repository

import android.util.Log
import com.runanywhere.startup_hackathon20.data.api.RetrofitClient
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import com.runanywhere.startup_hackathon20.data.models.User
import com.runanywhere.startup_hackathon20.data.models.UserListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class UserResult {
    data class Success(val users: List<User>) : UserResult()
    data class Error(val message: String) : UserResult()
}

class UserRepository(private val tokenManager: TokenManager) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getDevelopers(): UserResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Fetching developers...")
                val response = apiService.getDevelopers()

                Log.d("UserRepository", "Developers response code: ${response.code()}")
                Log.d("UserRepository", "Developers response body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()!!
                    
                    if (userResponse.success && userResponse.data != null) {
                        Log.d("UserRepository", "Found ${userResponse.data.size} developers")
                        UserResult.Success(userResponse.data)
                    } else {
                        Log.d("UserRepository", "No developers found, returning empty list")
                        UserResult.Success(emptyList())
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("UserRepository", "API error: $errorMessage")
                    UserResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Network error: ${e.message}", e)
                UserResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
}

