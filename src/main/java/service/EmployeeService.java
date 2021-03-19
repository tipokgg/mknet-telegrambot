package service;

import GoogleAPIUtils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.model.ValueRange;
import entity.Employee;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.StreamSupport;

public class EmployeeService {

    static {
        try {
            LOGGER = LoggerFactory.getLogger(EmployeeService.class);
            employees = EmployeeService.initEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Map<Long, Employee> employees;

    private static Logger LOGGER;

    public static void init() {
    }


    public static Map<Long, Employee> initEmployees() throws IOException {

        LOGGER.info("Starting init Employees in EmployeeService");

        long start = System.currentTimeMillis();

        Map<Long, Employee> result = new HashMap<>();

        String query = "https://wwwip.saitovnet.com/20150515/phonebook.php?json";

        URL url = new URL(query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            String inline = "";

            Scanner sc = new Scanner(url.openStream());
            while (sc.hasNext()) {
                inline += sc.nextLine();
            }
            sc.close();

            JSONArray arr = new JSONArray(inline);

            Object[] emp = StreamSupport.stream(arr.spliterator(), false)
                    .map(e -> (JSONArray) e)
                    .filter(x -> !x.getString(1).equals(""))
                    .toArray();


            for (Object json : emp) {
                JSONArray empl = (JSONArray) json;
                int startRow = initStartRow(empl.getString(0));
                result.put(Long.valueOf(empl.getString(1)), new Employee(empl.getString(0), startRow));
            }

        } else {
            LOGGER.error("Response code in initEmployees() method is " + responseCode);
            return null;
        }


        long end = System.currentTimeMillis() - start;
        System.out.println("Method getEmployees() in EmployeeService worked for " + end / 1000.0f + " secs");

        LOGGER.info("Successful initialization Employees in EmployeeService. Employees with TelegramID were found: " + result.size());

        return result;

    }

    public static String getFullName(long telegramId) {
        return employees.get(telegramId).getFullName();
    }

    public static int getRow(long telegramId) {
        return employees.get(telegramId).getStartRow();
    }

    private static int initStartRow(String fullName) throws IOException {

        int startString = 0;

        String searchStartStringRange = "Лист1!A1:A100";

        ValueRange startStringResponse = SheetsServiceUtil.getSheetsService().spreadsheets().values()
                .get(SheetsServiceUtil.getSpreadsheetId(), searchStartStringRange)
                .execute();

        List<List<Object>> values = startStringResponse.getValues();

        for (int i = 0; i < values.size(); i++) {
            if (!values.get(i).isEmpty()) {
                if (values.get(i).get(0).equals(fullName)) {
                    startString = i + 1;
                    break;
                }
            }
        }

        return startString;

    }

}
