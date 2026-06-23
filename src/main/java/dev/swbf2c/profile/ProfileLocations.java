package dev.swbf2c.profile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ProfileLocations {
    private ProfileLocations() {}

    public static Path findDefaultSaveDirectory() {
        List<Path> candidates = createCandidateDirectories();

        System.out.println("Checking Battlefront II profile directories:");

        for (Path candidate : candidates) {
            System.out.println("  " + candidate);

            if (Files.isDirectory(candidate)) {
                System.out.println("Default profile directory: " + candidate);
                return candidate;
            }
        }

        Path fallback = Path.of(System.getProperty("user.home"));

        System.out.println("No Battlefront II SaveGames folder found.");
        System.out.println("Falling back to: " + fallback);

        return fallback;
    }

    private static List<Path> createCandidateDirectories() {
        List<Path> candidates = new ArrayList<>();

        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String programFiles = System.getenv("ProgramFiles");

        addSteamCandidates(candidates, programFilesX86);
        addGogCandidates(candidates, programFilesX86, programFiles);
        addCdRomCandidates(candidates, programFiles);
        addDriveRootCandidates(candidates);

        return candidates;
    }

    private static void addSteamCandidates(
            List<Path> candidates,
            String programFilesX86
    ) {
        if (programFilesX86 != null) {
            candidates.add(Path.of(
                    programFilesX86,
                    "Steam",
                    "steamapps",
                    "common",
                    "Star Wars Battlefront II",
                    "GameData",
                    "SaveGames"
            ));
        }

        candidates.add(Path.of(
                "C:",
                "Program Files (x86)",
                "Steam",
                "steamapps",
                "common",
                "Star Wars Battlefront II",
                "GameData",
                "SaveGames"
        ));
    }

    private static void addGogCandidates(
            List<Path> candidates,
            String programFilesX86,
            String programFiles
    ) {
        String[] gogRoots = {
                "C:\\GOG Games",
                "D:\\GOG Games",
                "E:\\GOG Games",
                "F:\\GOG Games",
                "C:\\GOG Galaxy\\Games",
                "D:\\GOG Galaxy\\Games",
                "E:\\GOG Galaxy\\Games",
                "F:\\GOG Galaxy\\Games"
        };

        String[] gameFolderNames = getGameFolderNames();

        for (String root : gogRoots) {
            for (String gameFolderName : gameFolderNames) {
                candidates.add(Path.of(
                        root,
                        gameFolderName,
                        "GameData",
                        "SaveGames"
                ));
            }
        }

        if (programFilesX86 != null) {
            for (String gameFolderName : gameFolderNames) {
                candidates.add(Path.of(
                        programFilesX86,
                        "GOG Galaxy",
                        "Games",
                        gameFolderName,
                        "GameData",
                        "SaveGames"
                ));
            }
        }

        if (programFiles != null) {
            for (String gameFolderName : gameFolderNames) {
                candidates.add(Path.of(
                        programFiles,
                        "GOG Galaxy",
                        "Games",
                        gameFolderName,
                        "GameData",
                        "SaveGames"
                ));
            }
        }
    }

    private static void addCdRomCandidates(
            List<Path> candidates,
            String programFiles
    ) {
        if (programFiles != null) {
            candidates.add(Path.of(
                    programFiles,
                    "LucasArts",
                    "Star Wars Battlefront II",
                    "GameData",
                    "SaveGames"
            ));
        }

        candidates.add(Path.of(
                "C:",
                "Program Files",
                "LucasArts",
                "Star Wars Battlefront II",
                "GameData",
                "SaveGames"
        ));
    }

    private static void addDriveRootCandidates(List<Path> candidates) {
        String[] drives = {
                "C:",
                "D:",
                "E:",
                "F:"
        };

        String[] gameFolderNames = getGameFolderNames();

        for (String drive : drives) {
            for (String gameFolderName : gameFolderNames) {
                candidates.add(Path.of(
                        drive + "\\",
                        gameFolderName,
                        "GameData",
                        "SaveGames"
                ));
            }
        }
    }

    private static String[] getGameFolderNames() {
        return new String[] {
                "Star Wars Battlefront II",
                "STAR WARS Battlefront II",
                "Star Wars - Battlefront II",
                "STAR WARS - Battlefront II",
                "Star Wars Battlefront 2",
                "STAR WARS Battlefront 2",
                "Star Wars - Battlefront 2",
                "STAR WARS - Battlefront 2"
        };
    }
}