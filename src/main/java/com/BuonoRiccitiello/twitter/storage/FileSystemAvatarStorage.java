package com.BuonoRiccitiello.twitter.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemAvatarStorage implements AvatarStorage {

    private static final long MAX_BYTES = 2L * 1024L * 1024L; // 2MB

    @Override
    public String storeAvatar(Long userId, MultipartFile avatar) throws IOException {
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Seleziona un'immagine da caricare.");
        }

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Il file caricato deve essere un'immagine.");
        }

        if (avatar.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Dimensione immagine eccessiva. Max 2 MB.");
        }

        String originalFilename = avatar.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        if (!extension.matches("\\.(png|jpg|jpeg|gif|webp)")) {
            extension = ".png";
        }

        Path uploadDir = Paths.get("uploads", "avatars");
        Files.createDirectories(uploadDir);

        String filename = "user-" + userId + extension;
        Path destination = uploadDir.resolve(filename);
        Files.copy(avatar.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/avatars/" + filename;
    }
}

