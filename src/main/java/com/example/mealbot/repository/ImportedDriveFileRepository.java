package com.example.mealbot.repository;

import com.example.mealbot.domain.ImportedDriveFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportedDriveFileRepository extends JpaRepository<ImportedDriveFile, String> {
    Optional<ImportedDriveFile> findByFileName(String fileName);
}
