package com.BuonoRiccitiello.twitter.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction for storing user avatar images.
 */
public interface AvatarStorage {

    /**
     * Stores the given avatar and returns the public path to access it (e.g. /uploads/avatars/user-1.png).
     * Implementations should validate the file (type/size/extension) and may throw IOException on I/O errors
     * or IllegalArgumentException for invalid inputs.
     */
    String storeAvatar(Long userId, MultipartFile avatar) throws IOException;
}

