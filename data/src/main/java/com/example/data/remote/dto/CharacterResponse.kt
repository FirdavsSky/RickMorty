package com.example.data.remote.dto


data class CharacterResponse(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val origin: LocationRef?,
    val location: LocationRef?,
    val episode: List<String>
)

data class LocationRef(
    val name: String,
    val url: String
)

data class PageInfoResponse(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

data class CharacterListResponse(
    val info: PageInfoResponse,
    val results: List<CharacterResponse>
)