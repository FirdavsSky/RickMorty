package com.example.domain.usecase

import com.example.domain.model.CharacterModel
import com.example.domain.repository.CharactersRepository
import javax.inject.Inject

class GetCharacterByIdUseCase  @Inject constructor(
    private val repository: CharactersRepository
) {
    suspend operator fun invoke(id: Int): CharacterModel? {
        return repository.getCharacterById(id)
    }
}