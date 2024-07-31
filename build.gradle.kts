import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.*

plugins {
    java
    id("dev.s7a.gradle.minecraft.server") version "3.1.0"
}

group = "dev.pnfx"
version = "1.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// Ensure that the paper-plugin.yml file is included in the jar
tasks.jar {
    from(sourceSets.main.get().output)
    from("src/main/resources") {
        include("paper-plugin.yml")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


listOf(
        "8" to "1.8.8",
        "9" to "1.9.4",
        "10" to "1.10.2",
        "11" to "1.11.2",
        "12" to "1.12.2",
        "13" to "1.13.2",
        "14" to "1.14.4",
        "15" to "1.15.2",
        "16" to "1.16.5",
        "17" to "1.17.1",
        "18" to "1.18.2",
        "19" to "1.19.4",
        "20" to "1.20.4",
        "21" to "1.21",
).forEach { (name, version) ->
    tasks.register<LaunchMinecraftServerTask>("testPlugin$version") {
        dependsOn("build")

        doFirst {
            copy {
                from(buildDir.resolve("libs/${project.name}-${project.version}.jar"))
                into(buildDir.resolve("MinecraftServer$name/plugins"))
            }
        }

        serverDirectory.set(buildDir.resolve("MinecraftServer$name").absolutePath)
        jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper(version))
        agreeEula.set(true)
    }
}
