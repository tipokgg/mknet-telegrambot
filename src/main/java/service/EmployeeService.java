package service;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.StreamSupport;

public class EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);

    public static String getFullNameByTelegramId(long id) throws IOException {

        long start = System.currentTimeMillis();

        String stringId = String.valueOf(id);

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

            JSONArray emp = StreamSupport.stream(arr.spliterator(), false)
                    .map(e -> (JSONArray) e)
                    .filter(x -> x.getString(1).equals(stringId))
                    .findFirst().orElse(null);

            long end = System.currentTimeMillis() - start;
            System.out.println("Method getFullName() in EmployeeService worked for " + end / 1000.0f + " secs");

            if (emp == null)
                return null;
            else
                return emp.getString(0);

        } else {
            LOGGER.error("Response code in getFullNameByTelegramId method is " + responseCode);
            return null;
        }


    }

}
