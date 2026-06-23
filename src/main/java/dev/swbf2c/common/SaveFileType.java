package dev.swbf2c.common;

import java.nio.file.Path;
import java.util.Locale;

public enum SaveFileType {
    PROFILE,
    ROTE,
    GC,
    UNKNOWN;

    public static SaveFileType detect(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

        if (fileName.endsWith(".profile")) {
            return PROFILE;
        }

        if (fileName.endsWith(".rote")) {
            return ROTE;
        }

        if (fileName.endsWith(".gc")) {
            return GC;
        }

        return UNKNOWN;
    }

    public String extension() {
        return switch (this) {
            case PROFILE -> ".profile";
            case ROTE -> ".rote";
            case GC -> ".gc";
            case UNKNOWN -> "";
        };
    }

    public String displayName() {
        return switch (this) {
            case PROFILE -> "Profile";
            case ROTE -> "Rise of the Empire Campaign";
            case GC -> "Galactic Conquest";
            case UNKNOWN -> "Unknown";
        };
    }
}