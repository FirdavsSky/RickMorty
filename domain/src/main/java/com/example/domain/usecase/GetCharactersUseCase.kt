package com.example.domain.usecase

import androidx.paging.PagingData
import com.example.domain.model.CharacterModel
import com.example.domain.repository.CharactersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: CharactersRepository
) {
    private val TAG = "RickMorty.UseCase"

    operator fun invoke(
        query: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ): Flow<PagingData<CharacterModel>> {
        return repository.getCharactersPaged(query, status, species, gender)
    }
}
