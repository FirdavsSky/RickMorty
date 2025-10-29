package com.example.details.presentation.intent

sealed class CharacterDetailIntent {

    data class LoadCharacter(val id: Int) : CharacterDetailIntent()
}