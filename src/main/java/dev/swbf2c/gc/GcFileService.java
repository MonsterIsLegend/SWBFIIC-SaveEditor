package dev.swbf2c.gc;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class GcFileService {

    public GcConquestSave load(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        return new GcConquestSave(data);
    }

    public void save(Path path, GcConquestSave save) throws IOException {
        createVisibleBackup(path);
        writeSafely(path, save);
    }

    public void saveAs(Path path, GcConquestSave save) throws IOException {
        if (Files.exists(path)) {
            createVisibleBackup(path);
        }

        writeSafely(path, save);
    }

    public void restoreBackup(Path path) throws IOException {
        Path backupPath = getVisibleBackupPath(path);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file does not exist: " + backupPath);
        }

        GcConquestSave restoredSave =
                new GcConquestSave(Files.readAllBytes(backupPath));

        writeSafely(path, restoredSave);
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

    private Path getVisibleBackupPath(Path path) {
        return path.resolveSibling(path.getFileName() + ".bak");
    }

    private void writeSafely(Path path, GcConquestSave save) throws IOException {
        Path tempFile = createTempGcFile(path, save);

        try {
            moveReplacing(tempFile, path);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private Path createTempGcFile(
            Path targetPath,
            GcConquestSave save
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

        Files.write(tempFile, save.toByteArray());

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
}