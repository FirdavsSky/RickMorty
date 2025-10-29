package com.example.details.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.details.presentation.intent.CharacterDetailIntent
import com.example.details.presentation.uiState.CharacterDetailState
import com.example.domain.usecase.GetCharacterByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val getCharacterByIdUseCase: GetCharacterByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CharacterDetailState>(CharacterDetailState.Idle)
    val state: StateFlow<CharacterDetailState> = _state

    fun handleIntent(intent: CharacterDetailIntent) {
        when (intent) {
            is CharacterDetailIntent.LoadCharacter -> loadCharacter(intent.id)
        }
    }

    private fun loadCharacter(id: Int) {
        viewModelScope.launch {
            _state.value = CharacterDetailState.Loading
            try {
                val result = getCharacterByIdUseCase(id)
                _state.value = CharacterDetailState.Success(result)
            } catch (e: Exception) {
                _state.value = CharacterDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
