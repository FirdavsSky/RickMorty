package com.example.data.remote

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.data.local.dao.RemoteKeys
import com.example.data.local.db.AppDatabase
import com.example.data.local.model.CharacterEntity
import com.example.data.remote.api.RickAndMortyApi
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: AppDatabase,
    private val query: String?,
    private val status: String?,
    private val species: String?,
    private val gender: String?
) : RemoteMediator<Int, CharacterEntity>() {

    private val TAG = "RickMorty.RemoteMediator"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {
        Log.d(TAG, "=== load called ===")
        Log.d(TAG, "loadType = $loadType")
        Log.d(TAG, "state.pages.size = ${state.pages.size}")
        Log.d(TAG, "state.anchorPosition = ${state.anchorPosition}")
        Log.d(TAG, "state.lastItemOrNull() = ${state.lastItemOrNull()}")

        try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    Log.d(TAG, "Determined page = 1 for REFRESH")
                    1
                }
                LoadType.PREPEND -> {
                    Log.d(TAG, "PREPEND → endOfPaginationReached = true")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // Сначала пробуем взять последний элемент из state
                    var lastItem = state.lastItemOrNull()

                    // Если state.lastItemOrNull() null (первый запуск), достаем последний элемент из БД
                    if (lastItem == null) {
                        lastItem = db.characterDao().getLastCharacter()
                        Log.d(TAG, "lastItem after fallback = $lastItem")
                    }

                    if (lastItem == null) {
                        Log.d(TAG, "RemoteKeys for lastItem: null → endOfPaginationReached = true")
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val keys = db.remoteKeysDao().remoteKeysCharacterId(lastItem.id)
                    val nextKey = keys?.nextKey
                    Log.d(TAG, "RemoteKeys for lastItem: $keys, nextKey = $nextKey")

                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            Log.d(TAG, "Loading page: $page")

            val response = api.getCharacters(page, query, status, species, gender)
            val characters = response.results.map { dto ->
                Log.d(TAG, "Fetched character: $dto")
                CharacterEntity(
                    id = dto.id,
                    name = dto.name,
                    status = dto.status,
                    species = dto.species,
                    gender = dto.gender,
                    image = dto.image,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            val endOfPaginationReached = response.info.next == null
            Log.d(TAG, "endOfPaginationReached = $endOfPaginationReached")

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    Log.d(TAG, "Clearing DB before REFRESH")
                    db.characterDao().clearAll()
                    db.remoteKeysDao().clearRemoteKeys()
                }

                db.characterDao().insertAll(characters)

                val keys = characters.map {
                    RemoteKeys(
                        characterId = it.id,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (endOfPaginationReached) null else page + 1
                    )
                }
                db.remoteKeysDao().insertAll(keys)
                Log.d(TAG, "Inserted ${characters.size} characters and ${keys.size} remote keys")
            }

            Log.d(TAG, "=== load finished successfully ===")
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading characters: ${e.message}", e)
            return MediatorResult.Error(e)
        }
    }
}
