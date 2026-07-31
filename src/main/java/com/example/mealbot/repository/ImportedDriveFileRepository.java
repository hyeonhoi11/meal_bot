package com.example.mealbot.repository;

import com.example.mealbot.domain.ImportedDriveFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportedDriveFileRepository extends JpaRepository<ImportedDriveFile, String> {
}
