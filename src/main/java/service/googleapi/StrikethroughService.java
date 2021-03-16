package service.googleapi;

import GoogleAPIUtils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import service.EmployeeService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrikethroughService {
    public static String setStrikethroughForCell(Integer column, Integer row) throws IOException, GeneralSecurityException {

        String a1Notation = ColumnsConverter.getColumn(column) + (row + 1);

        List<String> ranges = new ArrayList<>(Collections.singletonList(a1Notation));

        Sheets sheetsService = SheetsServiceUtil.getSheetsService();
        Sheets.Spreadsheets.Get request = sheetsService.spreadsheets().get(SheetsServiceUtil.getSpreadsheetId());
        request.setRanges(ranges);
        request.setIncludeGridData(true);

        Spreadsheet response = request.execute();

        List<Request> requests = new ArrayList<>();

        List<RowData> rowDataList = response.getSheets().get(0).getData().get(0).getRowData();
        rowDataList.get(0).getValues().get(0).getUserEnteredFormat().getTextFormat().setStrikethrough(true);

        Request request1 = new Request();
        request1.setUpdateCells(
                new UpdateCellsRequest().setFields("*")
                        .setRows(rowDataList)
                        .setRange(new GridRange()
                                .setSheetId(0)
                                .setStartColumnIndex(column)
                                .setStartRowIndex(row)
                                .setEndColumnIndex(column + 1)
                                .setEndRowIndex(row + 1)));

        requests.add(request1);

        BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();
        requestBody.setRequests(requests);

        Sheets.Spreadsheets.BatchUpdate finalReq =
                sheetsService.spreadsheets().batchUpdate(SheetsServiceUtil.getSpreadsheetId(), requestBody);

        BatchUpdateSpreadsheetResponse response1 = finalReq.execute();

        return a1Notation;
    }

    // обходит все ячейки монтажника (ищет по telegramId) и сверяет на наличие data в них
    // возвращает список, где нулевой элемент это номер столбца, а первый это номер строки
    // в список добавляет координаты первой встретившейся ячейки, которая не зачеркнута
    public static List<Integer> getCellCoordinatesByData(String data, long telegramId) throws IOException {

        Sheets sheetsService = SheetsServiceUtil.getSheetsService(); // получения объекта для взаимодействия с GoogleAPI
        int startString = 0; // строка, с которой начинаются заявки нужного монтажника
        String fullName = EmployeeService.getFullNameByTelegramId(telegramId); // поиск ФИО по телеграм ид

        String searchStartStringRange = "Лист1!A1:A100";

        ValueRange startStringResponse = sheetsService.spreadsheets().values()
                .get(SheetsServiceUtil.getSpreadsheetId(), searchStartStringRange)
                .execute();

        List<List<Object>> b = startStringResponse.getValues();

        for (int i = 0; i < b.size(); i++) {
            if (!b.get(i).isEmpty()) {
                if (b.get(i).get(0).equals(fullName)) {
                    startString = i + 1;
                    break;
                }
            }
        }

        // если прошлись в цикле и не нашли совпадений по ФИО в графике, метод вернёт null
        if (startString == 0) return null;

        List<String> ranges = Collections.singletonList("B" + startString + ":AM" + (startString + 9));

        Sheets.Spreadsheets.Get request = sheetsService.spreadsheets().get(SheetsServiceUtil.getSpreadsheetId());
        request.setRanges(ranges);
        request.setIncludeGridData(true);

        Spreadsheet response = request.execute();

        GridData gridData = response.getSheets().get(0).getData().get(0);

        List<Integer> cell = new ArrayList<>();
        for (int i = 0; i < gridData.getRowData().size(); i++) {
            for (int j = 0; j < gridData.getRowData().get(i).getValues().size(); j++) {

                String check = gridData.getRowData().get(i).getValues().get(j).getFormattedValue();

                if (check != null
                        && check.contains(data)
                        && !gridData.getRowData().get(i).getValues().get(j).getEffectiveFormat().getTextFormat().getStrikethrough()) {

                    cell.add(0, gridData.getStartColumn() + j);
                    cell.add(1, gridData.getStartRow() + i);
                }
            }
        }

        return cell;
    }
}
