package com.yuraw.claudecode

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class RunClaudeCodeWithOptionsDialog(private val project: Project) : DialogWrapper(project) {

    private val commandField = JBTextField(ClaudeCodeSettingsState.getInstance(project).command, 40)

    var command: String = ClaudeCodeLauncher.DEFAULT_COMMAND
        private set

    init {
        title = "Run Claude Code"
        setOKButtonText("Run")
        // The platform selects the whole text when the field gets focus;
        // undo that afterwards so the caret sits at the end, ready to append.
        commandField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                SwingUtilities.invokeLater {
                    val end = commandField.document.length
                    commandField.select(end, end)
                }
            }
        })
        init()
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Command:", commandField)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun getPreferredFocusedComponent(): JComponent = commandField

    override fun doOKAction() {
        val text = CyrillicHomoglyphs.normalize(commandField.text.trim())
            .ifEmpty { ClaudeCodeLauncher.DEFAULT_COMMAND }
        ClaudeCodeSettingsState.getInstance(project).command = text
        command = text
        super.doOKAction()
    }
}
