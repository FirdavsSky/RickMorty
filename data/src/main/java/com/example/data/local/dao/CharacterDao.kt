package com.example.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.model.CharacterEntity

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM characters
        WHERE (:query IS NULL OR name LIKE '%' || :query || '%')
        AND (:status IS NULL OR status = :status)
        AND (:species IS NULL OR species = :species)
        AND (:gender IS NULL OR gender = :gender)
        ORDER BY id
    """)

    fun pagingSource(
        query: String?,
        status: String?,
        species: String?,
        gender: String?
    ): PagingSource<Int, CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacter(id: Int): CharacterEntity?


    @Query("SELECT * FROM characters ORDER BY id DESC LIMIT 1")
    suspend fun getLastCharacter(): CharacterEntity?
}
