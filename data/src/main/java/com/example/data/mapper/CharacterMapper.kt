package com.example.data.mapper

import android.util.Log
import com.example.data.local.model.CharacterEntity
import com.example.data.remote.dto.CharacterResponse
import com.example.domain.model.CharacterModel
import javax.inject.Inject


class CharacterMapper @Inject constructor() {
    fun responseToEntity(response: CharacterResponse): CharacterEntity {
        return CharacterEntity(
            id = response.id,
            name = response.name,
            status = response.status,
            species = response.species,
            gender = response.gender,
            image = response.image,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun entityToDomain(entity: CharacterEntity): CharacterModel {
        Log.d("entityToDomain", "entityToDomain: $entity")
        return com.example.domain.model.CharacterModel(
            id = entity.id,
            name = entity.name,
            status = entity.status,
            species = entity.species,
            gender = entity.gender,
            image = entity.image
        )
    }
}