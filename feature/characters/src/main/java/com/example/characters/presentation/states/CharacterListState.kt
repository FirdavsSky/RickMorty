package com.example.characters.presentation.states

import androidx.paging.PagingData
import com.example.domain.model.CharacterModel

data class CharacterListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String? = null,
    val statusFilter: String? = null,
    val speciesFilter: String? = null,
    val genderFilter: String? = null,
    val pagingData: PagingData<CharacterModel> = PagingData.empty()
)