package com.yuraw.claudecode

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.io.File

object ClaudeCodeLauncher {

    const val DEFAULT_COMMAND = "claude"

    private val LOG = Logger.getInstance(ClaudeCodeLauncher::class.java)

    fun launch(project: Project, command: String) {
        val basePath = project.basePath ?: return
        val projectDir = File(basePath)
        val normalized = CyrillicHomoglyphs.normalize(command.trim()).ifEmpty { DEFAULT_COMMAND }

        try {
            launchTerminal(projectDir, normalized)
        } catch (ex: Exception) {
            LOG.warn("Failed to launch Claude Code", ex)
            Messages.showErrorDialog(
                project,
                "Failed to launch Claude Code: ${ex.message}",
                "Claude Code"
            )
        }
    }

    private fun launchTerminal(dir: File, command: String) {
        val os = System.getProperty("os.name").lowercase()
        val terminalCommand: List<String> = when {
            os.contains("win") -> listOf(
                "cmd.exe", "/c", "start",
                "Claude Code", "/max",
                "cmd.exe", "/k", "cd /d \"${dir.absolutePath}\" && $command"
            )
            os.contains("mac") -> listOf(
                "osascript", "-e",
                "tell application \"Terminal\" to do script " +
                    "\"cd '${dir.absolutePath}' && $command\"\n" +
                    "tell application \"Terminal\" to activate\n" +
                    "tell application \"System Events\" to tell process \"Terminal\" " +
                    "to set value of attribute \"AXFullScreen\" of window 1 to true"
            )
            else -> listOf(
                "x-terminal-emulator", "-e",
                "bash -c \"cd '${dir.absolutePath}' && $command; exec bash\""
            )
        }
        ProcessBuilder(terminalCommand)
            .directory(dir)
            .start()
    }
}
