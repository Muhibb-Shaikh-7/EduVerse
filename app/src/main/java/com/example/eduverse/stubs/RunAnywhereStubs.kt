package com.example.eduverse.stubs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * TEMPORARY STUBS - These will be replaced by actual SDK classes after Gradle sync
 *
 * These stubs allow the code to compile before the RunAnywhere SDK is downloaded from JitPack.
 * Once you sync Gradle, these imports will be replaced by the real SDK classes.
 */

// Stub for model information
data class ModelInfoStub(
    val id: String,
    val name: String,
    val downloadProgress: Float = 0f
)

// Stub for RunAnywhere SDK
object RunAnywhereStub {
    suspend fun downloadModel(modelId: String): Flow<Float> {
        return flowOf(0f)
    }

    suspend fun loadModel(modelId: String): Boolean {
        return false
    }

    fun generateStream(prompt: String): Flow<String> {
        return flowOf("")
    }
}

// Stub extension function
suspend fun listAvailableModelsStub(): List<ModelInfoStub> {
    return emptyList()
}
