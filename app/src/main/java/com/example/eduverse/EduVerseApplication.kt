package com.example.eduverse

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EduVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize SDK asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            initializeSDK()
        }
    }

    private suspend fun initializeSDK() {
        try {
            Log.i(TAG, "Initializing RunAnywhere SDK...")

            // Step 1: Initialize SDK
            RunAnywhere.initialize(
                context = this@EduVerseApplication,
                apiKey = "dev",  // Any string works in dev mode
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()
            Log.i(TAG, "LlamaCpp Service Provider registered")

            // Step 3: Register Models
            registerModels()

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()

            Log.i(TAG, "RunAnywhere SDK initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "SDK initialization failed: ${e.message}", e)
        }
    }

    private suspend fun registerModels() {
        try {
            // Smallest model - great for testing (119 MB)
            addModelFromURL(
                url = "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                name = "SmolLM2 360M Q8_0",
                type = "LLM"
            )
            Log.i(TAG, "Registered SmolLM2 360M model")

            // Medium-sized model - better quality (374 MB)
            addModelFromURL(
                url = "https://huggingface.co/Triangle104/Qwen2.5-0.5B-Instruct-Q6_K-GGUF/resolve/main/qwen2.5-0.5b-instruct-q6_k.gguf",
                name = "Qwen 2.5 0.5B Instruct Q6_K",
                type = "LLM"
            )
            Log.i(TAG, "Registered Qwen 2.5 0.5B model")

            // Larger model - best quality (815 MB)
            addModelFromURL(
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                name = "Llama 3.2 1B Instruct Q6_K",
                type = "LLM"
            )
            Log.i(TAG, "Registered Llama 3.2 1B model")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to register models: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "EduVerseApp"
    }
}
