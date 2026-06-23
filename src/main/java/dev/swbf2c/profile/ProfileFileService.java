package dev.swbf2c.profile;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ProfileFileService {

    public BattlefrontProfile load(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        return new BattlefrontProfile(data);
    }

    public void save(Path path, BattlefrontProfile profile) throws IOException {
        createVisibleBackup(path);
        writeSafely(path, profile);
    }

    public void saveAs(Path path, BattlefrontProfile profile) throws IOException {
        if (Files.exists(path)) {
            createVisibleBackup(path);
        }

        writeSafely(path, profile);
    }

    public Path saveWithPossibleRename(
            Path currentPath,
            BattlefrontProfile profile
    ) throws IOException {
        String profileName = profile.getProfileName();
        validateProfileFileName(profileName);

        Path desiredPath = currentPath.resolveSibling(profileName + ".profile");

        if (sameNormalizedPath(currentPath, desiredPath)) {
            save(currentPath, profile);
            return currentPath;
        }

        if (Files.exists(desiredPath)) {
            throw new IOException(
                    "A profile file with this name already exists: "
                            + desiredPath.getFileName()
            );
        }

        Path tempFile = createTempProfileFile(desiredPath, profile);
        Path rollbackBackup = getRollbackBackupPath(currentPath);

        try {
            Files.deleteIfExists(rollbackBackup);

            if (Files.exists(currentPath)) {
                createVisibleBackupForRenamedProfile(currentPath, desiredPath);
                moveReplacing(currentPath, rollbackBackup);
            }

            moveReplacing(tempFile, desiredPath);

            Files.deleteIfExists(rollbackBackup);
            cleanupOldRenameArtifacts(currentPath);

            return desiredPath;

        } catch (IOException exception) {
            if (Files.exists(rollbackBackup) && !Files.exists(currentPath)) {
                moveReplacing(rollbackBackup, currentPath);
            }

            throw exception;

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public void restoreBackup(Path path) throws IOException {
        Path backupPath = getVisibleBackupPath(path);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file does not exist: " + backupPath);
        }

        BattlefrontProfile restoredProfile =
                new BattlefrontProfile(Files.readAllBytes(backupPath));

        writeSafely(path, restoredProfile);
    }

    public boolean backupExists(Path path) {
        return Files.exists(getVisibleBackupPath(path));
    }

    private void createVisibleBackup(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Path backupPath = getVisibleBackupPath(path);

        Files.copy(
                path,
                backupPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );
    }

    private void createVisibleBackupForRenamedProfile(
            Path oldPath,
            Path newPath
    ) throws IOException {
        if (!Files.exists(oldPath)) {
            return;
        }

        Path newBackupPath = getVisibleBackupPath(newPath);

        Files.copy(
                oldPath,
                newBackupPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );
    }

    private Path getVisibleBackupPath(Path path) {
        return path.resolveSibling(path.getFileName() + ".bak");
    }

    private Path getRollbackBackupPath(Path path) {
        return path.resolveSibling("." + path.getFileName() + ".swbfiic-rollback");
    }

    private void writeSafely(Path path, BattlefrontProfile profile) throws IOException {
        Path tempFile = createTempProfileFile(path, profile);

        try {
            moveReplacing(tempFile, path);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private Path createTempProfileFile(
            Path targetPath,
            BattlefrontProfile profile
    ) throws IOException {
        Path absolutePath = targetPath.toAbsolutePath();
        Path parent = absolutePath.getParent();

        if (parent == null) {
            parent = Path.of(".").toAbsolutePath();
        }

        Files.createDirectories(parent);

        Path tempFile = Files.createTempFile(
                parent,
                absolutePath.getFileName().toString(),
                ".tmp"
        );

        Files.write(tempFile, profile.toByteArray());

        return tempFile;
    }

    private void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private boolean sameNormalizedPath(Path first, Path second) {
        return first.toAbsolutePath()
                .normalize()
                .equals(second.toAbsolutePath().normalize());
    }

    private void validateProfileFileName(String profileName) {
        if (profileName.contains("\\")
                || profileName.contains("/")
                || profileName.contains(":")
                || profileName.contains("*")
                || profileName.contains("?")
                || profileName.contains("\"")
                || profileName.contains("<")
                || profileName.contains(">")
                || profileName.contains("|")) {
            throw new IllegalArgumentException(
                    "Profile name contains characters that cannot be used in a Windows filename."
            );
        }
    }

    private void cleanupOldRenameArtifacts(Path oldPath) throws IOException {
        Files.deleteIfExists(getVisibleBackupPath(oldPath));
        Files.deleteIfExists(oldPath.resolveSibling(oldPath.getFileName() + ".before-rename"));
        Files.deleteIfExists(oldPath.resolveSibling(oldPath.getFileName() + ".before-restore"));
        Files.deleteIfExists(getRollbackBackupPath(oldPath));
    }
}