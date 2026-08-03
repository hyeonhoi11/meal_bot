package com.example.mealbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "imported_drive_file")
public class ImportedDriveFile {

    @Id
    @Column(name = "file_id", length = 100)
    private String fileId;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "sheet_range", length = 100)
    private String sheetRange;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    public ImportedDriveFile(String fileId, String fileName, String sheetRange) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.sheetRange = sheetRange;
        this.importedAt = Instant.now();
    }
}
