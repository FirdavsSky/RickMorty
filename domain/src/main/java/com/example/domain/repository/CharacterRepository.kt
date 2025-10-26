package com.example.domain.repository

import androidx.paging.PagingData
import com.example.domain.model.CharacterModel
import kotlinx.coroutines.flow.Flow

interface CharactersRepository {

    fun getCharactersPaged(query: String?, status: String?, species: String?, gender: String?): Flow<PagingData<CharacterModel>>

    suspend fun getCharacterById(id: Int): CharacterModel?
}