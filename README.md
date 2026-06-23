# SWBFIIC SaveEditor

A desktop profile save editor for **Star Wars Battlefront II Classic**.

SWBFIIC SaveEditor edits Battlefront II Classic `.profile` files and creates backups before overwriting existing saves.

## Features

* Edit career medal counts
* Edit player points, kills, and deaths
* Automatic `.bak` backup before saving
* Restore from backup
* Save As support
* Steam, GOG, GOG Galaxy, and original CD-ROM save-folder detection
* Portable Windows app with bundled Java runtime
* Modern Java Swing UI using FlatLaf

## Download

Download the latest Windows portable release from the **Releases** page:

```text
SWBFIIC-SaveEditor-1.0.0-windows-portable.zip
```

Extract the zip, then run:

```text
SWBFIIC SaveEditor.exe
```

Java does not need to be installed separately for the portable release.

## Requirements

For the downloadable release:

* Windows
* Star Wars Battlefront II Classic

For building from source:

* Java 17 or newer
* Maven

## Supported fields

### Career Medals

* Gunslinger
* Frenzy
* Demolition
* Technician
* Marksman
* Regulator
* Endurance
* Guardian
* War Hero

### Career Stats

* Player Points
* Kills
* Deaths

## Backups

Before overwriting an existing profile, the app creates a backup next to the original file.

Example:

```text
Player 1.profile
Player 1.profile.bak
```

When restoring a backup, the current profile is first copied to:

```text
Player 1.profile.before-restore
```

## Known save locations

The app attempts to detect common Battlefront II Classic save folders, including Steam, GOG, GOG Galaxy, and original CD-ROM installations.

Examples:

```text
C:\Program Files (x86)\Steam\steamapps\common\Star Wars Battlefront II\GameData\SaveGames
C:\GOG Games\Star Wars Battlefront II\GameData\SaveGames
C:\GOG Galaxy\Games\Star Wars Battlefront II\GameData\SaveGames
C:\Program Files\LucasArts\Star Wars Battlefront II\GameData\SaveGames
```

It also checks common install folders on `C:`, `D:`, `E:`, and `F:`.

## Building from source

Build the runnable JAR:

```powershell
mvn clean package
```

The shaded JAR is created at:

```text
target\SWBFIIC-SaveEditor-1.0.0.jar
```

Create the portable Windows app:

```powershell
jpackage `
  --type app-image `
  --name "SWBFIIC SaveEditor" `
  --app-version "1.0.0" `
  --vendor "SWBFIIC SaveEditor" `
  --input target `
  --main-jar SWBFIIC-SaveEditor-1.0.0.jar `
  --main-class dev.swbf2c.App `
  --dest dist
```

Create the release zip:

```powershell
Compress-Archive `
  -Path ".\dist\SWBFIIC SaveEditor" `
  -DestinationPath ".\dist\SWBFIIC-SaveEditor-1.0.0-windows-portable.zip" `
  -Force
```

## Disclaimer

This is an unofficial fan-made utility.

It is not affiliated with LucasArts, Disney, Steam, or GOG.

Always back up your save files before editing them.

## License

This project is licensed under the GNU General Public License v3.0 or later.
