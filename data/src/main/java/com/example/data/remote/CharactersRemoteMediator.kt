package com.example.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.data.local.db.AppDatabase
import com.example.data.local.model.CharacterEntity
import com.example.data.remote.api.RickAndMortyApi


@OptIn(ExperimentalPagingApi::class, ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val api: RickAndMortyApi,
    private val db: AppDatabase,
    private val query: String?,
    private val status: String?,
    private val species: String?,
    private val gender: String?
) : RemoteMediator<Int, CharacterEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, CharacterEntity>): MediatorResult {
        try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastPage = state.pages.lastOrNull()?.nextKey ?: (state.pages.size + 1)
                    lastPage
                }
            }
            val response = api.getCharacters(page = page, name = query, status = status, species = species, gender = gender)
            val list = response.results.map { dto ->
                CharacterEntity(dto.id, dto.name, dto.status, dto.species, dto.gender, dto.image, System.currentTimeMillis())
            }
//            db.withTransaction {
//                if (loadType == LoadType.REFRESH) {
//                    db.characterDao().clearAll() // implement clearAll if needed
//                }
//                db.characterDao().insertAll(list)
//            }
            val end = response.info.next == null
            return MediatorResult.Success(endOfPaginationReached = end)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
