import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.EmployeeService;


public class TelegramBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);

    public static void main(String[] args) {
        LOGGER.info("Bot started up....");

        EmployeeService.init(); // инициализируем объекты монтажников для приложения

        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botApi = new TelegramBotsApi();
        Bot bot = new Bot(
                System.getenv("TG_TOKEN"),
                System.getenv("TG_BOTUSERNAME"));
        try {
            botApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}