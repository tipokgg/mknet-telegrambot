package GoogleAPIUtils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SheetsServiceUtil {

    static final String APPLICATION_NAME = "Google Sheets Example";
    static NetHttpTransport HTTP_TRANSPORT = null;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final static String spreadsheetId = "1iOSnDxiyQA0szC16cfYuOOKp4gl4nSGNYUk7Etobekw";

    public SheetsServiceUtil() {
    }

    public static Sheets getSheetsService() throws IOException {
        return new Sheets.Builder(HTTP_TRANSPORT, GoogleAuthorizeUtil.JSON_FACTORY, GoogleAuthorizeUtil.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

    }

    public static String getSpreadsheetId() {
        return spreadsheetId;
    }
}
