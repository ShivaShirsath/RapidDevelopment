package com.runanywhere.startup_hackathon20.data.repository

import com.runanywhere.startup_hackathon20.data.api.RetrofitClient
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import com.runanywhere.startup_hackathon20.data.models.AuthRequest
import com.runanywhere.startup_hackathon20.data.models.AuthResponse
import com.runanywhere.startup_hackathon20.data.models.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val response: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val tokenManager: TokenManager) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun login(email: String, password: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(AuthRequest(email, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    
                    if (authResponse.success && authResponse.data != null) {
                        val token = authResponse.data.token
                        val refreshToken = authResponse.data.refreshToken
                        
                        // Save tokens
                        tokenManager.saveTokens(token, refreshToken)
                        
                        // Save user info
                        authResponse.user?.let { user ->
                            tokenManager.saveUserInfo(user.id.toString(), user.name, user.email, user.role)
                        }
                        
                        AuthResult.Success(authResponse)
                    } else {
                        AuthResult.Error(authResponse.message ?: "Login failed")
                    }
                } else {
                    AuthResult.Error(response.message() ?: "Login failed")
                }
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun register(name: String, email: String, password: String, role: String = "developer"): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(name, email, password, role))
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    
                    if (authResponse.success && authResponse.data != null) {
                        val token = authResponse.data.token
                        val refreshToken = authResponse.data.refreshToken
                        
                        // Save tokens
                        tokenManager.saveTokens(token, refreshToken)
                        
                        // Save user info if available
                        authResponse.user?.let { user ->
                            tokenManager.saveUserInfo(user.id.toString(), user.name, user.email, user.role)
                        }
                        
                        AuthResult.Success(authResponse)
                    } else {
                        AuthResult.Error(authResponse.message ?: "Registration failed")
                    }
                } else {
                    AuthResult.Error(response.message() ?: "Registration failed")
                }
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun logout(): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logout()
                
                // Clear local tokens regardless of server response
                tokenManager.clearTokens()
                
                if (response.isSuccessful) {
                    AuthResult.Success(AuthResponse(success = true, message = "Logged out successfully"))
                } else {
                    AuthResult.Success(AuthResponse(success = true, message = "Logged out locally"))
                }
            } catch (e: Exception) {
                // Still clear tokens even if network fails
                tokenManager.clearTokens()
                AuthResult.Success(AuthResponse(success = true, message = "Logged out locally"))
            }
        }
    }
    
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    fun getUserName(): String? {
        return tokenManager.getUserName()
    }
    
    fun getUserEmail(): String? {
        return tokenManager.getUserEmail()
    }
}

