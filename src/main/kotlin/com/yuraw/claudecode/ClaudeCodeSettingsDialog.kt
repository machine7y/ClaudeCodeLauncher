package com.yuraw.claudecode

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class ClaudeCodeSettingsDialog(private val project: Project) : DialogWrapper(project) {

    private val commandField = JBTextField(ClaudeCodeSettingsState.getInstance(project).command, 30)

    init {
        title = "Claude Code Settings"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Command:", commandField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun doOKAction() {
        val text = CyrillicHomoglyphs.normalize(commandField.text.trim())
        ClaudeCodeSettingsState.getInstance(project).command = text.ifEmpty { "claude" }
        super.doOKAction()
    }
}
