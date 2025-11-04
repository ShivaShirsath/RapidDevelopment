package com.runanywhere.startup_hackathon20.data.api

import com.runanywhere.startup_hackathon20.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth Endpoints
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    // Project Endpoints
    @GET("projects")
    suspend fun getProjects(): Response<ProjectListResponse>
    
    @GET("projects/{id}")
    suspend fun getProject(@Path("id") projectId: String): Response<ProjectResponse>
    
    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectResponse>
    
    @PUT("projects/{id}")
    suspend fun updateProject(
        @Path("id") projectId: String,
        @Body request: UpdateProjectRequest
    ): Response<ProjectResponse>
    
    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") projectId: String): Response<ApiResponse<Unit>>
    
    // Task Endpoints
    @GET("tasks")
    suspend fun getTasks(@Query("projectId") projectId: String): Response<TaskListResponse>
    
    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskResponse>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: String,
        @Body request: UpdateTaskRequest
    ): Response<TaskResponse>
    
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: String): Response<ApiResponse<Unit>>
    
    // User Endpoints
    @GET("users/developers")
    suspend fun getDevelopers(): Response<UserListResponse>
}

