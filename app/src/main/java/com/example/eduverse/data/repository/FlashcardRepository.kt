package com.example.eduverse.data.repository

import android.content.Context
import com.example.eduverse.data.model.Flashcard
import com.example.eduverse.data.model.FlashcardSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Mock storage - replace with Firestore later
    private val flashcardSets = mutableMapOf<String, FlashcardSet>()
    private val _flashcardSetsFlow = MutableStateFlow<List<FlashcardSet>>(emptyList())

    init {
        // Add some mock flashcard sets
        addMockFlashcardSets()
    }

    private fun addMockFlashcardSets() {
        val set1 = FlashcardSet(
            id = UUID.randomUUID().toString(),
            title = "Machine Learning Basics",
            description = "Essential ML concepts and terminology",
            teacherId = "teacher1",
            teacherName = "Dr. Smith",
            subject = "Computer Science",
            cards = listOf(
                Flashcard(
                    id = "1",
                    front = "What is Machine Learning?",
                    back = "A subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed"
                ),
                Flashcard(
                    id = "2",
                    front = "What are the main types of Machine Learning?",
                    back = "Supervised Learning, Unsupervised Learning, and Reinforcement Learning"
                ),
                Flashcard(
                    id = "3",
                    front = "What is Supervised Learning?",
                    back = "Learning from labeled training data to make predictions on new, unseen data"
                ),
                Flashcard(
                    id = "4",
                    front = "What are common applications of ML?",
                    back = "Image recognition, natural language processing, recommendation systems, and predictive analytics"
                ),
                Flashcard(
                    id = "5",
                    front = "What is overfitting?",
                    back = "When a model learns the training data too well, including noise, and performs poorly on new data"
                )
            ),
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        val set2 = FlashcardSet(
            id = UUID.randomUUID().toString(),
            title = "Data Structures & Algorithms",
            description = "Core data structures every programmer should know",
            teacherId = "teacher1",
            teacherName = "Prof. Johnson",
            subject = "Computer Science",
            cards = listOf(
                Flashcard(
                    id = "1",
                    front = "What is an Array?",
                    back = "A collection of elements stored in contiguous memory locations, accessed by index"
                ),
                Flashcard(
                    id = "2",
                    front = "What is a Stack?",
                    back = "A LIFO (Last In First Out) data structure where elements are added and removed from the same end"
                ),
                Flashcard(
                    id = "3",
                    front = "What is a Queue?",
                    back = "A FIFO (First In First Out) data structure where elements are added at the rear and removed from the front"
                ),
                Flashcard(
                    id = "4",
                    front = "What is a Binary Tree?",
                    back = "A hierarchical data structure where each node has at most two children (left and right)"
                ),
                Flashcard(
                    id = "5",
                    front = "What is Big O notation?",
                    back = "A mathematical notation that describes the limiting behavior of an algorithm's time or space complexity"
                )
            ),
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        val set3 = FlashcardSet(
            id = UUID.randomUUID().toString(),
            title = "Python Programming",
            description = "Python language fundamentals",
            teacherId = "teacher2",
            teacherName = "Ms. Davis",
            subject = "Programming",
            cards = listOf(
                Flashcard(
                    id = "1",
                    front = "What is Python?",
                    back = "A high-level, interpreted programming language known for its simplicity and readability"
                ),
                Flashcard(
                    id = "2",
                    front = "What are Python decorators?",
                    back = "Functions that modify the behavior of other functions or classes"
                ),
                Flashcard(
                    id = "3",
                    front = "What is a list comprehension?",
                    back = "A concise way to create lists using a single line of code with optional filtering"
                ),
                Flashcard(
                    id = "4",
                    front = "What is the difference between a list and a tuple?",
                    back = "Lists are mutable (can be changed), while tuples are immutable (cannot be changed after creation)"
                )
            ),
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        flashcardSets[set1.id] = set1
        flashcardSets[set2.id] = set2
        flashcardSets[set3.id] = set3

        _flashcardSetsFlow.value = flashcardSets.values.toList()
    }

    suspend fun getAllFlashcardSets(): Result<List<FlashcardSet>> {
        return try {
            Result.success(flashcardSets.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeFlashcardSets(): Flow<List<FlashcardSet>> {
        return _flashcardSetsFlow.asStateFlow()
    }

    suspend fun getFlashcardSet(id: String): Result<FlashcardSet> {
        return try {
            val set = flashcardSets[id] ?: throw Exception("Flashcard set not found")
            Result.success(set)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFlashcardSet(flashcardSet: FlashcardSet): Result<String> {
        return try {
            val id = flashcardSet.id.ifEmpty { UUID.randomUUID().toString() }
            val setWithId = flashcardSet.copy(
                id = id,
                createdAt = System.currentTimeMillis()
            )
            flashcardSets[id] = setWithId
            _flashcardSetsFlow.value = flashcardSets.values.toList()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFlashcardSet(flashcardSet: FlashcardSet): Result<Unit> {
        return try {
            if (flashcardSet.id.isEmpty()) {
                throw Exception("Flashcard set ID is required")
            }
            flashcardSets[flashcardSet.id] = flashcardSet
            _flashcardSetsFlow.value = flashcardSets.values.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFlashcardSet(id: String): Result<Unit> {
        return try {
            flashcardSets.remove(id)
            _flashcardSetsFlow.value = flashcardSets.values.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: Replace with Firestore implementation
    /*
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun getAllFlashcardSets(): Result<List<FlashcardSet>> {
        return try {
            val snapshot = firestore.collection("flashcard_sets")
                .whereEqualTo("isPublic", true)
                .get()
                .await()
            
            val sets = snapshot.documents.mapNotNull { 
                it.toObject(FlashcardSet::class.java) 
            }
            
            Result.success(sets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}
