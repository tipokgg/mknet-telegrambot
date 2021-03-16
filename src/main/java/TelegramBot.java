import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.SecretsService;


public class TelegramBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    public static void main(String[] args) {
        LOGGER.info("Bot started up....");
        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botApi = new TelegramBotsApi();
        Bot bot = new Bot(
                SecretsService.getProperty("TG_TOKEN"),
                SecretsService.getProperty("TG_BOTUSERNAME"));
        try {
            botApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}