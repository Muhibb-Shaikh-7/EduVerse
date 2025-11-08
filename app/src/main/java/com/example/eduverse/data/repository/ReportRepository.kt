package com.example.eduverse.data.repository

import com.example.eduverse.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val REPORTS_COLLECTION = "reports"
    }

    suspend fun createReport(report: Report): Result<String> {
        return try {
            val docRef = firestore.collection(REPORTS_COLLECTION).document()
            val reportWithId = report.copy(id = docRef.id)
            docRef.set(reportWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReport(id: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(REPORTS_COLLECTION).document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveReport(
        id: String,
        resolvedBy: String,
        resolvedByName: String,
        resolution: String,
        actionTaken: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to ReportStatus.RESOLVED.name,
                "resolvedBy" to resolvedBy,
                "resolvedByName" to resolvedByName,
                "resolvedAt" to System.currentTimeMillis(),
                "resolution" to resolution,
                "actionTaken" to actionTaken
            )
            updateReport(id, updates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReport(id: String): Result<Report?> {
        return try {
            val doc = firestore.collection(REPORTS_COLLECTION).document(id).get().await()
            Result.success(doc.toReport())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection(REPORTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            Result.success(snapshot.documents.mapNotNull { it.toReport() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpenReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection(REPORTS_COLLECTION)
                .whereEqualTo("status", ReportStatus.OPEN.name)
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            Result.success(snapshot.documents.mapNotNull { it.toReport() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeReports(): Flow<List<Report>> = callbackFlow {
        val listener = firestore.collection(REPORTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toReport() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toReport(): Report? {
    return try {
        Report(
            id = id,
            type = ReportType.fromString(getString("type") ?: "OTHER"),
            entityType = ReportEntityType.fromString(getString("entityType") ?: "COURSE"),
            entityId = getString("entityId") ?: "",
            entityTitle = getString("entityTitle") ?: "",
            reason = getString("reason") ?: "",
            description = getString("description") ?: "",
            reportedBy = getString("reportedBy") ?: "",
            reportedByName = getString("reportedByName") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            status = ReportStatus.fromString(getString("status") ?: "OPEN"),
            resolvedBy = getString("resolvedBy") ?: "",
            resolvedByName = getString("resolvedByName") ?: "",
            resolvedAt = getLong("resolvedAt") ?: 0L,
            resolution = getString("resolution") ?: "",
            actionTaken = getString("actionTaken") ?: ""
        )
    } catch (e: Exception) {
        null
    }
}

private fun Report.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "type" to type.name,
    "entityType" to entityType.name,
    "entityId" to entityId,
    "entityTitle" to entityTitle,
    "reason" to reason,
    "description" to description,
    "reportedBy" to reportedBy,
    "reportedByName" to reportedByName,
    "createdAt" to createdAt,
    "status" to status.name,
    "resolvedBy" to resolvedBy,
    "resolvedByName" to resolvedByName,
    "resolvedAt" to resolvedAt,
    "resolution" to resolution,
    "actionTaken" to actionTaken
)
