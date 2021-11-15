import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


fun main() {
    val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
    val bot = Bot()
    telegramBotsApi.registerBot(bot)
}



