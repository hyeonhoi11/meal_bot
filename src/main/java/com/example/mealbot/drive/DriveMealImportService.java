package com.example.mealbot.drive;

import com.example.mealbot.domain.ImportedDriveFile;
import com.example.mealbot.repository.ImportedDriveFileRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DriveMealImportService {

    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Drive drive;
    private final Sheets sheets;
    private final ImportedDriveFileRepository importedRepository;

    @Value("${drive.folder-id}")
    private String folderId;

    @Value("${google.spreadsheet-id}")
    private String targetSpreadsheetId;

    @Value("${google.sheet-range}")
    private String targetRange;

    public DriveMealImportService(
            @Value("${google.credentials-path}") String credentialsPath,
            ImportedDriveFileRepository importedRepository) throws Exception {

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of(DriveScopes.DRIVE_READONLY, SheetsScopes.SPREADSHEETS));

        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();
        var credentialsAdapter = new HttpCredentialsAdapter(credentials);

        this.drive = new Drive.Builder(transport, jsonFactory, credentialsAdapter)
                .setApplicationName("meal-bot")
                .build();
        this.sheets = new Sheets.Builder(transport, jsonFactory, credentialsAdapter)
                .setApplicationName("meal-bot")
                .build();
        this.importedRepository = importedRepository;
    }

    public int importNewFilesIfAny() {
        List<File> files;
        try {
            files = drive.files().list()
                    .setQ("'%s' in parents and mimeType='%s' and trashed=false".formatted(folderId, XLSX_MIME))
                    .setFields("files(id,name)")
                    .execute()
                    .getFiles();
        } catch (Exception e) {
            log.error("Drive 폴더 조회 실패", e);
            return 0;
        }

        int imported = 0;
        for (File file : files) {
            ImportedDriveFile previous = importedRepository.findByFileName(file.getName()).orElse(null);
            if (previous != null && previous.getFileId().equals(file.getId())) continue;
            if (importOne(file, previous)) imported++;
        }
        return imported;
    }

    private boolean importOne(File file, ImportedDriveFile previous) {
        try {
            List<List<Object>> rows = readXlsxGrid(file.getId());

            if (rows.isEmpty()) {
                log.warn("{} 에서 읽은 행이 없습니다.", file.getName());
                return false;
            }

            if (previous != null && previous.getSheetRange() != null) {
                sheets.spreadsheets().values()
                        .clear(targetSpreadsheetId, previous.getSheetRange(), new ClearValuesRequest())
                        .execute();
            }

            AppendValuesResponse response = sheets.spreadsheets().values()
                    .append(targetSpreadsheetId, targetRange, new ValueRange().setValues(rows))
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
            String updatedRange = response.getUpdates().getUpdatedRange();

            if (previous != null) importedRepository.delete(previous);
            importedRepository.save(new ImportedDriveFile(file.getId(), file.getName(), updatedRange));

            log.info("{} 반영 완료 ({}행, {})", file.getName(), rows.size(), updatedRange);
            return true;

        } catch (Exception e) {
            log.error("{} 반영 실패", file.getName(), e);
            return false;
        }
    }

    private List<List<Object>> readXlsxGrid(String fileId) throws Exception {
        try (InputStream in = drive.files().get(fileId).executeMediaAsInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<List<Object>> rows = new ArrayList<>();

            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                List<Object> cells = new ArrayList<>();
                if (row != null) {
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        cells.add(cell == null ? "" : formatter.formatCellValue(cell));
                    }
                }
                rows.add(cells);
            }
            return rows;
        }
    }
}
