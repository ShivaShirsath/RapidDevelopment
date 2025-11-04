package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import com.runanywhere.startup_hackathon20.data.api.RetrofitClient
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {
    
    companion object {
        lateinit var tokenManager: TokenManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize TokenManager and RetrofitClient
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)

        // Initialize SDK asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            initializeSDK()
        }
    }

    private suspend fun initializeSDK() {
        try {
            // Step 1: Initialize SDK
            RunAnywhere.initialize(
                context = this@MyApplication,
                apiKey = "dev",  // Any string works in dev mode
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()

            // Step 3: Register Models
            registerModels()

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()

            Log.i("MyApp", "SDK initialized successfully")

        } catch (e: Exception) {
            Log.e("MyApp", "SDK initialization failed: ${e.message}")
        }
    }

    private suspend fun registerModels() {
        // Medium-sized model - better quality (374 MB)
        addModelFromURL(
            url = "https://huggingface.co/Triangle104/Qwen2.5-0.5B-Instruct-Q6_K-GGUF/resolve/main/qwen2.5-0.5b-instruct-q6_k.gguf",
            name = "Qwen 2.5 0.5B Instruct Q6_K",
            type = "LLM"
        )

        addModelFromURL(
            url = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q6_k.gguf",
            name = "Qwen 2.5 1.5B Instruct Q6_K",
            type = "LLM"
        )
    }
}
