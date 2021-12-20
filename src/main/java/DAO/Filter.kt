package DAO

data class Filter(
    var genresList: MutableList<Int> = mutableListOf(0),
    var countriesList: MutableList<Int> = mutableListOf(0),
    var order: String = "RATING",
    var type: String = "FILM",
    var ratingFrom: Int = 0,
    var ratingTo: Int = 0,
    var yearFrom: Int = 0,
    var yearTo: Int = 0,
    var page: Int = 1
)
