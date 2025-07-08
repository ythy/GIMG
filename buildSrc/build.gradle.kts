plugins {
    `kotlin-dsl`
}

allprojects {
    //skip Test tasks
    gradle.taskGraph.whenReady {
        tasks.forEach { task ->
            val lowercaseName = task.name.lowercase()
            if (lowercaseName.contains("javadoc")) {
                task.enabled = false
            }
        }
    }
}