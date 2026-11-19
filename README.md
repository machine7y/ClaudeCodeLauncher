# Claude Code Launcher

A plugin for IntelliJ IDEA and Android Studio that adds a Claude Code button
to the Project view toolbar. It launches [Claude Code](https://claude.com/claude-code)
for the current project in a separate, maximized terminal window.

![Claude Code button in the Project view toolbar](doc/toolbar-button.png)

## Features

Clicking the button opens a dropdown:

![Claude Code dropdown menu](doc/dropdown-menu.png)

- **Run Claude Code** runs plain `claude` in the project root.
- **Run with Options...** opens a dialog with an editable command line, such
  as `claude -c` or `claude --resume`. **Run** saves the command for this
  project and launches it. **Cancel** closes the dialog without saving.
- **Create .claude** creates a `.claude` folder in the project root.

![Run with Options dialog](doc/run-with-options-dialog.png)

The saved command is stored per project in `.idea/claudeCodeLauncher.xml`.
Cyrillic letters that look like Latin ones (for example, `с` typed instead of
`c` on a Russian keyboard layout) are replaced automatically, so `-с` still
works as `-c`.

## Requirements

- IntelliJ IDEA or Android Studio based on platform 2023.3 (build 233) or newer.
- The `claude` CLI installed and available on `PATH`.
- Terminal used for the launch:
  - Windows: `cmd.exe`
  - macOS: Terminal.app
  - Linux: `x-terminal-emulator`

## Build

```sh
./gradlew buildPlugin
```

On Windows, use `gradlew.bat buildPlugin`.

The plugin is written in Kotlin and built against IntelliJ IDEA Community.
Android Studio runs on the same platform, so one build works in both IDEs.
The build writes the zip to `build/distributions/claude-code-launcher-<version>.zip`.

## Install

1. Open **Settings → Plugins**.
2. Click the gear icon and choose **Install Plugin from Disk...**.
3. Select the zip from `build/distributions` and restart the IDE if asked.
