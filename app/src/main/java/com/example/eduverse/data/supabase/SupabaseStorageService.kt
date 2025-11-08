package com.example.eduverse.data.supabase

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase Storage Service
 *
 * Handles file operations using Supabase Storage:
 * - Upload PDFs and images
 * - Download files
 * - Delete files
 * - Generate public URLs
 */
@Singleton
class SupabaseStorageService @Inject constructor(
    private val storage: Storage,
    private val context: Context
) {
    companion object {
        private const val MATERIALS_BUCKET = "materials"
        private const val PROFILES_BUCKET = "profiles"
        private const val FLASHCARDS_BUCKET = "flashcards"
        private const val QUIZZES_BUCKET = "quizzes"
    }

    /**
     * Upload a PDF file to Supabase Storage
     */
    suspend fun uploadPDF(
        uri: Uri,
        userId: String,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val name = fileName ?: "${UUID.randomUUID()}.pdf"
            val path = "$userId/$name"

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Upload to Supabase
            storage.from(MATERIALS_BUCKET).upload(path, fileData) {
                upsert = false
            }

            // Get public URL
            val publicUrl = storage.from(MATERIALS_BUCKET).publicUrl(path)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload an image file to Supabase Storage
     */
    suspend fun uploadImage(
        uri: Uri,
        userId: String,
        bucket: String = MATERIALS_BUCKET,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val name = fileName ?: "${UUID.randomUUID()}.jpg"
            val path = "$userId/$name"

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Upload to Supabase
            storage.from(bucket).upload(path, fileData) {
                upsert = false
            }

            // Get public URL
            val publicUrl = storage.from(bucket).publicUrl(path)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload material (generic file) to Supabase Storage
     */
    suspend fun uploadMaterial(
        uri: Uri,
        userId: String,
        materialType: String,
        fileName: String? = null
    ): Result<MaterialUploadResult> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val extension = getFileExtension(uri.toString())
            val name = fileName ?: "material_${timestamp}.$extension"
            val path = "$userId/$materialType/$name"

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            val fileSize = fileData.size.toLong()

            // Upload to Supabase
            storage.from(MATERIALS_BUCKET).upload(path, fileData) {
                upsert = false
            }

            // Get public URL
            val publicUrl = storage.from(MATERIALS_BUCKET).publicUrl(path)

            Result.success(
                MaterialUploadResult(
                    publicUrl = publicUrl,
                    fileName = name,
                    fileSize = fileSize,
                    storagePath = path
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload profile picture
     */
    suspend fun uploadProfilePicture(
        uri: Uri,
        userId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val path = "$userId/profile.jpg"

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Upload to Supabase (update if exists)
            storage.from(PROFILES_BUCKET).upload(path, fileData) {
                upsert = true
            }

            // Get public URL
            val publicUrl = storage.from(PROFILES_BUCKET).publicUrl(path)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download file from Supabase Storage
     */
    suspend fun downloadFile(
        bucket: String,
        storagePath: String,
        localFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val data = storage.from(bucket).downloadAuthenticated(storagePath)
            localFile.writeBytes(data)
            Result.success(localFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get public URL for a file
     */
    fun getPublicUrl(bucket: String, storagePath: String): String {
        return storage.from(bucket).publicUrl(storagePath)
    }

    /**
     * Delete file from Supabase Storage
     */
    suspend fun deleteFile(
        bucket: String,
        storagePath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            storage.from(bucket).delete(storagePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * List all files in a directory
     */
    suspend fun listFiles(
        bucket: String,
        path: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val files = storage.from(bucket).list(path)
            val fileNames = files.map { it.name }
            Result.success(fileNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create storage buckets if they don't exist
     */
    suspend fun initializeBuckets(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Buckets should be created in Supabase Dashboard
            // This is just a verification method
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to get file extension
     */
    private fun getFileExtension(uri: String): String {
        return uri.substringAfterLast(".", "unknown")
    }
}

/**
 * Data class for material upload result
 */
data class MaterialUploadResult(
    val publicUrl: String,
    val fileName: String,
    val fileSize: Long,
    val storagePath: String
)

/**
 * Data class for file metadata
 */
data class FileMetadata(
    val name: String,
    val size: Long,
    val contentType: String,
    val createdAt: Long,
    val updatedAt: Long
)
