package com.vault.service;

import com.vault.config.VaultProperties;
import com.vault.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path basePath;

    public FileStorageService(VaultProperties props) {
        this.basePath = Paths.get(props.getFileStorage().getBasePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + basePath, e);
        }
    }

    public String store(MultipartFile file, String subdirectory) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("EMPTY_FILE", "File is empty");
        }

        try {
            Path dir = basePath.resolve(subdirectory);
            Files.createDirectories(dir);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String filename = UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            file.transferTo(target);

            log.info("Stored file: {}", target);
            return subdirectory + "/" + filename;

        } catch (IOException e) {
            throw ApiException.internal("FILE_STORAGE_ERROR", "Failed to store file");
        }
    }

    public Path resolve(String relativePath) {
        return basePath.resolve(relativePath).normalize();
    }
}
