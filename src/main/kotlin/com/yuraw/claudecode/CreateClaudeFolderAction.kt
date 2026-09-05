package com.yuraw.claudecode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

class CreateClaudeFolderAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath ?: return
        val claudeDir = File(basePath, ".claude")

        try {
            if (claudeDir.exists()) {
                Messages.showInfoMessage(
                    project,
                    "The .claude folder already exists in the project root.",
                    "Claude Code"
                )
                return
            }
            if (!claudeDir.mkdir()) {
                throw java.io.IOException("Could not create directory: ${claudeDir.absolutePath}")
            }
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(claudeDir)
        } catch (ex: Exception) {
            LOG.warn("Failed to create .claude folder", ex)
            Messages.showErrorDialog(
                project,
                "Failed to create .claude folder: ${ex.message}",
                "Claude Code"
            )
        }
    }

    companion object {
        private val LOG = Logger.getInstance(CreateClaudeFolderAction::class.java)
    }
}
