# 🔥 Firebender Prompt for Backend-Android Integration

Use this prompt with Firebender, Cursor AI, or any AI coding assistant to regenerate or modify the Android-Backend integration.

---

## Complete Prompt

```
I have two projects in the same workspace:

**1. Backend (Node.js/Express):**
- Location: /backend
- Base URL: /api/v1/
- Tech Stack: Node.js, Express, MongoDB, TypeScript
- Authentication: JWT Bearer tokens (accessToken + refreshToken)

API Endpoints:
- POST /api/v1/auth/register (body: {name, email, password, role})
- POST /api/v1/auth/login (body: {email, password})
- POST /api/v1/auth/logout (requires auth)
- POST /api/v1/auth/refresh-token (body: {refreshToken})
- GET /api/v1/projects (requires auth)
- GET /api/v1/projects/:id (requires auth)
- POST /api/v1/projects (body: {name, description}, requires auth)
- PUT /api/v1/projects/:id (body: {name?, description?}, requires auth)
- DELETE /api/v1/projects/:id (requires auth)
- GET /api/v1/tasks?projectId={id} (requires auth)
- POST /api/v1/tasks (body: {title, description, projectId, status?, blockReason?, assignedTo?}, requires auth)
- PUT /api/v1/tasks/:id (body: {title?, description?, status?, blockReason?, assignedTo?}, requires auth)
- DELETE /api/v1/tasks/:id (requires auth)

Response format: 
{
  success: boolean,
  message?: string,
  data?: any,
  error?: string
}

Task statuses: "to-do", "in-progress", "blocked", "done"

**2. Android Frontend (Kotlin/Compose):**
- Location: /app
- Package: com.runanywhere.startup_hackathon20
- Tech Stack: Kotlin, Jetpack Compose, Material 3
- Existing: RunAnywhere SDK integration, ChatViewModel, ChatScreen
- Dependencies already present: Retrofit, OkHttp, Gson, Room, EncryptedSharedPreferences

**Task:** Integrate the Android app with the backend API

**Requirements:**

1. **Data Layer:**
   - Create data models for User, Project, Task with proper serialization
   - Create Retrofit ApiService interface with all 13 endpoints
   - Configure RetrofitClient with:
     - Base URL (configurable for emulator/device/production)
     - OkHttp interceptor for automatic JWT token injection
     - Logging interceptor
     - Proper timeouts
   - Create TokenManager using EncryptedSharedPreferences for:
     - Storing accessToken and refreshToken securely
     - Storing user info (id, name, email, role)
     - isLoggedIn check
     - Clear tokens on logout
   - Create repositories:
     - AuthRepository: login, register, logout, isLoggedIn
     - ProjectRepository: getProjects, getProject, createProject, updateProject, deleteProject
     - TaskRepository: getTasks, createTask, updateTask, deleteTask
   - Use sealed classes for Result types (Success/Error)
   - Handle all errors gracefully

2. **Business Logic Layer:**
   - Create AuthViewModel with StateFlow for:
     - authState (Idle/Loading/Success/Error)
     - isLoggedIn
     - userName, userEmail
   - Create ProjectViewModel with StateFlow for:
     - projectState, projects list, selectedProject, isLoading
   - Create TaskViewModel with StateFlow for:
     - taskState, tasks list, isLoading
   - Use Kotlin Coroutines for async operations
   - Implement proper loading and error states

3. **UI Layer (Jetpack Compose with Material 3):**
   - LoginScreen:
     - Email and password fields with validation
     - Show/hide password toggle
     - Loading indicator during login
     - Error messages via Snackbar
     - Navigate to Register link
   - RegisterScreen:
     - Name, email, password, confirm password fields
     - Password match validation
     - Show/hide password toggles
     - Loading indicator
     - Navigate to Login link
   - ProjectListScreen:
     - List of projects in Cards
     - FloatingActionButton to create project
     - Dialog for creating project
     - Empty state when no projects
     - Logout menu option
     - Loading indicator
   - ProjectDetailScreen:
     - Show selected project name in TopAppBar
     - List of tasks with status chips
     - Color-coded status (to-do: secondary, in-progress: primary, blocked: error, done: tertiary)
     - FloatingActionButton to create task
     - Dialog for creating task
     - Dialog for editing task
     - Dropdown menu on tasks for Edit/Delete
     - Tap status chip to change status
     - Empty state when no tasks
     - Back button to return to projects
   
4. **Navigation:**
   - Use Jetpack Navigation Compose
   - Routes: Login, Register, ProjectList, ProjectDetail, Chat
   - Start destination based on isLoggedIn
   - Proper back stack management
   - Clear back stack on logout

5. **Application Setup:**
   - Update MyApplication.onCreate to:
     - Initialize TokenManager
     - Initialize RetrofitClient with TokenManager
   - Update MainActivity.onCreate to:
     - Initialize all repositories and ViewModels
     - Set up AppNavigation

6. **Code Quality:**
   - Follow MVVM architecture strictly
   - Separation of concerns
   - Type-safe code with proper null handling
   - Meaningful variable names
   - Proper error handling everywhere
   - Loading states for all async operations
   - Success/error feedback to users
   - Form validation
   - Confirmation dialogs for destructive actions

7. **Dependencies to add:**
   - androidx.navigation:navigation-compose:2.7.6
   - androidx.datastore:datastore-preferences:1.0.0
   - androidx.compose.material:material-icons-extended:1.6.0

8. **Keep Existing:**
   - Don't modify or remove ChatViewModel, ChatScreen, or RunAnywhere SDK code
   - Keep all existing dependencies
   - Preserve existing theme files

9. **Additional Features:**
   - Auto-login on app restart if tokens exist
   - Automatic token injection in API calls
   - Secure token storage
   - Material 3 design throughout
   - Empty states with helpful messages
   - Loading indicators during network calls
   - Error messages in Snackbars
   - Form field validation
   - Confirmation dialogs for delete operations

**File Structure to Create:**
```
app/src/main/java/com/runanywhere/startup_hackathon20/
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   └── RetrofitClient.kt
│   ├── models/
│   │   ├── User.kt
│   │   ├── Project.kt
│   │   ├── Task.kt
│   │   └── ApiResponse.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── ProjectRepository.kt
│   │   └── TaskRepository.kt
│   └── local/
│       └── TokenManager.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ProjectViewModel.kt
│   └── TaskViewModel.kt
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   └── projects/
│       ├── ProjectListScreen.kt
│       └── ProjectDetailScreen.kt
└── navigation/
    └── Navigation.kt
```

**Testing Requirements:**
The integration is successful when:
1. User can register and login
2. JWT tokens are stored securely
3. App auto-logins on restart
4. Projects list loads after login
5. User can create/delete projects
6. User can view project tasks
7. User can create/edit/delete tasks
8. User can change task status
9. Logout clears tokens and returns to login
10. All errors are handled with user-friendly messages

Make the code production-ready, following Android best practices and Kotlin coding conventions.
```

---

## Additional Context (Optional)

If the AI needs more context, provide:

```
The backend is already running and tested. Focus on Android implementation.

Backend Response Examples:

Login Success:
{
  "success": true,
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "_id": "user_id",
    "name": "John Doe",
    "email": "john@example.com",
    "role": "user"
  }
}

Get Projects Success:
{
  "success": true,
  "data": [
    {
      "_id": "project_id",
      "name": "Project Name",
      "description": "Project Description",
      "createdBy": "user_id",
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}

Get Tasks Success:
{
  "success": true,
  "data": [
    {
      "_id": "task_id",
      "title": "Task Title",
      "description": "Task Description",
      "status": "to-do",
      "projectId": "project_id",
      "blockReason": null,
      "assignedTo": null,
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}

Error Response:
{
  "success": false,
  "message": "Error message here"
}
```

---

## Customization Options

You can modify the prompt for:

1. **Different UI Framework:** Replace "Jetpack Compose" with "XML Views" or "Flutter"
2. **Different Architecture:** Replace "MVVM" with "MVI" or "Clean Architecture"
3. **Additional Features:** Add requirements like offline mode, search, filters, etc.
4. **Different Backend:** Modify API endpoints and response formats

---

## Usage Tips

1. **Copy the entire prompt** from the "Complete Prompt" section
2. **Paste it** into Firebender, Cursor AI, or your AI assistant
3. **Wait** for the AI to generate all files
4. **Review** the generated code
5. **Test** the integration
6. **Iterate** if needed by asking follow-up questions

---

## Follow-up Prompts

After the initial generation, you might need:

**Add Token Refresh:**
```
Add automatic token refresh when access token expires:
1. Intercept 401 responses
2. Call refresh-token endpoint
3. Retry original request
4. Logout if refresh fails
```

**Add Offline Support:**
```
Add offline support using Room database:
1. Cache projects and tasks locally
2. Sync when network available
3. Show cached data when offline
4. Add refresh indicators
```

**Add Search:**
```
Add search functionality:
1. Search bar in ProjectListScreen
2. Filter projects by name
3. Search tasks in ProjectDetailScreen
4. Highlight search results
```

---

## Expected Output

The AI should:
1. ✅ Create 18 new Kotlin files
2. ✅ Update 2 existing files (MainActivity, MyApplication)
3. ✅ Add 3 new dependencies to build.gradle.kts
4. ✅ Implement complete MVVM architecture
5. ✅ Create working UI screens with Material 3
6. ✅ Set up navigation
7. ✅ Handle all edge cases and errors
8. ✅ Provide production-ready code

---

**Save this prompt for future use! 🔥**

