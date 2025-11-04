package com.runanywhere.startup_hackathon20.data.api

import android.util.Log
import com.runanywhere.startup_hackathon20.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // TODO: Replace with your backend URL
    private const val BASE_URL = "http://192.168.31.130:3001/api/v1/" // Android emulator localhost
    // For physical device, use: "http://YOUR_LOCAL_IP:3000/api/v1/"
    // For production: "https://your-backend-url.com/api/v1/"
    
    private var tokenManager: TokenManager? = null
    
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
        Log.d("RetrofitClient", "Initialized with base URL: $BASE_URL")
    }
    
    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager?.getAccessToken()
        val request = chain.request().newBuilder()

        Log.d("RetrofitClient", "Request URL: ${chain.request().url}")

        if (token != null) {
            Log.d("RetrofitClient", "Adding auth header with token: ${token.substring(0, 10)}...")
            request.addHeader("Authorization", "Bearer $token")
        } else {
            Log.d("RetrofitClient", "No auth token available")
        }

        val finalRequest = request.build()
        val response = chain.proceed(finalRequest)

        Log.d("RetrofitClient", "Response code: ${response.code}")

        response
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

