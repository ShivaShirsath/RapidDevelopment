# Android-Backend Integration Guide

## 🎉 Integration Complete!

Your Android app has been successfully integrated with the Node.js backend. This guide will help you configure and test the integration.

---

## 📁 Project Structure

```
app/src/main/java/com/runanywhere/startup_hackathon20/
├── data/
│   ├── api/
│   │   ├── ApiService.kt           # Retrofit API interface
│   │   └── RetrofitClient.kt       # Retrofit configuration
│   ├── models/
│   │   ├── User.kt                 # User models
│   │   ├── Project.kt              # Project models
│   │   ├── Task.kt                 # Task models
│   │   └── ApiResponse.kt          # Generic API responses
│   ├── repository/
│   │   ├── AuthRepository.kt       # Authentication logic
│   │   ├── ProjectRepository.kt    # Project CRUD operations
│   │   └── TaskRepository.kt       # Task CRUD operations
│   └── local/
│       └── TokenManager.kt         # Secure JWT storage
├── viewmodel/
│   ├── AuthViewModel.kt            # Auth state management
│   ├── ProjectViewModel.kt         # Project state management
│   └── TaskViewModel.kt            # Task state management
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt          # Login UI
│   │   └── RegisterScreen.kt       # Registration UI
│   └── projects/
│       ├── ProjectListScreen.kt    # Projects list
│       └── ProjectDetailScreen.kt  # Project tasks
├── navigation/
│   └── Navigation.kt               # App navigation
├── MainActivity.kt                 # Main entry point
└── MyApplication.kt                # App initialization
```

---

## ⚙️ Configuration Steps

### 1. **Configure Backend URL**

Open `app/src/main/java/com/runanywhere/startup_hackathon20/data/api/RetrofitClient.kt` and update the `BASE_URL`:

```kotlin
// For Android Emulator (localhost)
private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

// For Physical Device (replace with your computer's IP)
private const val BASE_URL = "http://192.168.1.X:3000/api/v1/"

// For Production
private const val BASE_URL = "https://your-backend-url.com/api/v1/"
```

**Finding your local IP:**
- **Mac/Linux:** `ifconfig | grep "inet " | grep -v 127.0.0.1`
- **Windows:** `ipconfig` and look for IPv4 Address

---

### 2. **Start the Backend Server**

```bash
cd backend
npm install
npm run dev
```

The backend should start on `http://localhost:3000`

**Verify it's running:**
```bash
curl http://localhost:3000/api
```
You should see the Swagger documentation.

---

### 3. **Build and Run Android App**

```bash
./gradlew assembleDebug
# or use Android Studio
```

---

## 🔐 Authentication Flow

### User Registration
1. Open the app → Register screen
2. Enter: Name, Email, Password, Confirm Password
3. Tap "Register"
4. App automatically logs you in and navigates to Projects

### User Login
1. Enter: Email and Password
2. Tap "Login"
3. JWT tokens are stored securely using EncryptedSharedPreferences
4. Navigates to Projects screen

### Token Management
- **Access Token**: Used for API authentication (stored in `Authorization: Bearer {token}`)
- **Refresh Token**: Used to get new access tokens
- **Auto-logout**: If token expires or is invalid

---

## 📊 Features Implemented

### ✅ Authentication
- [x] User Registration
- [x] User Login
- [x] Secure token storage (EncryptedSharedPreferences)
- [x] Auto-login on app restart
- [x] Logout functionality

### ✅ Project Management
- [x] Create Project
- [x] List all Projects
- [x] View Project details
- [x] Update Project
- [x] Delete Project

### ✅ Task Management
- [x] Create Task
- [x] List Tasks by Project
- [x] Update Task (title, description, status)
- [x] Change Task Status (to-do, in-progress, blocked, done)
- [x] Delete Task

### ✅ UI/UX
- [x] Material 3 Design
- [x] Loading states
- [x] Error handling with Snackbars
- [x] Form validation
- [x] Empty states
- [x] Confirmation dialogs

---

## 🧪 Testing the Integration

### 1. **Test Registration**
```
1. Open app
2. Click "Don't have an account? Register"
3. Fill form:
   - Name: John Doe
   - Email: john@example.com
   - Password: password123
   - Confirm Password: password123
4. Tap Register
5. Should navigate to Projects screen
```

### 2. **Test Project Creation**
```
1. On Projects screen, tap the "+" FAB
2. Enter:
   - Project Name: My First Project
   - Description: Testing project creation
3. Tap Create
4. Project should appear in the list
```

### 3. **Test Task Creation**
```
1. Tap on a project to open it
2. Tap the "+" FAB
3. Enter:
   - Task Title: First Task
   - Description: This is a test task
4. Tap Create
5. Task should appear with "to-do" status
```

### 4. **Test Task Status Change**
```
1. In Project Detail screen
2. Tap on task status chip
3. Select new status (e.g., "in-progress")
4. Status should update and color change
```

---

## 🔧 Backend API Endpoints Used

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `POST /api/v1/auth/logout` - Logout user
- `POST /api/v1/auth/refresh-token` - Refresh access token

### Projects
- `GET /api/v1/projects` - Get all projects
- `GET /api/v1/projects/:id` - Get single project
- `POST /api/v1/projects` - Create project
- `PUT /api/v1/projects/:id` - Update project
- `DELETE /api/v1/projects/:id` - Delete project

### Tasks
- `GET /api/v1/tasks?projectId={id}` - Get tasks by project
- `POST /api/v1/tasks` - Create task
- `PUT /api/v1/tasks/:id` - Update task
- `DELETE /api/v1/tasks/:id` - Delete task

---

## 🐛 Troubleshooting

### Issue: "Network error occurred"
**Solution:**
1. Check if backend is running
2. Verify BASE_URL is correct
3. Check internet permission in AndroidManifest.xml
4. For emulator: Use `10.0.2.2` instead of `localhost`
5. For physical device: Ensure device and computer are on same WiFi

### Issue: "Unauthorized" error
**Solution:**
1. Login again
2. Check if tokens are being saved (add logs in TokenManager)
3. Verify backend authentication middleware is working

### Issue: App crashes on startup
**Solution:**
1. Check Logcat for errors
2. Verify all ViewModels are initialized correctly
3. Ensure TokenManager is initialized in MyApplication

### Issue: "Failed to fetch projects"
**Solution:**
1. Ensure you're logged in
2. Check if backend has CORS enabled
3. Verify JWT token is valid
4. Check backend logs for errors

---

## 📱 Network Configuration for Different Scenarios

### Android Emulator
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"
```

### Physical Device (Same WiFi)
```bash
# Find your computer's IP
ifconfig en0 | grep "inet " | awk '{print $2}'
# Example output: 192.168.1.100
```
```kotlin
private const val BASE_URL = "http://192.168.1.100:3000/api/v1/"
```

### Production
```kotlin
private const val BASE_URL = "https://your-api.com/api/v1/"
```

---

## 🔒 Security Features

1. **Encrypted Storage**: JWT tokens stored using EncryptedSharedPreferences
2. **HTTPS Ready**: Production should use HTTPS
3. **Password Validation**: Passwords must match during registration
4. **Token Refresh**: Automatic token refresh (implement if needed)

---

## 🚀 Next Steps

### Recommended Enhancements
1. **Add Token Refresh Interceptor**: Auto-refresh expired tokens
2. **Offline Support**: Cache data using Room database
3. **Pull-to-Refresh**: Add SwipeRefresh to lists
4. **Search & Filter**: Add search functionality for projects/tasks
5. **User Profile**: Display user info, change password
6. **Push Notifications**: Notify about task updates
7. **Dark Mode**: Already supported via Material 3
8. **Pagination**: Implement pagination for large lists
9. **File Attachments**: Upload files to tasks
10. **Real-time Updates**: WebSocket for live updates

### Backend Enhancements Needed
1. **Token Refresh Endpoint**: Implement automatic token refresh
2. **Password Reset**: Email-based password reset
3. **User Roles**: Implement role-based access control
4. **Search API**: Backend search endpoints
5. **WebSocket**: Real-time updates

---

## 📚 Dependencies Added

```gradle
// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")

// DataStore for token storage
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Compose Icons
implementation("androidx.compose.material:material-icons-extended:1.6.0")

// Already existing:
// - Retrofit (networking)
// - OkHttp (HTTP client)
// - Gson (JSON parsing)
// - EncryptedSharedPreferences (secure storage)
```

---

## 🎯 Architecture

**Pattern**: MVVM (Model-View-ViewModel)
- **Models**: Data classes representing API responses
- **Repository**: Data access layer, handles API calls
- **ViewModel**: Business logic, state management
- **UI**: Composable functions for screens

**Benefits**:
- Separation of concerns
- Testable code
- Reactive UI with StateFlow
- Lifecycle-aware components

---

## 📞 Support

If you encounter issues:
1. Check Logcat for detailed errors
2. Verify backend logs
3. Test API endpoints with Postman/curl
4. Check network connectivity
5. Review this guide

---

## ✨ Success Indicators

Your integration is working when:
- ✅ User can register and login
- ✅ Projects list loads after login
- ✅ New projects can be created
- ✅ Project details show tasks
- ✅ Tasks can be created and updated
- ✅ Task status changes reflect immediately
- ✅ Logout works and returns to login screen
- ✅ App remembers login on restart

---

**Integration completed successfully! 🎉**

For questions or issues, refer to the backend API documentation at:
`http://localhost:3000/api` (Swagger UI)

