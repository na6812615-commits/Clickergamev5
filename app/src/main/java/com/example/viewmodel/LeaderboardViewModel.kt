package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameStateEntity
import com.example.data.LeaderboardEntry
import com.example.data.LeaderboardRepository
import com.example.data.LeaderboardSortBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val sortBy: LeaderboardSortBy = LeaderboardSortBy.CLICKS,
    val errorMessage: String? = null,
    val syncStatusMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModel(
    private val repository: LeaderboardRepository,
    val currentUserId: String,
    defaultDisplayName: String?
) : ViewModel() {

    private val sortByFlow = MutableStateFlow(LeaderboardSortBy.CLICKS)
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    val resolvedDisplayName: String = LeaderboardRepository.sanitizeDisplayName(defaultDisplayName)

    init {
        viewModelScope.launch {
            sortByFlow
                .flatMapLatest { sort ->
                    _uiState.update { it.copy(isLoading = true, sortBy = sort, errorMessage = null) }
                    repository.observeTopPlayers(sortBy = sort)
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                                ?: "Không thể tải bảng xếp hạng từ Firestore."
                        )
                    }
                }
                .collect { entries ->
                    _uiState.update {
                        it.copy(
                            entries = entries,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun setSortBy(sortBy: LeaderboardSortBy) {
        if (sortByFlow.value != sortBy) {
            sortByFlow.value = sortBy
        }
    }

    fun syncScore(game: GameStateEntity, customName: String? = null) {
        if (_uiState.value.isSyncing) return
        val effectiveName = LeaderboardRepository.sanitizeDisplayName(
            customName?.takeIf { it.isNotBlank() } ?: resolvedDisplayName
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = null, errorMessage = null) }
            val result = repository.submitScore(effectiveName, game)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = "Đã đồng bộ điểm lên Bảng Xếp Hạng!"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = err.localizedMessage
                                ?: "Không thể đồng bộ điểm lên Firestore."
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(syncStatusMessage = null, errorMessage = null) }
    }

    companion object {
        fun factory(
            repository: LeaderboardRepository,
            currentUserId: String,
            defaultDisplayName: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LeaderboardViewModel(
                    repository = repository,
                    currentUserId = currentUserId,
                    defaultDisplayName = defaultDisplayName
                ) as T
            }
        }
    }
}
