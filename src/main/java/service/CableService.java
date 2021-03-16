package service;

import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Scanner;

public class CableService {

    public static String testCable(String contractNumber) throws IOException {

        String query = String.format(
                "http://sys.mk-net.ru/20180730/data.php?contract_id=%s&t=%s",
                contractNumber,
                new Timestamp(System.currentTimeMillis()).getTime());

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

            JSONArray first = arr.getJSONArray(0);
            JSONArray second = arr.getJSONArray(1);
            JSONArray third = arr.getJSONArray(2);


            StringBuilder result = new StringBuilder();

            result.append("<b>Проверка кабеля по №").append(contractNumber).append("</b>\n").append('\n')
                    .append("<b>Статус порта:</b> ").append(first.getString(0)).append('\n');

            if (first.getInt(5) != 0) {
                result.append("<b>Скорость порта:</b> ").append(first.getInt(5)).append(" Mbit/s ").append('\n');
            }

            if (first.getString(0).equals("UP") && second.isNull(0)) {
                result.append("<b>MAC-адрес:</b> (отсутствует)").append("\n \n");
            } else if (!second.isNull(0)) {
                result.append("<b>MAC-адрес:</b> ").append(second.getString(0)).append("\n \n");
            }

            result.append("<b>Первая пара:</b> ").append(first.getString(1)).append(" (").append(first.getInt(2)).append("м)").append('\n')
                    .append("<b>Вторая пара:</b> ").append(first.getString(3)).append(" (").append(first.getInt(4)).append("м)").append('\n')
                    .append("<b>CRC ошибки:</b> ").append(third.getInt(1)).append('\n')
                    .append("<b>_______</b>").append('\n')
                    .append("<b>ID свича:</b> ").append(third.getInt(0)).append('\n')
                    .append("<b>Номер порта:</b> ").append(first.getInt(6)).append('\n');

            return result.toString();
        } else return "Ошибка проверки кабеля по №" + contractNumber;
    }
}
