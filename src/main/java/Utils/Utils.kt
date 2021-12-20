package Utils

import DAO.Filter


fun buildGenresCallback(genres: HashMap<String, Int>, isReset: Boolean): List<String> {
    val listGenres = mutableListOf<String>()
    for ((key, value) in genres) {
        listGenres.add(key)
    }
    val listCallback = mutableListOf<String>()
    if (!isReset) {
        for (i in 0 until listGenres.size) {
            listCallback.add("filter_genre_${listGenres[i]}")
        }
    } else {
        for (i in 0 until listGenres.size) {
            listCallback.add("filter_genre_${listGenres[i]}_reset")
        }
    }
    return listCallback
}

fun getGenreFromCallback(callback: String): String {
    var genre = ""
    if (!callback.contains("reset")) {
        genre = callback.substring(13, callback.length)
    } else {
        genre = callback.substring(13, callback.length)
        genre = genre.substring(0, genre.indexOf('_'))
    }
    return genre
}

fun getFilterMessage(genresStringList: String, ratingStringList: String, yearStringList: String): String {
    return "*Сейчас выбраны следующие параметры:*\n" +
            "*Жанр:* $genresStringList\n" +
            "*Рейтинг:* $ratingStringList\n" +
            "*Год:* $yearStringList\n"
}

fun parseGenre(genresStringList: String, genreMap: HashMap<String, Int>): MutableList<Int> {
    var genresIntValue = mutableListOf<Int>()
    var genreList = mutableListOf<String>()
    var tempGenreStrings = genresStringList
    var secondTempGenre = ""
    var count = 0
    while (tempGenreStrings != "") {
        if (tempGenreStrings.contains(',')) {
            secondTempGenre = tempGenreStrings.substring(count, tempGenreStrings.indexOf(','))
            tempGenreStrings =
                tempGenreStrings.substring(tempGenreStrings.indexOf(',') + 1, tempGenreStrings.length).trim(' ')
            genreList.add(secondTempGenre)
        } else {
            secondTempGenre = tempGenreStrings
            genreList.add(secondTempGenre)
            break
        }
    }
    for (i in 0 until genreList.size) {
        genresIntValue.add(genreMap.get(genreList[i])!!)
    }
    return genresIntValue
}

fun parseSpanNumbers(string: String, isFirstPart: Boolean): Int {
    if (isFirstPart) {
        return string.substring(0, string.indexOf('-')).toInt()
    } else {
        return string.substring(string.indexOf('-') + 1).toInt()
    }
}

fun parseGenreForRequest(genresList: MutableList<Int>): String {
    var genresForRequest = ""
    for (i in 0 until genresList.size) {
        genresForRequest += "${genresList[i]},"
    }
    return genresForRequest.substring(0, genresForRequest.length - 1)
}


fun buildSearchByFiltersRequest(filter: Filter): String {
    var genres = ""
    if (filter.genresList.size != 0) {
        genres = parseGenreForRequest(filter.genresList)
    }
    var resultRequest = "/api/v2.1/films/search-by-filters?"
    if (genres != "0") {
        resultRequest += "genre=$genres&"
    }
    resultRequest += "order=${filter.order}&type=${filter.type}"
    if (filter.ratingFrom != 0) {
        resultRequest += "&ratingFrom=${filter.ratingFrom}"
    }
    if (filter.ratingTo != 0) {
        resultRequest += "&ratingTo=${filter.ratingTo}"
    }
    if (filter.yearFrom != 0) {
        resultRequest += "&yearFrom=${filter.yearTo}"
    }
    if (filter.yearTo != 0) {
        resultRequest += "&yearTo=${filter.yearTo}"
    }
    resultRequest += "&page=${filter.page}"
    return resultRequest
}


