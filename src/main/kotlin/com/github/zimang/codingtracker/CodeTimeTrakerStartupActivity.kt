

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import io.ktor.utils.io.errors.*
import java.nio.file.Files
import java.nio.file.Paths

class CodeTimeTrackerStartupActivity : StartupActivity {

    private val projectStartTimes = mutableMapOf<Project, Long>()

    override fun runActivity(project: Project) {
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor = event.editor
                val project = editor.project
                if (project != null && !projectStartTimes.containsKey(project)) {
                    projectStartTimes[project] = System.currentTimeMillis()
                }
            }

            override fun editorReleased(event: EditorFactoryEvent) {
                val editor = event.editor
                val project = editor.project
                if (project != null && projectStartTimes.containsKey(project)) {
                    val startTime = projectStartTimes.remove(project) ?: return
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    // Save the duration to a JSON file
                    saveCodingTime(project, duration)
                }
            }
        }, project)
    }

    private fun saveCodingTime(project: Project, duration: Long) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val record = CodingTimeRecord(project.name, duration)

        try {
            val json = gson.toJson(record)
            Files.write(Paths.get("path/to/your/coding_times.json"), json.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    data class CodingTimeRecord(val projectName: String, val duration: Long)
}
