import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:${System.getenv("DOKKA_VERSION")}")
    }
}

apply(from = "../template.root.gradle.kts")

fun createTask(name: String) {
    tasks.register(name, org.jetbrains.dokka.gradle.DokkaTask::class) {
        dokkaSourceSets {
            moduleName.set("Some example")
            register("kotlin-stdlib-common") {
                sourceRoots.from("src/main/java")
                sourceRoots.from("src/main/kotlin")
                samples.from("src/main/kotlin")
            }
        }
    }
}

task("runTasks") {
    val taskNumber = (properties["task_number"] as String).toInt()
    repeat(taskNumber) { i ->
        createTask("task_" + i)
        dependsOn("task_" + i)
    }
}
