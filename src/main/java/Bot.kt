import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class Bot() : TelegramLongPollingBot() {
    override fun getBotToken() = "2127424705:AAFo97m-rBUH_NwgCwfM9_BqQbC4YMULfxQ"

    override fun getBotUsername() = "seenima_bot"

    override fun onUpdateReceived(update: Update) {
        if(update.message.text == "/start") {
            var sendMessage = SendMessage()
            sendMessage.chatId = update.message.chatId.toString()
            sendMessage.text = "Привет!"
            execute(sendMessage)
        }
    }
}