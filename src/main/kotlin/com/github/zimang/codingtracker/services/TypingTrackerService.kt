package com.github.zimang.codingtracker.listeners

import com.google.gson.GsonBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.Timer

@Service(Service.Level.PROJECT)
class TypingTrackerService(private val project: Project) {

    private val typingStartTimes = mutableListOf<LocalDateTime>()
    private val typingIntervals = mutableListOf<TypingRecord>()
    private var lastTypingTime: LocalDateTime? = null
    private var typingTimer: Timer? = null
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd-HH-mm-ss")

    init {
        // Register the document listener for all open editors
        EditorFactory.getInstance().addEditorFactoryListener(MyEditorFactoryListener(), project)
    }

    private fun startTyping(fileType: String) {
        if (lastTypingTime == null) {
            lastTypingTime = LocalDateTime.now()
            typingStartTimes.add(lastTypingTime!!)
            startTypingTimer(fileType)
        }
    }

    private fun endTyping(fileType: String) {
        lastTypingTime?.let { startTime ->
            val endTime = LocalDateTime.now()
            val interval = Duration.between(startTime, endTime).toMillis()
            val typingRecord = TypingRecord(
                projectName = project.name,
                startTime = startTime.format(dateTimeFormatter),
                endTime = endTime.format(dateTimeFormatter),
                interval = interval,
                client = System.getProperty("os.name"),
                fileType = fileType
            )
            typingIntervals.add(typingRecord)
            saveRecord(typingRecord)
            lastTypingTime = null
            stopTypingTimer()
        }
    }

    private fun startTypingTimer(fileType: String) {
        typingTimer = Timer(2000) {
            endTyping(fileType)
        }.apply {
            isRepeats = false
            start()
        }
    }

    private fun stopTypingTimer() {
        typingTimer?.stop()
        typingTimer = null
    }

    private fun saveRecord(record: TypingRecord) {
        val recordFile = File(project.basePath, "record.json")
        val records = if (recordFile.exists()) {
            gson.fromJson(recordFile.readText(), Array<TypingRecord>::class.java).toMutableList()
        } else {
            mutableListOf()
        }
        records.add(record)
        recordFile.writeText(gson.toJson(records))
    }

    fun getTypingIntervals(): List<TypingRecord> = typingIntervals

    private inner class MyEditorFactoryListener : EditorFactoryListener {
        override fun editorCreated(event: EditorFactoryEvent) {
            val editor = event.editor
            val document = editor.document
            val file = FileDocumentManager.getInstance().getFile(document)
            val fileType = file?.extension ?: "unknown"

            document.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    startTyping(fileType)
                    typingTimer?.restart()
                }
            })
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            // Optionally, you can remove the listener here if needed
        }
    }

    data class TypingRecord(
        val projectName: String,
        val startTime: String,
        val endTime: String,
        val interval: Long,
        val client: String,
        val fileType: String
    )
}
