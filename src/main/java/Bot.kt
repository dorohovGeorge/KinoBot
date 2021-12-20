import API.API
import DAO.Filter
import DAO.GenreButton
import Utils.*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.io.File
import kotlin.collections.ArrayList


class Bot() : TelegramLongPollingBot() {
    private var genreText = "Жанр"
    private var ratingText = "Рейтинг"
    private var yearText = "Год"
    private var searchText = "Начать поиск\uD83D\uDE80"
    private var genreCallBack = "filter_genre_callback"
    private var ratingCallBack = "filter_rating_callback"
    private var yearCallBack = "filter_year_callback"
    private var searchCallBack = "filter_search_callback"
    private var resetSearchCallBack = "filter_reset_search"
    private var genresStringList = "-"
    private var ratingStringList = "-"
    private var yearStringList = "-"
    private var filterMessage: String = "*Сейчас выбраны следующие параметры:*\n" +
            "*Жанр:* $genresStringList\n" +
            "*Рейтинг:* $ratingStringList\n" +
            "*Год:* $yearStringList\n"
    private val api: API = API()
    private var pagesCounter = 1
    private var genresButton = mutableListOf<GenreButton>()
    private var genresListCallback = listOf<String>()
    private var genreFilterString = "*Пожалуйста, выберите нужные вам жанры:*"
    private var ratingFilterString = "*Пожалуйста, укажите диапазон рейтинга фильма*\n" +
            "Например: 7-9 "
    private var yaerFilterString = "*Пожалуйста, укажите период выхода фильма*\n" +
            "Например: 2018-2020"
    var filmList = mutableListOf<Int>()
    override fun getBotToken(): String {
        var botToken = ""
        File("src/main/resources/BOT_TOKEN.txt").forEachLine { botToken = it }
        return botToken
    }

    init {
        genresListCallback = buildGenresCallback(api.getGenreMap(), false)
        for (i in genresListCallback.indices) {
            genresButton.add(
                GenreButton(
                    getGenreFromCallback(genresListCallback[i]),
                    "filter_genre_${getGenreFromCallback(genresListCallback[i])}"
                )
            )
        }
    }

    override fun getBotUsername() = "seenima_bot"

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            var responseText = ""
            if (message.hasText()) {
                val messageText = message.text
                if (messageText == "/start") {
                    responseText = "Добро пожаловать!"
                    val responseMessage = SendMessage(chatId.toString(), responseText)
                    responseMessage.enableMarkdown(true)
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Поиск по фильтрам"),
                        )
                    )
                    execute(responseMessage)
                }
                if (messageText == "Получить фильм по ID") {
                    responseText = api.getInfoAboutFilmById(301)
                    val responseMessage = SendMessage(chatId.toString(), responseText)
                    responseMessage.enableMarkdown(true)
                    execute(responseMessage)
                }
                if (messageText == "Поиск по фильтрам") {
                    searchByFilters(update)
                }
                var regex = Regex("\\d+")
                if (messageText.contains(regex) && messageText.contains("-")) {
                    if ((messageText[1] == '-' || messageText[2] == '-')) {
                        ratingStringList = messageText
                        searchByFilters(update)
                    } else if (messageText[4] == '-') {
                        yearStringList = messageText
                        searchByFilters(update)
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            val call_data = update.callbackQuery.data

            if (call_data.contains("filter")) {
                searchByFilters(update)
            }
        }
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }

    private fun searchByFilters(update: Update) {
        if (!update.hasCallbackQuery()) {
            val message = update.message
            val chatId = message.chatId
            val markupInline = InlineKeyboardMarkup()
            val responseMessage =
                SendMessage(chatId.toString(), getFilterMessage(genresStringList, ratingStringList, yearStringList))
            genreText = "Жанр"
            genreCallBack = "filter_genre_callback"
            if (message.hasText()) {
                val messageText = message.text
                var regex: Regex = Regex("\\d+")
                if (messageText.contains(regex) && messageText.contains("-")) {
                    if ((messageText[1] == '-' || messageText[2] == '-')) {
                        responseMessage.text = getFilterMessage(genresStringList, ratingStringList, yearStringList)
                    } else if (messageText[4] == '-') {
                        responseMessage.text = getFilterMessage(genresStringList, ratingStringList, yearStringList)
                    }
                }
            }

            val rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
            rowsInline.add(buildRowsFilter(genreText, genreCallBack))
            rowsInline.add(buildRowsFilter(ratingText, ratingCallBack))
            rowsInline.add(buildRowsFilter(yearText, yearCallBack))
            rowsInline.add(buildRowsFilter(searchText, searchCallBack))
            markupInline.keyboard = rowsInline
            responseMessage.replyMarkup = markupInline
            responseMessage.parseMode = ParseMode.HTML
            responseMessage.enableMarkdown(true)
            this.execute(responseMessage)
        } else {
            val call_data = update.callbackQuery.data
            val messageId = update.callbackQuery.message.messageId.toLong()
            val chatId = update.callbackQuery.message.chatId
            val newMessage = EditMessageText()
            newMessage.chatId = chatId.toString()
            newMessage.messageId = messageId.toInt()
            newMessage.text = getFilterMessage(genresStringList, ratingStringList, yearStringList)
            val markupInline = InlineKeyboardMarkup()
            var rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
            if (!call_data.contains("reset", true)) {
                if (call_data.contains("genre")) {
                    newMessage.text = genreFilterString
                    rowsInline = buildGenreFilter(update)
                }
                if (call_data.contains("rating")) {
                    newMessage.text = ratingFilterString
                }
                if (call_data.contains("year")) {
                    newMessage.text = yaerFilterString
                }
                if (call_data.contains("search")) {
                    newMessage.text = searchFilm(true)
                    rowsInline.add(buildRowsFilter("Следующий фильм", "filter_search_reset_next"))
                    rowsInline.add(buildRowsFilter("Изменить фильтры", resetSearchCallBack))
                }
            } else if (call_data.contains("next")) {
                newMessage.text = searchFilm(false)
                rowsInline.add(buildRowsFilter("Следующий фильм", "filter_search_reset_next"))
                rowsInline.add(buildRowsFilter("Изменить фильтры", resetSearchCallBack))
            } else {
                if (call_data.contains("reset")) {
                    genreCallBack = "filter_genre_callback"
                    rowsInline.add(buildRowsFilter(genreText, genreCallBack))
                    rowsInline.add(buildRowsFilter(ratingText, ratingCallBack))
                    rowsInline.add(buildRowsFilter(yearText, yearCallBack))
                    rowsInline.add(buildRowsFilter(searchText, searchCallBack))
                }
            }


            markupInline.keyboard = rowsInline
            newMessage.replyMarkup = markupInline
            newMessage.parseMode = ParseMode.HTML
            newMessage.enableMarkdown(true)
            this.execute(newMessage)
        }
    }

    private fun buildRowsFilter(
        text: String,
        callBack: String,
    ): List<InlineKeyboardButton> {

        val textRowInline: MutableList<InlineKeyboardButton> = ArrayList()
        val filterText = InlineKeyboardButton(text)
        filterText.callbackData = callBack
        textRowInline.add(
            filterText
        )
        return textRowInline
    }

    private fun buildGenreFilter(update: Update): MutableList<List<InlineKeyboardButton>> {
        val call_data = update.callbackQuery.data

        for (i in genresListCallback.indices) {
            if (call_data.contains(genresListCallback[i])) {
                if (!call_data.contains("enable")) {
                    genresButton[i] = GenreButton(
                        "✅${getGenreFromCallback(genresListCallback[i])}",
                        "filter_genre_${getGenreFromCallback(genresListCallback[i])}_enable"
                    )
                } else {
                    genresButton[i] = GenreButton(
                        getGenreFromCallback(genresListCallback[i]),
                        "filter_genre_${getGenreFromCallback(genresListCallback[i])}"
                    )
                }
            }
        }
        val rowsInline: MutableList<List<InlineKeyboardButton>> = ArrayList()
        var tempGenresStringList = ""
        rowsInline.add(buildRowsFilter("Готово", "filter_genre_callback_reset"))
        for (i in 0 until genresButton.size) {
            if (genresButton[i].text.contains("✅")) {
                tempGenresStringList += genresButton[i].text.substring(1, genresButton[i].text.length) + ", "
            }
            rowsInline.add(buildRowsFilter(genresButton[i].text, genresButton[i].callBack))
        }
        if (tempGenresStringList == "") {
            genresStringList = "-"
        } else {
            genresStringList = tempGenresStringList.substring(0, tempGenresStringList.length - 2)
        }


        return rowsInline
    }

    private fun searchFilm(isFirst: Boolean): String {
        var filter = Filter()
        if (genresStringList != "-") {
            filter.genresList = parseGenre(genresStringList, api.getGenreMap())
        }
        if (ratingStringList != "-") {
            var ratingFrom = parseSpanNumbers(ratingStringList, true)
            var ratingTo = parseSpanNumbers(ratingStringList, false)
            filter.ratingFrom = ratingFrom
            filter.ratingTo = ratingTo
        }
        if (yearStringList != "-") {
            filter.yearFrom = parseSpanNumbers(yearStringList, true)
            filter.yearTo = parseSpanNumbers(yearStringList, false)
        }
        if (isFirst) {
            filmList = api.getFilmByFilters(filter)
        }
        var film = filmList[0]
        var tempList = filmList.subList(1, filmList.size)
        filmList = tempList
        if (filmList.size == 0) {
            if (pagesCounter < api.pagesCount) {
                pagesCounter++
            } else {
                return "К сожалению нам нечего вам больше показать, попробуйте изменить фильтры"
            }
            filter.page = pagesCounter
            filmList = api.getFilmByFilters(filter)
        }
        return api.getInfoAboutFilmById(film)
    }
}