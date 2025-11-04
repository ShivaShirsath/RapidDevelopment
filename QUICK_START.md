# 🚀 Quick Start Guide

## Prerequisites
- ✅ Android Studio installed
- ✅ Node.js installed
- ✅ Backend code in `/backend` folder
- ✅ Android code in `/app` folder

---

## ⚡ 3-Step Setup

### Step 1: Configure Backend URL

Edit `app/src/main/java/com/runanywhere/startup_hackathon20/data/api/RetrofitClient.kt`:

```kotlin
// Line 13: Change BASE_URL based on your setup

// Using Android Emulator?
private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

// Using Physical Device?
// Find your IP: ifconfig (Mac) or ipconfig (Windows)
private const val BASE_URL = "http://YOUR_IP:3000/api/v1/"
```

---

### Step 2: Start Backend

```bash
cd backend
npm install
npm run dev
```

Wait for: `Server is running on port 3000`

---

### Step 3: Run Android App

**Option A: Android Studio**
1. Open project in Android Studio
2. Click Run ▶️

**Option B: Command Line**
```bash
./gradlew installDebug
```

---

## ✅ Verify Integration

1. **App opens** → Should show Login screen
2. **Click "Register"** → Fill form and register
3. **After registration** → Should navigate to Projects screen
4. **Create a project** → Tap the + button
5. **Open the project** → Tap on project card
6. **Create a task** → Tap the + button

If all steps work: **✨ Integration successful!**

---

## 🆘 Quick Fixes

### "Network error"
```bash
# Check if backend is running
curl http://localhost:3000/api

# If not running:
cd backend && npm run dev
```

### Can't connect from Android
```bash
# Find your computer's IP
ifconfig | grep "inet " | grep -v 127.0.0.1

# Update BASE_URL in RetrofitClient.kt with your IP
```

### App crashes
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

---

## 📖 Full Documentation

See `BACKEND_INTEGRATION_GUIDE.md` for:
- Complete architecture
- API endpoints
- Troubleshooting
- Advanced features
- Testing guide

---

## 🎯 What's Integrated?

✅ User authentication (Register/Login/Logout)  
✅ Project management (CRUD)  
✅ Task management (CRUD)  
✅ Secure token storage  
✅ Material 3 UI  
✅ Error handling  
✅ Loading states  

---

**Ready to build! 🔨**

