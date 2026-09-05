package com.yuraw.claudecode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import java.io.File

class RunClaudeCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val projectDir = File(basePath)
        val command = CyrillicHomoglyphs.normalize(ClaudeCodeSettingsState.getInstance(project).command)

        try {
            launchTerminal(projectDir, command)
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
                "Claude Code",
                "cmd.exe", "/k", "cd /d \"${dir.absolutePath}\" && $command"
            )
            os.contains("mac") -> listOf(
                "osascript", "-e",
                "tell application \"Terminal\" to do script " +
                    "\"cd '${dir.absolutePath}' && $command\""
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

    companion object {
        private val LOG = Logger.getInstance(RunClaudeCodeAction::class.java)
    }
}
