package com.example.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.local.db.AppDatabase
import com.example.data.mapper.CharacterMapper
import com.example.data.remote.CharactersRemoteMediator
import com.example.data.remote.api.RickAndMortyApi
import com.example.domain.model.CharacterModel
import com.example.domain.repository.CharactersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharactersRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: AppDatabase,
    private val mapper: CharacterMapper
) : CharactersRepository {


    @OptIn(ExperimentalPagingApi::class)
    override fun getCharactersPaged(
        query: String?,
        status: String?,
        species: String?,
        gender: String?
    ): Flow<PagingData<CharacterModel>> {

        val pagingSourceFactory = {
            db.characterDao().pagingSource(query, status, species, gender)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5
            ),
            remoteMediator = CharactersRemoteMediator(api, db, query, status, species, gender),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                try {
                    val domain = mapper.entityToDomain(entity)
                    domain
                } catch (e: Exception) {
                    mapper.entityToDomain(entity)
                }
            }
        }
    }

    override suspend fun getCharacterById(id: Int): CharacterModel? {

        return db.characterDao().getCharacter(id)?.let {
            val domain = mapper.entityToDomain(it)
            domain
        }
    }
}


