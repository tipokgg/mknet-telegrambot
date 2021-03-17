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

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");


    public static List<RowData> getRowData(long telegramId, Date date) throws IOException {

        int startString = EmployeeService.getRow(telegramId); // строка, с которой начинаются заявки нужного монтажника
        String column = getColumn(date); // колонка по нужной дате

        // формируем диапазон запроса
        List<String> ranges = Collections.singletonList(column + startString + ":" + column + (startString + 9));

        Sheets.Spreadsheets.Get request = SheetsServiceUtil.getSheetsService().spreadsheets().get(SheetsServiceUtil.getSpreadsheetId());
        request.setRanges(ranges);
        request.setIncludeGridData(true);

        Spreadsheet response = request.execute();

        return response.getSheets().get(0).getData().get(0).getRowData();

    }

    private static String getColumn(Date date) throws IOException {

        String dateFormatted = sdf.format(date);

        String column = "";

        String dateRange = "A1:AR1";
        ValueRange dateResponse = SheetsServiceUtil.getSheetsService().spreadsheets().values()
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
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return column;
    }

}
