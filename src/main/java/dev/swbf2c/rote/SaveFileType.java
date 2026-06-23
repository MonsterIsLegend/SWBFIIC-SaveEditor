package dev.swbf2c.rote;

import java.nio.file.Path;
import java.util.Locale;

public enum SaveFileType {
    PROFILE,
    ROTE,
    UNKNOWN;

    public static SaveFileType detect(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

        if (fileName.endsWith(".profile")) {
            return PROFILE;
        }

        if (fileName.endsWith(".rote")) {
            return ROTE;
        }

        return UNKNOWN;
    }

    public String extension() {
        return switch (this) {
            case PROFILE -> ".profile";
            case ROTE -> ".rote";
            case UNKNOWN -> "";
        };
    }

    public String displayName() {
        return switch (this) {
            case PROFILE -> "Profile";
            case ROTE -> "Rise of the Empire Campaign";
            case UNKNOWN -> "Unknown";
        };
    }
}