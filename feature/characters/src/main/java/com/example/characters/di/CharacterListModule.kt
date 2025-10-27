package com.example.characters.di

import com.example.domain.repository.CharactersRepository
import com.example.domain.usecase.GetCharactersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object CharacterListModule {

    // UseCases
    @Provides
    fun provideGetCharactersUseCase(repository: CharactersRepository): GetCharactersUseCase {
        return GetCharactersUseCase(repository)
    }
}