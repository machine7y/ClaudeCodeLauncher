package com.yuraw.claudecode

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

// Stored per-project in <project>/.idea/claudeCodeLauncher.xml (not shared
// via VCS by default, similar to workspace.xml).
@State(name = "ClaudeCodeSettings", storages = [Storage("claudeCodeLauncher.xml")])
class ClaudeCodeSettingsState : PersistentStateComponent<ClaudeCodeSettingsState> {

    var command: String = "claude"

    override fun getState(): ClaudeCodeSettingsState = this

    override fun loadState(state: ClaudeCodeSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): ClaudeCodeSettingsState =
            project.getService(ClaudeCodeSettingsState::class.java)
    }
}
