package com.example.eduverse.data.repository

import android.content.Context
import com.example.eduverse.data.model.Quiz
import com.example.eduverse.data.model.QuizQuestion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Mock storage - replace with Firestore later
    private val quizzes = mutableMapOf<String, Quiz>()
    private val _quizzesFlow = MutableStateFlow<List<Quiz>>(emptyList())

    init {
        // Add some mock quizzes
        addMockQuizzes()
    }

    private fun addMockQuizzes() {
        val quiz1 = Quiz(
            id = UUID.randomUUID().toString(),
            title = "Machine Learning Fundamentals",
            description = "Test your knowledge of ML basics",
            teacherId = "teacher1",
            teacherName = "Dr. Smith",
            subject = "Computer Science",
            questions = listOf(
                QuizQuestion(
                    id = "1",
                    question = "What does AI stand for?",
                    options = listOf(
                        "Artificial Intelligence",
                        "Automated Internet",
                        "Advanced Integration",
                        "None of the above"
                    ),
                    correctAnswer = 0,
                    explanation = "AI stands for Artificial Intelligence, the simulation of human intelligence by machines."
                ),
                QuizQuestion(
                    id = "2",
                    question = "Which is a type of Machine Learning?",
                    options = listOf(
                        "Manual Learning",
                        "Supervised Learning",
                        "Random Learning",
                        "Fixed Learning"
                    ),
                    correctAnswer = 1,
                    explanation = "Supervised Learning is one of the main types of machine learning, where models learn from labeled data."
                ),
                QuizQuestion(
                    id = "3",
                    question = "What is the purpose of a training dataset?",
                    options = listOf(
                        "To test the model",
                        "To teach the model patterns",
                        "To deploy the model",
                        "To delete the model"
                    ),
                    correctAnswer = 1,
                    explanation = "A training dataset is used to teach the model patterns and relationships in the data."
                ),
                QuizQuestion(
                    id = "4",
                    question = "What is overfitting?",
                    options = listOf(
                        "Model performs well on all data",
                        "Model is too simple",
                        "Model learns training data too well",
                        "Model cannot learn"
                    ),
                    correctAnswer = 2,
                    explanation = "Overfitting occurs when a model learns the training data too well, including noise, reducing its ability to generalize."
                ),
                QuizQuestion(
                    id = "5",
                    question = "Which algorithm is used for classification?",
                    options = listOf(
                        "Linear Regression",
                        "Decision Tree",
                        "K-Means",
                        "PCA"
                    ),
                    correctAnswer = 1,
                    explanation = "Decision Tree is a popular algorithm for classification tasks, dividing data into branches based on features."
                )
            ),
            xpReward = 50,
            timeLimit = 300, // 5 minutes
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        val quiz2 = Quiz(
            id = UUID.randomUUID().toString(),
            title = "Data Structures Quiz",
            description = "Test your understanding of basic data structures",
            teacherId = "teacher1",
            teacherName = "Prof. Johnson",
            subject = "Computer Science",
            questions = listOf(
                QuizQuestion(
                    id = "1",
                    question = "Which data structure uses LIFO principle?",
                    options = listOf(
                        "Queue",
                        "Stack",
                        "Array",
                        "Linked List"
                    ),
                    correctAnswer = 1,
                    explanation = "A Stack follows the Last In First Out (LIFO) principle, where the last element added is the first to be removed."
                ),
                QuizQuestion(
                    id = "2",
                    question = "What is the time complexity of accessing an array element by index?",
                    options = listOf(
                        "O(n)",
                        "O(log n)",
                        "O(1)",
                        "O(n²)"
                    ),
                    correctAnswer = 2,
                    explanation = "Array access by index is O(1) constant time because arrays use direct memory addressing."
                ),
                QuizQuestion(
                    id = "3",
                    question = "Which data structure is best for implementing a priority queue?",
                    options = listOf(
                        "Array",
                        "Linked List",
                        "Heap",
                        "Stack"
                    ),
                    correctAnswer = 2,
                    explanation = "A Heap is the most efficient data structure for implementing a priority queue with O(log n) operations."
                ),
                QuizQuestion(
                    id = "4",
                    question = "What is a binary tree?",
                    options = listOf(
                        "A tree with two nodes",
                        "A tree where each node has at most two children",
                        "A tree with binary values",
                        "A tree with two levels"
                    ),
                    correctAnswer = 1,
                    explanation = "A binary tree is a hierarchical structure where each node has at most two children: left and right."
                ),
                QuizQuestion(
                    id = "5",
                    question = "Which operation is NOT efficient in a linked list?",
                    options = listOf(
                        "Insertion at beginning",
                        "Deletion at beginning",
                        "Random access by index",
                        "Sequential traversal"
                    ),
                    correctAnswer = 2,
                    explanation = "Random access by index is O(n) in a linked list because you must traverse from the head to reach the desired position."
                ),
                QuizQuestion(
                    id = "6",
                    question = "What is the main advantage of a hash table?",
                    options = listOf(
                        "Sorted data",
                        "Fast average-case lookup",
                        "Low memory usage",
                        "Sequential access"
                    ),
                    correctAnswer = 1,
                    explanation = "Hash tables provide O(1) average-case time complexity for insert, delete, and search operations."
                ),
                QuizQuestion(
                    id = "7",
                    question = "Which traversal visits the root node first?",
                    options = listOf(
                        "Inorder",
                        "Preorder",
                        "Postorder",
                        "Level-order"
                    ),
                    correctAnswer = 1,
                    explanation = "Preorder traversal visits the root node first, then the left subtree, and finally the right subtree."
                ),
                QuizQuestion(
                    id = "8",
                    question = "What is the space complexity of recursion?",
                    options = listOf(
                        "O(1)",
                        "O(log n)",
                        "O(n)",
                        "Depends on recursion depth"
                    ),
                    correctAnswer = 3,
                    explanation = "Recursion space complexity depends on the maximum recursion depth due to call stack usage."
                ),
                QuizQuestion(
                    id = "9",
                    question = "Which sorting algorithm has O(n log n) average complexity?",
                    options = listOf(
                        "Bubble Sort",
                        "Selection Sort",
                        "Merge Sort",
                        "Insertion Sort"
                    ),
                    correctAnswer = 2,
                    explanation = "Merge Sort has O(n log n) time complexity in all cases (best, average, and worst) and is a stable sorting algorithm."
                ),
                QuizQuestion(
                    id = "10",
                    question = "What is a circular linked list?",
                    options = listOf(
                        "A list with circular nodes",
                        "A list where the last node points to the first",
                        "A list stored in circular memory",
                        "A list with circular values"
                    ),
                    correctAnswer = 1,
                    explanation = "In a circular linked list, the last node points back to the first node, forming a circle."
                )
            ),
            xpReward = 100,
            timeLimit = 600, // 10 minutes
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        val quiz3 = Quiz(
            id = UUID.randomUUID().toString(),
            title = "Python Basics",
            description = "Quick quiz on Python fundamentals",
            teacherId = "teacher2",
            teacherName = "Ms. Davis",
            subject = "Programming",
            questions = listOf(
                QuizQuestion(
                    id = "1",
                    question = "Which of these is a mutable data type in Python?",
                    options = listOf(
                        "Tuple",
                        "String",
                        "List",
                        "Integer"
                    ),
                    correctAnswer = 2,
                    explanation = "Lists are mutable in Python, meaning their contents can be changed after creation."
                ),
                QuizQuestion(
                    id = "2",
                    question = "What keyword is used to define a function?",
                    options = listOf(
                        "function",
                        "def",
                        "func",
                        "define"
                    ),
                    correctAnswer = 1,
                    explanation = "The 'def' keyword is used to define functions in Python."
                ),
                QuizQuestion(
                    id = "3",
                    question = "How do you create a dictionary in Python?",
                    options = listOf(
                        "[]",
                        "{}",
                        "()",
                        "<>"
                    ),
                    correctAnswer = 1,
                    explanation = "Curly braces {} are used to create dictionaries in Python, with key-value pairs."
                )
            ),
            xpReward = 30,
            timeLimit = 180, // 3 minutes
            createdAt = System.currentTimeMillis(),
            isPublic = true
        )

        quizzes[quiz1.id] = quiz1
        quizzes[quiz2.id] = quiz2
        quizzes[quiz3.id] = quiz3

        _quizzesFlow.value = quizzes.values.toList()
    }

    suspend fun getAllQuizzes(): Result<List<Quiz>> {
        return try {
            Result.success(quizzes.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeQuizzes(): Flow<List<Quiz>> {
        return _quizzesFlow.asStateFlow()
    }

    suspend fun getQuiz(id: String): Result<Quiz> {
        return try {
            val quiz = quizzes[id] ?: throw Exception("Quiz not found")
            Result.success(quiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createQuiz(quiz: Quiz): Result<String> {
        return try {
            val id = quiz.id.ifEmpty { UUID.randomUUID().toString() }
            val quizWithId = quiz.copy(
                id = id,
                createdAt = System.currentTimeMillis()
            )
            quizzes[id] = quizWithId
            _quizzesFlow.value = quizzes.values.toList()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuiz(quiz: Quiz): Result<Unit> {
        return try {
            if (quiz.id.isEmpty()) {
                throw Exception("Quiz ID is required")
            }
            quizzes[quiz.id] = quiz
            _quizzesFlow.value = quizzes.values.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuiz(id: String): Result<Unit> {
        return try {
            quizzes.remove(id)
            _quizzesFlow.value = quizzes.values.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: Replace with Firestore implementation
    /*
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun getAllQuizzes(): Result<List<Quiz>> {
        return try {
            val snapshot = firestore.collection("quizzes")
                .whereEqualTo("isPublic", true)
                .get()
                .await()
            
            val quizList = snapshot.documents.mapNotNull { 
                it.toObject(Quiz::class.java) 
            }
            
            Result.success(quizList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}
