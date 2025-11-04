package com.runanywhere.startup_hackathon20.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.Project
import com.runanywhere.startup_hackathon20.data.repository.ProjectRepository
import com.runanywhere.startup_hackathon20.data.repository.ProjectResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProjectState {
    object Idle : ProjectState()
    object Loading : ProjectState()
    data class Success(val message: String) : ProjectState()
    data class Error(val message: String) : ProjectState()
}

class ProjectViewModel(private val projectRepository: ProjectRepository) : ViewModel() {
    
    private val _projectState = MutableStateFlow<ProjectState>(ProjectState.Idle)
    val projectState: StateFlow<ProjectState> = _projectState.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _projectState.value = ProjectState.Loading
            
            when (val result = projectRepository.getProjects()) {
                is ProjectResult.Success -> {
                    _projects.value = result.projects
                    _projectState.value = ProjectState.Idle
                    _isLoading.value = false
                }
                is ProjectResult.Error -> {
                    _projectState.value = ProjectState.Error(result.message)
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = projectRepository.getProject(projectId)) {
                is ProjectResult.SingleSuccess -> {
                    _selectedProject.value = result.project
                    _isLoading.value = false
                }
                is ProjectResult.Error -> {
                    _projectState.value = ProjectState.Error(result.message)
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            _projectState.value = ProjectState.Loading
            _isLoading.value = true
            
            when (val result = projectRepository.createProject(name, description)) {
                is ProjectResult.SingleSuccess -> {
                    // Optimistically add the new project to the list
                    val newProject = result.project
                    val currentProjects = _projects.value.toMutableList()
                    currentProjects.add(0, newProject) // Add at the beginning
                    _projects.value = currentProjects
                    
                    _projectState.value = ProjectState.Success("Project created successfully")
                    _isLoading.value = false
                    
                    // Refresh the list from server to ensure we have the latest data
                    loadProjects()
                }
                is ProjectResult.Error -> {
                    _projectState.value = ProjectState.Error(result.message)
                    _isLoading.value = false
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun updateProject(projectId: String, name: String?, description: String?) {
        viewModelScope.launch {
            _projectState.value = ProjectState.Loading
            
            when (val result = projectRepository.updateProject(projectId, name, description)) {
                is ProjectResult.SingleSuccess -> {
                    _projectState.value = ProjectState.Success("Project updated successfully")
                    loadProjects() // Refresh the list
                }
                is ProjectResult.Error -> {
                    _projectState.value = ProjectState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _projectState.value = ProjectState.Loading
            
            when (val result = projectRepository.deleteProject(projectId)) {
                is ProjectResult.Success -> {
                    _projectState.value = ProjectState.Success("Project deleted successfully")
                    loadProjects() // Refresh the list
                }
                is ProjectResult.Error -> {
                    _projectState.value = ProjectState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    fun setSelectedProject(project: Project) {
        _selectedProject.value = project
    }
    
    fun resetProjectState() {
        _projectState.value = ProjectState.Idle
    }
}

