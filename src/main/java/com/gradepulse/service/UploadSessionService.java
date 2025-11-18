package com.gradepulse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadSessionService {
    private final Map<String, Path> sessions = new ConcurrentHashMap<>();
    private final Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "gradepulse-uploads");

    public UploadSessionService() throws IOException {
        Files.createDirectories(tempDir);
    }

    public String saveFile(String sessionId, MultipartFile file) throws IOException {
        Path path = tempDir.resolve(sessionId + ".xlsx");
        if (path == null) {
            throw new IOException("Failed to create file path");
        }
        // Use Files.copy instead of transferTo to avoid null safety warning
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        sessions.put(sessionId, path);
        return sessionId;
    }

    public Path getFilePath(String sessionId) {
        Path path = sessions.get(sessionId);
        if (path == null) {
            throw new IllegalArgumentException("Session expired");
        }
        return path;
    }

    public InputStream getFileInputStream(String sessionId) throws IOException {
        Path path = getFilePath(sessionId);
        return Files.newInputStream(path);
    }

    public void cleanup(String sessionId) {
        Path path = sessions.remove(sessionId);
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
        }
    }
}