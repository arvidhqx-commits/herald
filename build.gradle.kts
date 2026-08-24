plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}
group = "dev.herald"
version = "0.1.0"
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }
tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
