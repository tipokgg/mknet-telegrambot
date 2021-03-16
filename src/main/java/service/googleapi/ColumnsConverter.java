package service.googleapi;

import java.util.HashMap;
import java.util.Map;


// костыль, который позволяет получить буквенное представление столбца по его порядковому номеру
public class ColumnsConverter {

    private static Map<Integer, String> map = new HashMap<>();

    static {
        map.put(0, "A");
        map.put(1, "B");
        map.put(2, "C");
        map.put(3, "D");
        map.put(4, "E");
        map.put(5, "F");
        map.put(6, "G");
        map.put(7, "H");
        map.put(8, "I");
        map.put(9, "J");
        map.put(10, "K");
        map.put(11, "L");
        map.put(12, "M");
        map.put(13, "N");
        map.put(14, "O");
        map.put(15, "P");
        map.put(16, "Q");
        map.put(17, "R");
        map.put(18, "S");
        map.put(19, "T");
        map.put(20, "U");
        map.put(21, "V");
        map.put(22, "W");
        map.put(23, "Z");
        map.put(24, "Y");
        map.put(25, "Z");
        map.put(26, "AA");
        map.put(27, "AB");
        map.put(28, "AC");
        map.put(29, "AD");
        map.put(30, "AE");
        map.put(31, "AF");
        map.put(32, "AG");
        map.put(33, "AH");
        map.put(34, "AI");
        map.put(35, "AJ");
        map.put(36, "AK");
        map.put(37, "AL");
        map.put(38, "AM");
        map.put(39, "AN");
        map.put(40, "AO");
    }

    public static String getColumn(int i) {
        return map.get(i);
    }

}
