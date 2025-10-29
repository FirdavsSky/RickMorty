package com.example.details.di

import com.example.domain.repository.CharactersRepository
import com.example.domain.usecase.GetCharacterByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CharacterDetailModule {

    @Provides
    fun provideGetCharacterByIdUseCase(repository: CharactersRepository): GetCharacterByIdUseCase {
        return GetCharacterByIdUseCase(repository)
    }
}