package entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillingContract {

    String cid; //+
    String number; //+
    String fullName; //+
    String phones; //+
    String address; //+
    String comment; //+
    String tariff; //+

    String installDate; //+
    String installTime;//+
    String installer; //+

    String promotion; //+
    String agent; //+

    String crmTicket; //++
    String crmTicketText; //++

    List<Integer> unStrikethroughCell;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = format.parse(installDate);

            DateFormat df = new SimpleDateFormat("dd.MM.yy");
            this.installDate = df.format(date);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            this.installDate = installDate;
        }
    }

    public String getInstallTime() {
        return installTime;
    }

    public void setInstallTime(String installTime) {
        this.installTime = installTime;
    }

    public String getInstaller() {
        return installer;
    }

    public void setInstaller(String installer) {
        this.installer = installer;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getCrmTicket() {
        return crmTicket;
    }

    public void setCrmTicket(String crmTicket) {
        this.crmTicket = crmTicket;
    }

    public String getCrmTicketText() {
        return crmTicketText;
    }

    public void setCrmTicketText(String crmTicketText) {
        this.crmTicketText = crmTicketText;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        if (this.tariff == null) {
            this.tariff = tariff;
        } else {
            this.tariff += '\n' + tariff;
        }
    }

    public List<Integer> getUnStrikethroughCell() {
        return unStrikethroughCell;
    }

    public void setUnStrikethroughCell(List<Integer> unStrikethroughCell) {
        this.unStrikethroughCell = unStrikethroughCell;
    }

    public String getInfoForInstall() {

        StringBuilder sb = new StringBuilder();

        sb.append("<b>Заявка на подключение</b> \n" +
                "<b>Договор №" + number + "\n</b>" +
                "<b>ФИО:</b> " + fullName + '\n' +
                "<b>Телефон:</b> " + phones + '\n' +
                "<b>Адрес:</b> " + address + '\n');

        if (comment == null) {
            sb.append("<b>Комментарий:</b> (пусто)" + '\n');
        } else {
            sb.append("<b>Комментарий:</b> " + comment + '\n');
        }

        sb.append("<b>Дата\\время:</b> " + installDate + " " + installTime + '\n' +
                "<b>Монтажник:</b> " + installer + '\n');


        if (promotion != null) {
            sb.append("<b>_____</b> " + '\n' +
                    "<b>Агент:</b> " + agent + '\n' +
                    "<b>Акция:</b> " + promotion);
        }

        return sb.toString();
    }

    public String getInfoForRepair() {

        StringBuilder sb = new StringBuilder();


        sb.append("<b>Заявка на ремонт №" + crmTicket + "</b>" + '\n' +
                crmTicketText + "\n" +
                "<b>_______</b>" + "\n" +
                "<b>по договору №" + number + "\n</b>" +
                "<b>ФИО:</b> " + fullName + '\n' +
                "<b>Телефон:</b> " + phones + '\n' +
                "<b>Адрес:</b> " + address + '\n' +
                "<b>Тариф:</b> " + tariff);

        if (comment != null) {
            sb.append("\n<b>Комментарий:</b> " + comment + '\n');
        }

        return sb.toString();
    }
}
