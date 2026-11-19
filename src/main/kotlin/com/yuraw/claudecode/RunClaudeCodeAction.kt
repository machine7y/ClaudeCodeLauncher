package com.yuraw.claudecode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

// Launches plain `claude` with no extra arguments.
class RunClaudeCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ClaudeCodeLauncher.launch(project, ClaudeCodeLauncher.DEFAULT_COMMAND)
    }
}
