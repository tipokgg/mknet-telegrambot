package GoogleAPIUtils;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import service.googleapi.ColumnsConverter;
import service.EmployeeService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SheetsQuickstart {

    public static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    public static Map<String, String> getMapWithTickets(long telegramId, Date date) throws IOException, GeneralSecurityException {

        long start = System.currentTimeMillis();

        // форматирование даты под то, как она выглядит в графике
        String dateFormatted = sdf.format(date);

        Sheets sheetsService = SheetsServiceUtil.getSheetsService(); // получения объекта для взаимодействия с GoogleAPI
        int startString = 0; // строка, с которой начинаются заявки нужного монтажника
        String column = null; // колонка, по которой будут искаться заявки по нужной дате
        String fullName = EmployeeService.getFullNameByTelegramId(telegramId); // поиск ФИО монтажника по TelegramID

        // если по TelegramID никто не найден, метод getMapWithTickets вернёт null
        if (fullName == null)
            return null;

        float delta1 = System.currentTimeMillis()-start;
        System.out.println("Method getMapWithTickets() working now for " + delta1 / 1000 + " secs (before getting date column)");

        // поиск в диапазоне A1:AR1 даты, по которой запрашиваются заявки
        String dateRange = "A1:AR1";
        ValueRange dateResponse = sheetsService.spreadsheets().values()
                .get(SheetsServiceUtil.getSpreadsheetId(), dateRange)
                .execute();

        List<List<Object>> dateValues = dateResponse.getValues();

        // проходим в цикле, если находим дату, то записываем её в column в виде AA (конвертируем при помощи ColumnsConverter)
        for (int i = 0; i < dateValues.get(0).size(); i++) {
            try {
                String s = (String) dateValues.get(0).get(i);
                if (s.equals(dateFormatted)) {
                    column = ColumnsConverter.getColumn(i);
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
        }

        float delta2 = System.currentTimeMillis()-start;
        System.out.println("Method getMapWithTickets() working now for " + delta2 / 1000 + " secs (before getting installer string)");


        // ищем строчку монтажника в первой колонке по ФИО.
        String searchStartStringRange = "Лист1!A1:A100";

        ValueRange startStringResponse = sheetsService.spreadsheets().values()
                .get(SheetsServiceUtil.getSpreadsheetId(), searchStartStringRange)
                .execute();

        // получение всех ячеек столбца A1:A100
        List<List<Object>> b = startStringResponse.getValues();

        for (int i = 0; i < b.size(); i++) {
            if (!b.get(i).isEmpty()) {
               if (b.get(i).get(0).equals(fullName)) {
                   startString = i+1;
                   break;
                }
            }
        }

        // если прошлись в цикле и не нашли совпадений по ФИО в графике, метод getMapWithTickets вернёт null
        if (startString == 0) return null;

        float delta3 = System.currentTimeMillis()-start;
        System.out.println("Method getMapWithTickets() working now for " + delta3 / 1000 + " secs (before getting tickets)");

        // рендж ячеек в котором содержатся заявки конкретного монтажинка (строки) по конкретной дате (столбец)
        String range = "Лист1!" + column + startString + ":" +
                column + (startString + 9);

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SheetsServiceUtil.getSpreadsheetId(), range)
                .execute();

        List<List<Object>> values = response.getValues();

        Map<String, String> map = new TreeMap<>();

        String key = "";
        String value = "";

        for (int i = 0; i < 10; i++) {

            // если количество полученных ячеек < 10 (т.е. в конце графика есть пустые
            // и мы сейчас находимся на итерации, когда можем получить значение времени, но ячейка под ним пустая
            if (values.size() < 10 && i == values.size() - 1) {
                map.put((String) values.get(i).get(0), "Пустая ячейка");
                break;
            }
            // если количество полученных ячеек < 10 (т.е. в конце графика есть пустые
            // и мы сейчас находимся на итерации, когда не можем получить значение времени (ячейчка пуста) и дальше тоже пусто
            if (values.size() < 10 && i == values.size()) {
                map.put("Пустая ячейка", "Пустая ячейка");
                break;
            }

            // проверям что в ячейке что то есть, иначе пишем что ячейка пустая
            if (values.get(i).size() > 0)
                key = (String) values.get(i).get(0);
            else
                key = "Пустая ячейка";

            if (values.get(++i).size() > 0) {
                value = (String) values.get(i).get(0);
            } else value = "Пустая ячейка";

            map.put(key, value);
        }

        long end = System.currentTimeMillis() - start;
        System.out.println("Method getMapWithTickets() in GoogleAPI worked for " + end / 1000.0f + " secs");

        return map;

    }


    public static String isInstallOrTechnical(String input) {

        String onlyDigits = input.replaceAll("\\D+", "");

        if (onlyDigits.length() != 5) return "empty_type";

        if (onlyDigits.startsWith("2") || onlyDigits.startsWith("3")) return "isInstall";
        if (onlyDigits.startsWith("8")) return "isRepair";

        return "empty_type";
    }

}
