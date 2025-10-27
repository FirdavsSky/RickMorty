package com.example.details.presentation.uiState

import com.example.domain.model.CharacterModel

sealed class CharacterDetailState {
    object Idle : CharacterDetailState()
    object Loading : CharacterDetailState()
    data class Success(val character: CharacterModel?) : CharacterDetailState()
    data class Error(val message: String) : CharacterDetailState()
}