package dev.swbf2c.rote;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class RoteFileService {

    public RoteCampaignSave load(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        return new RoteCampaignSave(data);
    }

    public void save(Path path, RoteCampaignSave save) throws IOException {
        createBackupIfMissing(path);
        writeSafely(path, save);
    }

    public void saveAs(Path path, RoteCampaignSave save) throws IOException {
        if (Files.exists(path)) {
            createBackupIfMissing(path);
        }

        writeSafely(path, save);
    }

    public void restoreBackup(Path path) throws IOException {
        Path backupPath = getBackupPath(path);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file does not exist: " + backupPath);
        }

        byte[] backupData = Files.readAllBytes(backupPath);
        RoteCampaignSave restoredSave = new RoteCampaignSave(backupData);

        if (Files.exists(path)) {
            Path beforeRestorePath = path.resolveSibling(
                    path.getFileName() + ".before-restore"
            );

            Files.copy(
                    path,
                    beforeRestorePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            );
        }

        writeSafely(path, restoredSave);
    }

    public boolean backupExists(Path path) {
        return Files.exists(getBackupPath(path));
    }

    private void createBackupIfMissing(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Path backupPath = getBackupPath(path);

        if (!Files.exists(backupPath)) {
            Files.copy(
                    path,
                    backupPath,
                    StandardCopyOption.COPY_ATTRIBUTES
            );
        }
    }

    private Path getBackupPath(Path path) {
        return path.resolveSibling(path.getFileName() + ".bak");
    }

    private void writeSafely(Path path, RoteCampaignSave save) throws IOException {
        Path absolutePath = path.toAbsolutePath();
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

        try {
            Files.write(tempFile, save.toByteArray());
            replaceFile(tempFile, absolutePath);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void replaceFile(Path tempFile, Path targetFile) throws IOException {
        try {
            Files.move(
                    tempFile,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    tempFile,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}