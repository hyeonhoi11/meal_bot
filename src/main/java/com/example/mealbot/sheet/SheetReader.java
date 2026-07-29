package com.example.mealbot.sheet;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class SheetReader {

    private final Sheets sheets;
    private final String spreadsheetId;
    private final String range;

    public SheetReader(@Value("${google.credentials-path}") String credentialsPath,
                       @Value("${google.spreadsheet-id}") String spreadsheetId,
                       @Value("${google.sheet-range}") String range) throws Exception {

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));

        this.sheets = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("meal-bot")
                .build();

        this.spreadsheetId = spreadsheetId;
        this.range = range;
    }

    public List<List<Object>> readRows() throws IOException {
        List<List<Object>> values = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .setValueRenderOption("FORMATTED_VALUE")
                .execute()
                .getValues();
        return values == null ? List.of() : values;
    }
}
