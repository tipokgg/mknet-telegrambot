package dao;

import entity.BillingContract;
import service.SecretsService;

import java.sql.*;

public class SQLConnector {

    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    SecretsService.getProperty("DB_BILLING"),
                    SecretsService.getProperty("DB_USER"),
                    SecretsService.getProperty("DB_PASS"));
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static BillingContract getBillingContractForInstall(String contract) {

        BillingContract billingContract = new BillingContract();
        billingContract.setNumber(contract);

        connect();


        //cpt2 - таблица с адресами
        //cpt1.pid=1 - значение ФИО

        // первый запрос на получение cid, адреса, ФИО по номеру договора
        String query = "SELECT " +
                "c.id AS cid, " +
                "cpt2.address AS address, " +
                "cpt1.val AS fullname " +
                "FROM contract c " +
                "INNER JOIN contract_parameter_type_2 cpt2 ON cpt2.cid = c.id " +
                "INNER JOIN contract_parameter_type_1 cpt1 ON cpt1.cid = c.id " +
                "WHERE cpt1.pid=1 AND c.title=" + contract;

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setCid(set.getString("cid"));
                billingContract.setAddress(set.getString("address"));
                billingContract.setFullName(set.getString("fullname"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // второй запрос, на получение номеров телефонов и комментариев к ним по cid
        query = "SELECT phone, comment FROM contract_parameter_type_phone_item WHERE cid=" + billingContract.getCid();

        StringBuilder phones = new StringBuilder();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                phones.append(set.getString("phone") + " ");
                if (!set.getString("comment").isEmpty())
                    phones.append(" (").append(set.getString("comment")).append(") ");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // добавление телефонов в мапу
        billingContract.setPhones(phones.toString());

        // третий запрос на получение комментария по cid
        query = "SELECT val AS comment FROM contract_parameter_type_1 WHERE pid=44 AND cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next())
                billingContract.setComment(set.getString("comment"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // получение даты и времени подключения по cid
        query = "SELECT cpt6.val AS installdate, cpt7v.title AS installtime FROM contract_parameter_type_6 cpt6 " +
                "INNER JOIN contract_parameter_type_7 cpt7 ON cpt7.cid = cpt6.cid " +
                "INNER JOIN contract_parameter_type_7_values cpt7v ON cpt7v.id = cpt7.val " +
                "WHERE cpt6.pid=7 AND cpt7.pid = 80 AND cpt6.cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setInstallDate(set.getString("installdate"));
                billingContract.setInstallTime(set.getString("installtime"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        query = "SELECT cpt7v.title AS promotion FROM contract_parameter_type_7 cpt7 " +
                "INNER JOIN contract_parameter_type_7_values cpt7v ON cpt7v.id = cpt7.val " +
                "WHERE cpt7.pid=100 AND cpt7.cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setPromotion(set.getString("promotion"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        query = "SELECT cpt7v.title AS agent FROM contract_parameter_type_7 cpt7 " +
                "INNER JOIN contract_parameter_type_7_values cpt7v ON cpt7v.id = cpt7.val " +
                "WHERE cpt7.pid=96 AND cpt7.cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setAgent(set.getString("agent"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        query = "SELECT cpt7v.title AS installer FROM contract_parameter_type_7 cpt7 " +
                "INNER JOIN contract_parameter_type_7_values cpt7v ON cpt7v.id = cpt7.val " +
                "WHERE cpt7.pid=79 AND cpt7.cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setInstaller(set.getString("installer"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        disconnect();


        return billingContract;
    }

    public static BillingContract getBillingContractForRepair(String crmTicket) {

        BillingContract billingContract = new BillingContract();
        billingContract.setCrmTicket(crmTicket);

        connect();

        String query = "SELECT rc.cid AS cid, rp.comment AS crmtext FROM register_call rc " +
                "INNER JOIN register_problem rp ON rp.id= rc.rpid " +
                "WHERE rc.rpid=" + crmTicket;

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setCid(set.getString("cid"));
                billingContract.setCrmTicketText(set.getString("crmtext"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        //cpt2 - таблица с адресами
        //cpt1.pid=1 - значение ФИО

        // первый запрос на получение cid, адреса, ФИО по номеру договора
        query = "SELECT " +
                "c.title AS number, " +
                "cpt2.address AS address, " +
                "cpt1.val AS fullname " +
                "FROM contract c " +
                "INNER JOIN contract_parameter_type_2 cpt2 ON cpt2.cid = c.id " +
                "INNER JOIN contract_parameter_type_1 cpt1 ON cpt1.cid = c.id " +
                "WHERE cpt1.pid=1 AND c.id=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setNumber(set.getString("number"));
                billingContract.setAddress(set.getString("address"));
                billingContract.setFullName(set.getString("fullname"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // второй запрос, на получение номеров телефонов и комментариев к ним по cid
        query = "SELECT phone, comment FROM contract_parameter_type_phone_item WHERE cid=" + billingContract.getCid();

        StringBuilder phones = new StringBuilder();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                phones.append(set.getString("phone") + " ");
                if (!set.getString("comment").isEmpty())
                    phones.append(" (").append(set.getString("comment")).append(") ");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // добавление телефонов в мапу
        billingContract.setPhones(phones.toString());


        query = "SELECT val AS comment FROM contract_parameter_type_1 WHERE pid=44 AND cid=" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next())
                billingContract.setComment(set.getString("comment"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        query = "SELECT title AS tariff " +
                "FROM contract_tariff ct " +
                "INNER JOIN tariff_plan tp ON tp.id = ct.tpid " +
                "WHERE ct.date2 IS NULL AND cid =" + billingContract.getCid();

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                billingContract.setTariff(set.getString("tariff"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        disconnect();

        return billingContract;
    }

    public static String getSmallAddress(String request_type, String number) {

        long start = System.currentTimeMillis();

        String cid = "";
        String query = "";

        connect();

        if (request_type.equals("isInstall"))
            query = "SELECT id AS cid FROM contract " +
                    "WHERE title=" + number;
        else if (request_type.equals("isRepair")) {
            query = "SELECT cid FROM register_call " +
                    "WHERE rpid=" + number;
        } else return "";

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                cid = set.getString("cid");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder("(");

        query = "SELECT s.title AS street, h.house AS house, h.frac AS frac, cpt2.flat AS flat, cpt2.room\n" +
                "FROM contract_parameter_type_2 cpt2 \n" +
                "INNER JOIN address_house h ON h.id = cpt2.hid \n" +
                "INNER JOIN address_street s ON s.id = h.streetid \n" +
                "WHERE cpt2.cid =" + cid;

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                sb.append(set.getString("street")).append(" ");
                sb.append(set.getString("house"));

                if (!set.getString("frac").isBlank()) sb.append(set.getString("frac"));
                sb.append("-");

                if (!set.getString("flat").isBlank()) sb.append(set.getString("flat"));
                if (!set.getString("room").isBlank()) sb.append(" ").append(set.getString("room"));

                sb.append(")");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        disconnect();

        String result = sb.toString();

        if (result.length() > 20) {
            if (result.contains("проспект")) result = result.replaceAll("проспект", "пр-т");
            if (result.contains("проезд")) result = result.replaceAll("проезд", "пр-д");
            if (result.contains("Генерала")) result = result.replaceAll("Генерала", "Г.");
            if (result.contains("Октябрьский")) result = result.replaceAll("Октябрьский", "Окт.");
            if (result.contains("Красногвардейский бульвар"))
                result = result.replaceAll("Красногвардейский бульвар", "КГБ");
            if (result.contains("Академика Доллежаля")) result = result.replaceAll("Академика Доллежаля", "Ак Долл.");
            if (result.contains("п. ")) result = result.replaceAll("п. ", "");
            if (result.contains("д. ")) result = result.replaceAll("д. ", "");
            if (result.contains("Бульвар 65 лет Победы")) result = result.replaceAll("Бульвар 65 лет Победы", "Б-р 65 лет");
        }

        long end = System.currentTimeMillis() - start;
        System.out.println("Method getSmallAddress() in SQLConnector worked for " + end / 1000.0f + " secs");

        return result;



    }

    public static String getPppoeCredentials(String cid) {

        connect();

        String query = "SELECT a.login_alias AS login, l.pswd AS password FROM user_login_1 l " +
                "INNER JOIN `user_alias_1` a ON a.login_id = l.id " + "WHERE l.cid =" + cid;

        String login = "";
        String pass = "";

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                login = set.getString("login");
                pass = set.getString("password");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        disconnect();

        return "<b>Данные для PPPoE:</b>\n \n" +
                "<b>Логин:</b> " + login + "\n" +
                "<b>Пароль:</b> " + pass;
    }

    public static String getPersonalCredentials(String cid) {

        connect();

        String query = "SELECT title AS login, pswd AS password FROM contract " +
                "WHERE id =" + cid;

        String login = "";
        String pass = "";

        try (ResultSet set = statement.executeQuery(query)) {
            while (set.next()) {
                login = set.getString("login");
                pass = set.getString("password");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        disconnect();

        return "<b>Данные для \"Личного кабинета\"</b> \n" +
                "<b>Логин:</b> " + login + "\n" +
                "<b>Пароль:</b> " + pass + "\n \n" +
                "<b>Данные для \"Смотрёшки\"</b>" + "\n" +
                "<b>Логин:</b> " + login + "@mk-net.ru\n" +
                "<b>Пароль:</b> " + pass + "\n";
    }
}



