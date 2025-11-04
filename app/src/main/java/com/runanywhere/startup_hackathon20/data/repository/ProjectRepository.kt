package com.runanywhere.startup_hackathon20.data.repository

import android.util.Log
import com.runanywhere.startup_hackathon20.data.api.RetrofitClient
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import com.runanywhere.startup_hackathon20.data.models.CreateProjectRequest
import com.runanywhere.startup_hackathon20.data.models.Project
import com.runanywhere.startup_hackathon20.data.models.ProjectResponse
import com.runanywhere.startup_hackathon20.data.models.UpdateProjectRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ProjectResult {
    data class Success(val projects: List<Project>) : ProjectResult()
    data class SingleSuccess(val project: Project) : ProjectResult()
    data class Error(val message: String) : ProjectResult()
}

class ProjectRepository(private val tokenManager: TokenManager) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getProjects(): ProjectResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ProjectRepository", "Fetching projects...")
                Log.d(
                    "ProjectRepository",
                    "Access token: ${tokenManager.getAccessToken()?.substring(0, 10)}..."
                )

                val response = apiService.getProjects()

                Log.d("ProjectRepository", "Response code: ${response.code()}")
                Log.d("ProjectRepository", "Response message: ${response.message()}")
                Log.d("ProjectRepository", "Response body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val projectResponse = response.body()!!

                    Log.d(
                        "ProjectRepository",
                        "Project response success: ${projectResponse.success}"
                    )
                    Log.d("ProjectRepository", "Project response data: ${projectResponse.data}")

                    if (projectResponse.success && projectResponse.data != null) {
                        Log.d(
                            "ProjectRepository",
                            "Returning ${projectResponse.data.size} projects"
                        )
                        ProjectResult.Success(projectResponse.data)
                    } else {
                        Log.d("ProjectRepository", "No projects found, returning empty list")
                        ProjectResult.Success(emptyList())
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("ProjectRepository", "API error: $errorMessage")
                    ProjectResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Network error: ${e.message}", e)
                ProjectResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun getProject(projectId: String): ProjectResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ProjectRepository", "Fetching project: $projectId")
                val response = apiService.getProject(projectId)
                
                if (response.isSuccessful && response.body() != null) {
                    val projectResponse = response.body()!!
                    
                    if (projectResponse.success && projectResponse.data != null) {
                        ProjectResult.SingleSuccess(projectResponse.data)
                    } else {
                        ProjectResult.Error(projectResponse.message ?: "Project not found")
                    }
                } else {
                    ProjectResult.Error(response.message() ?: "Failed to fetch project")
                }
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error fetching project: ${e.message}", e)
                ProjectResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun createProject(name: String, description: String): ProjectResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ProjectRepository", "Creating project: $name")
                val response = apiService.createProject(CreateProjectRequest(name, description))
                
                if (response.isSuccessful && response.body() != null) {
                    val projectResponse = response.body()!!
                    
                    if (projectResponse.success && projectResponse.data != null) {
                        Log.d("ProjectRepository", "Project created successfully")
                        ProjectResult.SingleSuccess(projectResponse.data)
                    } else {
                        ProjectResult.Error(projectResponse.message ?: "Failed to create project")
                    }
                } else {
                    ProjectResult.Error(response.message() ?: "Failed to create project")
                }
            } catch (e: Exception) {
                Log.e("ProjectRepository", "Error creating project: ${e.message}", e)
                ProjectResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun updateProject(projectId: String, name: String?, description: String?): ProjectResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateProject(
                    projectId,
                    UpdateProjectRequest(name, description)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val projectResponse = response.body()!!
                    
                    if (projectResponse.success && projectResponse.data != null) {
                        ProjectResult.SingleSuccess(projectResponse.data)
                    } else {
                        ProjectResult.Error(projectResponse.message ?: "Failed to update project")
                    }
                } else {
                    ProjectResult.Error(response.message() ?: "Failed to update project")
                }
            } catch (e: Exception) {
                ProjectResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
    
    suspend fun deleteProject(projectId: String): ProjectResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteProject(projectId)
                
                if (response.isSuccessful) {
                    ProjectResult.Success(emptyList())
                } else {
                    ProjectResult.Error(response.message() ?: "Failed to delete project")
                }
            } catch (e: Exception) {
                ProjectResult.Error(e.message ?: "Network error occurred")
            }
        }
    }
}

