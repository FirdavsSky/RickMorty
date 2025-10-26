package com.example.characters.presentation.intents

sealed class CharacterListIntent {
    object Load : CharacterListIntent()
    data class Search(val query: String) : CharacterListIntent()
    data class ApplyFilter(val status: String?, val species: String?, val gender: String?) : CharacterListIntent()
    object Refresh: CharacterListIntent()
}