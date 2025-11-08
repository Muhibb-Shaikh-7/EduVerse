package com.example.eduverse.data.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage Service
 *
 * Handles file operations using Firebase Storage:
 * - Upload videos, PDFs, images, and documents
 * - Download files
 * - Delete files
 * - Generate public URLs
 */
@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage,
    private val context: Context
) {
    companion object {
        private const val COURSES_BUCKET = "courses"
        private const val LESSONS_BUCKET = "lessons"
        private const val PROFILES_BUCKET = "profiles"
        private const val ANNOUNCEMENTS_BUCKET = "announcements"
        private const val MATERIALS_BUCKET = "materials"
    }

    /**
     * Upload course thumbnail
     */
    suspend fun uploadCourseThumbnail(
        uri: Uri,
        courseId: String
    ): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "$COURSES_BUCKET/$courseId/thumbnail/$fileName"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            // Upload to Firebase Storage
            storageRef.putBytes(fileData, metadata).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload lesson video
     */
    suspend fun uploadLessonVideo(
        uri: Uri,
        courseId: String,
        lessonId: String,
        onProgress: ((Int) -> Unit)? = null
    ): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.mp4"
            val path = "$LESSONS_BUCKET/$courseId/$lessonId/video/$fileName"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build()

            // Upload with progress tracking
            val uploadTask = storageRef.putBytes(fileData, metadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress?.invoke(progress)
            }

            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload lesson document (PDF, etc.)
     */
    suspend fun uploadLessonDocument(
        uri: Uri,
        courseId: String,
        lessonId: String,
        fileName: String? = null
    ): Result<String> {
        return try {
            val name = fileName ?: "${UUID.randomUUID()}.pdf"
            val path = "$LESSONS_BUCKET/$courseId/$lessonId/documents/$name"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Get MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            // Upload to Firebase Storage
            storageRef.putBytes(fileData, metadata).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
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
    ): Result<String> {
        return try {
            val path = "$PROFILES_BUCKET/$userId/avatar.jpg"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            // Upload to Firebase Storage (overwrite if exists)
            storageRef.putBytes(fileData, metadata).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload announcement image
     */
    suspend fun uploadAnnouncementImage(
        uri: Uri,
        announcementId: String
    ): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "$ANNOUNCEMENTS_BUCKET/$announcementId/$fileName"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            // Upload to Firebase Storage
            storageRef.putBytes(fileData, metadata).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload generic material
     */
    suspend fun uploadMaterial(
        uri: Uri,
        userId: String,
        materialType: String,
        fileName: String? = null
    ): Result<MaterialUploadResult> {
        return try {
            val timestamp = System.currentTimeMillis()
            val extension = getFileExtension(uri)
            val name = fileName ?: "material_${timestamp}.$extension"
            val path = "$MATERIALS_BUCKET/$userId/$materialType/$name"
            val storageRef = storage.reference.child(path)

            // Read file data
            val fileData = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw Exception("Failed to read file")

            val fileSize = fileData.size.toLong()

            // Get MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()

            // Upload to Firebase Storage
            storageRef.putBytes(fileData, metadata).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(
                MaterialUploadResult(
                    publicUrl = downloadUrl,
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
     * Delete file from Firebase Storage
     */
    suspend fun deleteFile(storagePath: String): Result<Unit> {
        return try {
            val storageRef = storage.reference.child(storagePath)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get file metadata
     */
    suspend fun getFileMetadata(storagePath: String): Result<StorageMetadata> {
        return try {
            val storageRef = storage.reference.child(storagePath)
            val metadata = storageRef.metadata.await()
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to get file extension from URI
     */
    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("pdf") == true -> "pdf"
            mimeType?.contains("image") == true -> "jpg"
            mimeType?.contains("video") == true -> "mp4"
            else -> uri.toString().substringAfterLast(".", "unknown")
        }
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
