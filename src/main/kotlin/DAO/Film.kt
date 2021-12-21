package DAO

import kotlinx.serialization.Serializable

@Serializable
data class Film(
    var kinopoiskId: Int = 0,
    var nameRu: String = "",
    var nameOriginal: String = "",
    @Serializable(with = GenreListSerializer::class)
    var genres: List<Genre> = listOf(Genre()),
    var ratingKinopoisk: Double = 0.0,
    var filmLength: Int = 0,
    @Serializable(with = CountryListSerializer::class)
    var countries: List<Country> = listOf(Country()),
    var webUrl: String = "",
    var posterUrl: String = ""
)
