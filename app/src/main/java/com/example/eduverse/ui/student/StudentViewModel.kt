package com.example.eduverse.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduverse.data.model.*
import com.example.eduverse.data.repository.AuthRepository
import com.example.eduverse.data.repository.FlashcardRepository
import com.example.eduverse.data.repository.QuizRepository
import com.example.eduverse.data.repository.StudentProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studentProgressRepository: StudentProgressRepository,
    private val flashcardRepository: FlashcardRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentUiState>(StudentUiState.Loading)
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private val _studentProgress = MutableStateFlow<StudentProgress?>(null)
    val studentProgress: StateFlow<StudentProgress?> = _studentProgress.asStateFlow()

    private val _flashcardSets = MutableStateFlow<List<FlashcardSet>>(emptyList())
    val flashcardSets: StateFlow<List<FlashcardSet>> = _flashcardSets.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _newBadges = MutableStateFlow<List<Badge>>(emptyList())
    val newBadges: StateFlow<List<Badge>> = _newBadges.asStateFlow()

    init {
        loadStudentData()
    }

    private fun loadStudentData() {
        viewModelScope.launch {
            _uiState.value = StudentUiState.Loading

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = StudentUiState.Error("User not logged in")
                return@launch
            }

            try {
                // Load student progress
                studentProgressRepository.getStudentProgress(currentUser.uid)
                    .onSuccess { progress ->
                        _studentProgress.value = progress

                        // Observe progress changes
                        studentProgressRepository.observeStudentProgress(currentUser.uid)
                            .collect { updatedProgress ->
                                _studentProgress.value = updatedProgress
                            }
                    }
                    .onFailure { error ->
                        _uiState.value =
                            StudentUiState.Error(error.message ?: "Failed to load progress")
                    }

                // Load flashcard sets
                flashcardRepository.getAllFlashcardSets()
                    .onSuccess { sets ->
                        _flashcardSets.value = sets
                    }

                // Load quizzes
                quizRepository.getAllQuizzes()
                    .onSuccess { quizList ->
                        _quizzes.value = quizList
                    }

                _uiState.value = StudentUiState.Success
            } catch (e: Exception) {
                _uiState.value = StudentUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun completeQuiz(quiz: Quiz, score: Int, totalQuestions: Int, answers: List<QuizAnswer>) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            val oldProgress = _studentProgress.value

            studentProgressRepository.completeQuiz(
                userId = currentUser.uid,
                quiz = quiz,
                score = score,
                totalQuestions = totalQuestions,
                answers = answers
            ).onSuccess { updatedProgress ->
                _studentProgress.value = updatedProgress

                // Check for new badges
                val oldBadgeIds = oldProgress?.badges?.map { it.id } ?: emptyList()
                val newlyUnlockedBadges = updatedProgress.badges.filter { it.id !in oldBadgeIds }
                if (newlyUnlockedBadges.isNotEmpty()) {
                    _newBadges.value = newlyUnlockedBadges
                }
            }.onFailure { error ->
                _uiState.value = StudentUiState.Error(error.message ?: "Failed to save quiz result")
            }
        }
    }

    fun studyFlashcardSet(flashcardSetId: String) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser() ?: return@launch

            studentProgressRepository.studyFlashcardSet(currentUser.uid, flashcardSetId)
                .onSuccess { updatedProgress ->
                    _studentProgress.value = updatedProgress
                }
        }
    }

    fun clearNewBadges() {
        _newBadges.value = emptyList()
    }

    fun refreshData() {
        loadStudentData()
    }
}

sealed class StudentUiState {
    object Loading : StudentUiState()
    object Success : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}
