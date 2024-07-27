package com.github.zimang.codingtracker.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.zimang.codingtracker.listeners.TypingTrackerService
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val typingTrackerService = project.service<TypingTrackerService>()
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(createContent(typingTrackerService), "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createContent(typingTrackerService: TypingTrackerService): JPanel {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val scrollPane = JScrollPane(panel)
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS

        val mainPanel = JBPanel<JBPanel<*>>()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.add(scrollPane)

        val refreshButton = JButton("Refresh").apply {
            addActionListener {
                panel.removeAll()
                typingTrackerService.getTypingIntervals().forEach { record ->
                    panel.add(JBLabel(record.toString()))
                }
                panel.revalidate()
                panel.repaint()
            }
        }

        mainPanel.add(refreshButton)

        return mainPanel
    }
}
