package com.yuraw.claudecode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

// Opens a dialog with the saved command line (e.g. "claude --resume");
// pressing Run saves it and launches Claude Code with it.
class RunClaudeCodeWithOptionsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = RunClaudeCodeWithOptionsDialog(project)
        if (dialog.showAndGet()) {
            ClaudeCodeLauncher.launch(project, dialog.command)
        }
    }
}
