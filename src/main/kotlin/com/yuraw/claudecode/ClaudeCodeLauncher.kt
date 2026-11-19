package com.yuraw.claudecode

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.io.File
import java.util.concurrent.TimeUnit

object ClaudeCodeLauncher {

    const val DEFAULT_COMMAND = "claude"

    private val LOG = Logger.getInstance(ClaudeCodeLauncher::class.java)

    fun launch(project: Project, command: String) {
        val basePath = project.basePath ?: return
        val projectDir = File(basePath)
        val normalized = CyrillicHomoglyphs.normalize(command.trim()).ifEmpty { DEFAULT_COMMAND }

        try {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> launchWindows(projectDir, normalized)
                os.contains("mac") -> launchMac(project, projectDir, normalized)
                else -> launchLinux(projectDir, normalized)
            }
        } catch (ex: Exception) {
            LOG.warn("Failed to launch Claude Code", ex)
            showError(project, "Failed to launch Claude Code: ${ex.message}")
        }
    }

    private fun launchWindows(dir: File, command: String) {
        ProcessBuilder(
            "cmd.exe", "/c", "start",
            "Claude Code", "/max",
            "cmd.exe", "/k", "cd /d \"${dir.absolutePath}\" && $command"
        )
            .directory(dir)
            .start()
    }

    // Terminal.app runs the script in a login shell, so the user's PATH
    // (and therefore `claude`) is available there.
    private fun launchMac(project: Project, dir: File, command: String) {
        val shellLine = "cd ${shellQuote(dir.absolutePath)} && $command"
        val process = ProcessBuilder(
            "osascript",
            "-e", "tell application \"Terminal\"",
            "-e", "do script \"${appleScriptEscape(shellLine)}\"",
            "-e", "activate",
            "-e", "end tell",
            // Full screen needs the Accessibility permission for the IDE.
            // Without it the terminal still opens, just not full screen.
            "-e", "try",
            "-e", "tell application \"System Events\" to tell process \"Terminal\" " +
                "to set value of attribute \"AXFullScreen\" of window 1 to true",
            "-e", "end try"
        )
            .directory(dir)
            .redirectErrorStream(true)
            .start()

        // osascript returns as soon as Terminal has the script, so wait for it
        // off the UI thread and report failures such as a denied Automation
        // permission, which would otherwise go unnoticed.
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroy()
                    return@executeOnPooledThread
                }
                if (process.exitValue() != 0) {
                    val output = process.inputStream.bufferedReader().readText().trim()
                    LOG.warn("osascript failed (${process.exitValue()}): $output")
                    val hint = if (output.contains("-1743") || output.contains("Not authorized")) {
                        "\n\nAllow the IDE to control Terminal in System Settings → " +
                            "Privacy & Security → Automation."
                    } else ""
                    showError(project, "Failed to open Terminal: $output$hint")
                }
            } catch (ex: Exception) {
                LOG.warn("Failed to check osascript result", ex)
            }
        }
    }

    private fun launchLinux(dir: File, command: String) {
        // Run through the user's interactive shell so PATH entries added in
        // ~/.bashrc / ~/.zshrc (where `claude` is often installed) are picked
        // up, then keep the window open with a fresh shell.
        val shell = System.getenv("SHELL")?.takeIf { it.isNotBlank() } ?: "/bin/bash"
        val script = "cd ${shellQuote(dir.absolutePath)} && $command; exec ${shellQuote(shell)}"
        val shellArgv = listOf(shell, "-i", "-c", script)

        val terminal = linuxTerminalCandidates().firstOrNull { findInPath(it.executable) != null }
            ?: throw IllegalStateException(
                "No supported terminal emulator found. Install one of: " +
                    LINUX_TERMINALS.joinToString { it.executable }
            )

        ProcessBuilder(terminal.argv(dir, shellArgv))
            .directory(dir)
            .start()
    }

    private class LinuxTerminal(
        val executable: String,
        val argv: (dir: File, shellArgv: List<String>) -> List<String>
    )

    private val LINUX_TERMINALS = listOf(
        LinuxTerminal("gnome-terminal") { dir, cmd ->
            listOf("gnome-terminal", "--maximize", "--working-directory=${dir.absolutePath}", "--") + cmd
        },
        LinuxTerminal("konsole") { dir, cmd ->
            listOf("konsole", "--workdir", dir.absolutePath, "-e") + cmd
        },
        LinuxTerminal("xfce4-terminal") { dir, cmd ->
            listOf("xfce4-terminal", "--maximize", "--working-directory=${dir.absolutePath}", "-x") + cmd
        },
        // Debian policy: `x-terminal-emulator -e <command> <args...>`.
        LinuxTerminal("x-terminal-emulator") { _, cmd ->
            listOf("x-terminal-emulator", "-e") + cmd
        },
        LinuxTerminal("tilix") { dir, cmd ->
            listOf("tilix", "--maximize", "--working-directory=${dir.absolutePath}", "-e", cmd.joinToString(" ") { shellQuote(it) })
        },
        LinuxTerminal("kitty") { dir, cmd ->
            listOf("kitty", "--start-as=maximized", "--directory", dir.absolutePath) + cmd
        },
        LinuxTerminal("alacritty") { dir, cmd ->
            listOf("alacritty", "-o", "window.startup_mode=\"Maximized\"", "--working-directory", dir.absolutePath, "-e") + cmd
        },
        LinuxTerminal("xterm") { _, cmd ->
            listOf("xterm", "-maximized", "-e") + cmd
        }
    )

    // The terminal that matches the current desktop goes first, so KDE users
    // get Konsole even when GNOME Terminal is also installed, and so on.
    private fun linuxTerminalCandidates(): List<LinuxTerminal> {
        val desktop = (System.getenv("XDG_CURRENT_DESKTOP") ?: "").uppercase()
        val preferred = when {
            desktop.contains("KDE") -> "konsole"
            desktop.contains("XFCE") -> "xfce4-terminal"
            desktop.contains("GNOME") || desktop.contains("UNITY") ||
                desktop.contains("CINNAMON") || desktop.contains("BUDGIE") -> "gnome-terminal"
            else -> null
        }
        return LINUX_TERMINALS.sortedBy { if (it.executable == preferred) 0 else 1 }
    }

    private fun findInPath(executable: String): File? =
        (System.getenv("PATH") ?: "").split(File.pathSeparator)
            .filter { it.isNotEmpty() }
            .map { File(it, executable) }
            .firstOrNull { it.isFile && it.canExecute() }

    // POSIX single-quote quoting: safe for any path, including ones with ' or spaces.
    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

    private fun appleScriptEscape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun showError(project: Project, message: String) {
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) {
                Messages.showErrorDialog(project, message, "Claude Code")
            }
        }
    }
}
