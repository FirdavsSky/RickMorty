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
        try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeysDao = db.remoteKeysDao()
                    val lastItem = state.lastItemOrNull()
                    val keys = lastItem?.let { remoteKeysDao.remoteKeysCharacterId(it.id) }
                    keys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            Log.d(TAG, "Loading page: $page, type: $loadType")

            val response = api.getCharacters(page, query, status, species, gender)


            val characters = response.results.map { dto ->
                Log.d(TAG, "characters $dto")
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

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    Log.d(TAG, "Clearing all characters and remote keys from DB")
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

                Log.d(TAG, "Inserted ${characters.size} characters and ${keys.size} remote keys into DB")
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading characters: ${e.message}", e)
            return MediatorResult.Error(e)
        }
    }
}
