package com.example.characters.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.characters.presentation.intents.CharacterListIntent
import com.example.characters.presentation.states.CharacterListState
import com.example.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
): ViewModel() {

    private val _state = MutableStateFlow(CharacterListState())
    val state: StateFlow<CharacterListState> = _state.asStateFlow()

    private val intentChannel = Channel<CharacterListIntent>(Channel.Factory.UNLIMITED)
    val intents = intentChannel.receiveAsFlow()

    init {
        handleIntents()
        intentChannel.trySend(CharacterListIntent.Load)
    }

    fun submitIntent(intent: CharacterListIntent) = intentChannel.trySend(intent)

    private fun handleIntents() {

        viewModelScope.launch {

            intents.collect { intent ->
                when (intent) {
                    is CharacterListIntent.Load -> loadCharacters()
                    is CharacterListIntent.Search -> {
                        _state.update { it.copy(searchQuery = intent.query) }
                        loadCharacters()
                    }
                    is CharacterListIntent.ApplyFilter -> {
                        _state.update { it.copy(statusFilter = intent.status, speciesFilter = intent.species, genderFilter = intent.gender) }
                        loadCharacters()
                    }
                    is CharacterListIntent.Refresh -> {
                        loadCharacters(refresh = true)
                    }
                }
            }
        }
    }

    private var currentPagingJob: Job? = null

    private fun loadCharacters(refresh: Boolean = false) {
        currentPagingJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        currentPagingJob = viewModelScope.launch {
            val s = _state.value
            getCharactersUseCase(s.searchQuery, s.statusFilter, s.speciesFilter, s.genderFilter)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _state.update { it.copy(isLoading = false, pagingData = pagingData) }
                }
        }
    }
}