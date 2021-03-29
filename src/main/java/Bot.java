import service.googleapi.MainSheetsService;
import com.google.api.services.sheets.v4.model.RowData;
import com.vdurmont.emoji.EmojiParser;
import dao.SQLConnector;
import entity.BillingContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import service.CableService;
import service.EmployeeService;
import service.googleapi.StrikethroughService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    protected Bot(String botToken, String botUsername) {
    }

    @Override
    public String getBotUsername() {
        return  System.getenv("TG_BOTNAME");
        //возвращаем юзера
    }

    @Override
    public void onUpdateReceived(Update update) {

        long start = System.currentTimeMillis();

        try {
            if (update.hasMessage() &&
                    EmployeeService.getFullName(update.getMessage().getChatId()) != null) {

                if (update.getMessage().hasText()) {
                    if (update.getMessage().getText().equals("/start")) {
                        execute(sendInlineKeyBoardMessage(update.getMessage().getChatId(), new Date()));
                    }
                }

            } else if (update.hasCallbackQuery() &&
                    EmployeeService.getFullName(update.getCallbackQuery().getMessage().getChatId()) != null) {

                // конструкция try-finally для того чтобы отправить ответ на Callback Query в блоке finally
                try {

                    if (update.getCallbackQuery().getData().startsWith("±29")) {

                        String msg = update.getCallbackQuery().getData().replaceAll("±", "");
                        BillingContract billingContract = SQLConnector.getBillingContractForInstall(msg);

                        List<Integer> unStrikethroughCell = StrikethroughService.getCellCoordinatesByData(msg, update.getCallbackQuery().getMessage().getChatId());

                        billingContract.setUnStrikethroughCell(unStrikethroughCell);

                        // дополнительынй обработчик для того, чтобы если в тексте комментария содержится какой нибудь
                        // символ форматирования телеграм api (например, <html>)
                        // перехватывать исключение и отправлять сообщение без .setParseMode("HTML")
                        try {
                            execute(new SendMessage()
                                    .setParseMode("HTML")
                                    .setText(billingContract.getInfoForInstall())
                                    .setReplyMarkup(getKeyboardForContract(billingContract))
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId()));
                        } catch (TelegramApiRequestException e) {
                            LOGGER.error("Exception in onUpdateReceived method (else-if block, send install ticket)");
                            LOGGER.error(e.getMessage() + ": " + e.getApiResponse());
                            execute(new SendMessage()
                                    .setText(billingContract.getInfoForInstall())
                                    .setReplyMarkup(getKeyboardForContract(billingContract))
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId()));
                        }

                    } else if (update.getCallbackQuery().getData().startsWith("±8") && update.getCallbackQuery().getData().length() == 6) {

                        String msg = update.getCallbackQuery().getData().replaceAll("±", "");
                        BillingContract billingContract = SQLConnector.getBillingContractForRepair(msg);

                        List<Integer> unStrikethroughCell = StrikethroughService.getCellCoordinatesByData(msg, update.getCallbackQuery().getMessage().getChatId());

                        billingContract.setUnStrikethroughCell(unStrikethroughCell);

                        // дополнительынй обработчик для того, чтобы если в тексте комментария содержится какой нибудь
                        // символ форматирования телеграм api (например, <html>)
                        // перехватывать исключение и отправлять сообщение без .setParseMode("HTML")
                        try {
                            execute(new SendMessage()
                                    .setParseMode("HTML")
                                    .setText(billingContract.getInfoForRepair())
                                    .setReplyMarkup(getKeyboardForContract(billingContract))
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId()));
                        } catch (TelegramApiRequestException e) {
                            LOGGER.error("Exception in onUpdateReceived method (else-if block, send repair ticket)");
                            LOGGER.error(e.getMessage() + ": " + e.getApiResponse());
                            execute(new SendMessage()
                                    .setText(billingContract.getInfoForRepair())
                                    .setReplyMarkup(getKeyboardForContract(billingContract))
                                    .setChatId(update.getCallbackQuery().getMessage().getChatId()));
                        }

                    } else if (update.getCallbackQuery().getData().startsWith("±pppoe")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");
                        String message = SQLConnector.getPppoeCredentials(ss[2]);

                        execute(new SendMessage()
                                .setParseMode("HTML")
                                .setText(message)
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));

                    } else if (update.getCallbackQuery().getData().startsWith("±lk")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");
                        String message = SQLConnector.getPersonalCredentials(ss[2]);

                        execute(new SendMessage()
                                .setParseMode("HTML")
                                .setText(message)
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));

                    } else if (update.getCallbackQuery().getData().startsWith("±customTicketDate")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");

                        Date date = new Date();

                        try {
                            date = sdf.parse(ss[2]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        execute(sendInlineKeyBoardMessage(update.getCallbackQuery().getMessage().getChatId(), date));
                    } else if (update.getCallbackQuery().getData().startsWith("±cabdiag")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");
                        long startCableTest = System.currentTimeMillis();
                        String message = CableService.testCable(ss[2]);
                        long end = System.currentTimeMillis() - startCableTest;
                        System.out.println("Method testCable worked for " + end / 1000.0f + " secs");

                        execute(new SendMessage()
                                .setParseMode("HTML")
                                .setText(message)
                                .setReplyMarkup(getKeyboardForCableTest(ss[2]))
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));

                    } else if (update.getCallbackQuery().getData().startsWith("±strike")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");

                        String a1Cell = StrikethroughService.setStrikethroughForCell(Integer.parseInt(ss[2]), Integer.parseInt(ss[3]));

                        execute(new SendMessage()
                                .setText("Заявка по координатам " + a1Cell + " зачеркнута!")
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));

                        execute(sendInlineKeyBoardMessage(update.getCallbackQuery().getMessage().getChatId(), new Date()));


                    } else if (update.getCallbackQuery().getData().startsWith("±empty_type")) {

                        String[] ss = update.getCallbackQuery().getData().split("±");

                        execute(new SendMessage()
                                .setText("Не удалось определить тип заявки. \n Информация в ячейке графика: \n" + ss[2])
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));

                    } else {
                        execute(new SendMessage()
                                .setText("Не понимаю о чём ты...")
                                .setChatId(update.getCallbackQuery().getMessage().getChatId()));
                    }
                } finally {
                    execute(new AnswerCallbackQuery().setCallbackQueryId(update.getCallbackQuery().getId()));
                }
            }

        } catch (TelegramApiException | IOException | GeneralSecurityException e) {
            LOGGER.error("Exception in onUpdateReceived method (global)", e);
        } catch (Exception e) {
            LOGGER.error("Unhandled exception in onUpdateReceived() method", e);
        }

        long end = System.currentTimeMillis() - start;
        System.out.println("Method worked for " + end / 1000.0f + " secs");

    }


    public SendMessage sendInlineKeyBoardMessage(long telegramId, Date date) throws IOException, GeneralSecurityException {

        List<RowData> cells = MainSheetsService.getRowData(telegramId, date);

        if (cells == null) {
            return new SendMessage().setChatId(telegramId)
                    .setText("Не найдено заявок для TelegramID " + telegramId);
        }


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            String clearNumber = "";
            String beforeCleaning = cells.get(i + 1).getValues().get(0).getFormattedValue(); // сохраняем состояние до выделения всех цифр изх строки

            if (beforeCleaning != null)
                clearNumber = beforeCleaning.replaceAll("\\D+", ""); // берём все цифры из строки

            // если полученная строчка не подходит под логику технички или подключки
            if (isInstallOrTechnical(clearNumber).equals("empty_type")) {
                // если не подходит (вернулся empty_type), то в clearNumber записываем то что было в ячейке изначально
                clearNumber = "empty_type±" + beforeCleaning;
            }

            String formattedNumber = ""; // подготовка "форматированного" текста, который будет отображаться на кнопке на клавиатуре


            // в зависимости от того, зачёркнута эта ячейка или нет (или вообще пустая), подготавливаем вывод
            if (beforeCleaning == null)
                formattedNumber = "(пусто)";
            else if (cells.get(i + 1).getValues().get(0).getEffectiveFormat().getTextFormat().getStrikethrough())
                formattedNumber = cells.get(i + 1).getValues().get(0).getFormattedValue() + " " + EmojiParser.parseToUnicode(" :white_check_mark:");
            else if (!cells.get(i + 1).getValues().get(0).getEffectiveFormat().getTextFormat().getStrikethrough())
                formattedNumber = cells.get(i + 1).getValues().get(0).getFormattedValue() + SQLConnector.getSmallAddress(isInstallOrTechnical(clearNumber), clearNumber);

            // собираем кнопку
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton()
                    .setText(specialTrim(cells.get(i).getValues().get(0).getFormattedValue()) + ": " + formattedNumber)
                    .setCallbackData("±" + clearNumber);

            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonsRow);

            i++; // увеличиваем счётчик чтобы перескочить строку
        }

        // подготовка дат для перемещения по графику вперёд-назад
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.add(Calendar.DATE, -1);
        String prevDay = sdf.format(c.getTime());

        c.add(Calendar.DATE, 2);
        String nextDay = sdf.format(c.getTime());


        InlineKeyboardButton leftArrow = new InlineKeyboardButton()
                .setText(EmojiParser.parseToUnicode(":arrow_left: " + prevDay.substring(0, 5)))
                .setCallbackData("±customTicketDate±" + prevDay);

        InlineKeyboardButton rightArrow = new InlineKeyboardButton()
                .setText(EmojiParser.parseToUnicode(nextDay.substring(0, 5) + " :arrow_right:"))
                .setCallbackData("±customTicketDate±" + nextDay);

        List<InlineKeyboardButton> footerRow = new ArrayList<>();
        footerRow.add(leftArrow);
        footerRow.add(rightArrow);
        rowList.add(footerRow);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return new SendMessage().setChatId(telegramId)
                .setText("Заявки для " + EmployeeService.getFullName(telegramId) + " на " + sdf.format(date))
                .setReplyMarkup(inlineKeyboardMarkup);
    }

    private InlineKeyboardMarkup getKeyboardForContract(BillingContract contract) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton()
                .setText("Проверить кабель")
                .setCallbackData("±cabdiag±" + contract.getNumber());

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton()
                .setText("Получить данные для PPPoE")
                .setCallbackData("±pppoe±" + contract.getCid());

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton()
                .setText("Получить данные для ЛК/Смотрешки")
                .setCallbackData("±lk±" + contract.getCid());

        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();


        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();

        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        keyboardButtonsRow3.add(inlineKeyboardButton3);

        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        rowList.add(keyboardButtonsRow3);

        if (contract.getUnStrikethroughCell().size() > 0) {
            inlineKeyboardButton4
                    .setText("Зачеркнуть заявку")
                    .setCallbackData("±strike±" + contract.getUnStrikethroughCell().get(0) + "±" + contract.getUnStrikethroughCell().get(1));
            List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
            keyboardButtonsRow4.add(inlineKeyboardButton4);
            rowList.add(keyboardButtonsRow4);
        }

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getKeyboardForCableTest(String contractNumber) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton()
                .setText("Проверить ещё раз")
                .setCallbackData("±cabdiag±" + contractNumber);

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        rowList.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    // обрезает пробелы, удаляет пробелы внутри, удаляет из текста "-00"
    private String specialTrim(String s) {

        String result = s.trim();
        result = result.replaceAll(" ", "");
        result = result.replaceAll("-00", "");

        return result;
    }

    private String isInstallOrTechnical(String input) {

        String onlyDigits = input.replaceAll("\\D+", "");

        if (onlyDigits.length() != 5) return "empty_type";

        if (onlyDigits.startsWith("2") || onlyDigits.startsWith("3")) return "isInstall";
        if (onlyDigits.startsWith("8")) return "isRepair";

        return "empty_type";
    }


    @Override
    public String getBotToken() {
        return  System.getenv("TG_TOKEN");
        //Токен бота
    }
}