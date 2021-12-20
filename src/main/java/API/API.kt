package API

import DAO.Film
import DAO.Filter
import Utils.buildSearchByFiltersRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.collections.HashMap

class API() {
    private val mainUrl = "https://kinopoiskapiunofficial.tech"
    private var API_KEY = ""
    private var genresMap = HashMap<String, Int>()
    private var genreAndCountryMap = HashMap<String, Int>()
    private val countriesMap = HashMap<String, Int>()
    var pagesCount = 1

    init {
        readApiKey("src/main/resources/API-KEY.txt")
        fillGenreAndCountryMap()
    }


    fun getInfoAboutFilmById(id: Int): String {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$mainUrl/api/v2.2/films/$id"))
            .GET()
            .header("X-API-KEY", API_KEY)
            .header("Content-Type", "application/json")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val tempFormat = Json { ignoreUnknownKeys = true }
        val format = Json(tempFormat) { coerceInputValues = true }
        val film: Film = format.decodeFromString(response.body())
        return parseInfoAboutFilm(film)
    }

    fun getFilmByFilters(filter: Filter): MutableList<Int> {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$mainUrl${buildSearchByFiltersRequest(filter)}"))
            .GET()
            .header("X-API-KEY", API_KEY)
            .header("Content-Type", "application/json")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        val jsonObject = JSONTokener(response.body()).nextValue() as JSONObject
        val format = Json { ignoreUnknownKeys = true }
        var films = jsonObject.getJSONArray("films")
        pagesCount = jsonObject.getInt("pagesCount")
        var filmList = mutableListOf<Int>()
        for (i in 0 until films.length()) {
            var url = films.getJSONObject(i).getString("posterUrl")
            val film = getFilmId(url)
            filmList.add(film)
        }

        return filmList
    }

    private fun parseInfoAboutFilm(film: Film): String {
        var result = ""
        var parsedGenre = ""
        film.genres.forEach { genre -> parsedGenre += "${genre.genre}, " }
        parsedGenre = parsedGenre.dropLast(2)
        var parsedCountry = ""
        film.countries.forEach { country -> parsedCountry += "${country.country}, " }
        parsedCountry = parsedCountry.dropLast(2)
        var name = film.nameRu
        if (film.nameRu == "") {
            name = film.nameOriginal
        }
        result += "*Название:* $name \n" +
                "*Жанр:* $parsedGenre \n" +
                "*Рейтинг:* ${film.ratingKinopoisk} \n" +
                "*Длительность:* ${film.filmLength} \n" +
                "*Страна:* $parsedCountry \n" +
                "*Фильм на кинопоиске:* ${film.webUrl} \n"
        //"*Картинка:* ${film.picture} \n" ;
        println(film.nameRu)
        return result
    }

    private fun readApiKey(fileName: String) {
        File(fileName).forEachLine { API_KEY = it }
    }

    private fun fillGenreAndCountryMap() {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$mainUrl/api/v2.1/films/filters"))
            .GET()
            .header("X-API-KEY", API_KEY)
            .header("Content-Type", "application/json")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val jsonObject = JSONTokener(response.body()).nextValue() as JSONObject
        var genres = jsonObject.getJSONArray("genres")
        val countries = jsonObject.getJSONArray("countries")

        for (i in 0 until genres.length()) {
            genresMap.put(
                genres.getJSONObject(i).getString("genre"),
                genres.getJSONObject(i).getInt("id")
            )
        }

        for (i in 0 until countries.length()) {
            countriesMap.put(
                countries.getJSONObject(i).getString("country"),
                countries.getJSONObject(i).getInt("id")
            )
        }
    }

    fun getGenreMap(): HashMap<String, Int> {
        return genresMap
    }

    private fun getFilmId(url: String): Int {
        var id = url.substring(url.indexOf("kp/") + 3, url.indexOf(".jpg"))
        return id.toInt()
    }
}