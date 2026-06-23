# SWBFIIC SaveEditor

A Windows save editor for **Star Wars Battlefront II Classic**.

SWBFIIC SaveEditor can edit `.profile`, `.rote`, and `.gc` save files from the game's `GameData/SaveGames` folder.

## Features

### Profile Editor `.profile`

Edit Battlefront II Classic profile data:

* Profile name
* Career medals
* Player points
* Kills
* Deaths

### Rise of the Empire Editor `.rote`

Edit Rise of the Empire campaign progress:

* Select the current campaign mission
* Useful for skipping broken or stuck campaign states

### Galactic Conquest Editor `.gc`

Edit Galactic Conquest save data:

* Player and AI credits
* Player and AI bonus slots
* Controlled planets
* Fleets
* Unlocked units

## Download

Download the latest Windows portable release from the **Releases** page.

Use the file named like:

`SWBFIIC-SaveEditor-1.3.0-windows-portable.zip`

Extract the zip and run:

`SWBFIIC SaveEditor.exe`

Do not download the automatically generated source-code zip unless you want the source code.

## Save File Location

The editor attempts to detect common Battlefront II Classic save locations automatically.

Common locations include:

```text
C:\Program Files (x86)\Steam\steamapps\common\Star Wars Battlefront II\GameData\SaveGames
C:\GOG Games\Star Wars Battlefront II\GameData\SaveGames
C:\Program Files\LucasArts\Star Wars Battlefront II\GameData\SaveGames
```

Custom installs are also checked on common drive roots such as `C:`, `D:`, `E:`, and `F:`.

## Supported File Types

| File type  | Purpose                              |
| ---------- | ------------------------------------ |
| `.profile` | Player profile, medals, stats        |
| `.rote`    | Rise of the Empire campaign progress |
| `.gc`      | Galactic Conquest saves              |

## Backups

Before overwriting a save file, the editor creates or updates a `.bak` backup next to the original file.

Example:

```text
YourStepSister.profile
YourStepSister.profile.bak
```

If something goes wrong, use the restore backup button inside the editor.

## Building from Source

Requirements:

* JDK 21
* Maven

Build the jar:

```powershell
mvn clean package
```

Run the jar:

```powershell
java -jar target\SWBFIIC-SaveEditor-1.3.0.jar
```

Create a Windows portable app image with `jpackage`:

```powershell
jpackage ^
  --type app-image ^
  --name "SWBFIIC SaveEditor" ^
  --app-version "1.3.0" ^
  --vendor "SWBFIIC SaveEditor" ^
  --input target ^
  --main-jar SWBFIIC-SaveEditor-1.3.0.jar ^
  --main-class dev.swbf2c.App ^
  --dest dist
```

## Project Structure

```text
src/main/java/dev/swbf2c/
├── common
├── gc
├── profile
├── rote
├── ui
└── App.java
```

## Disclaimer

This is an unofficial fan-made tool for Star Wars Battlefront II Classic.

It is not affiliated with or endorsed by LucasArts, Pandemic Studios, Disney, GOG, Steam, or any related rights holders.

Use at your own risk and keep backups of important save files.
