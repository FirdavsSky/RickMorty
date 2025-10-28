package com.example.data.remote

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

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {

        try {
            val page = when (loadType) {

                LoadType.REFRESH -> 1

                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)


                LoadType.APPEND -> {

                    var lastItem = state.lastItemOrNull()

                    if (lastItem == null) {
                        lastItem = db.characterDao().getLastCharacter()
                    }

                    if (lastItem == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val keys = db.remoteKeysDao().remoteKeysCharacterId(lastItem.id)
                    val nextKey = keys?.nextKey

                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }


            val response = api.getCharacters(page, query, status, species, gender)

            val characters = response.results.map { dto ->
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
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
