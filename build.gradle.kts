import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.6.10"
    java
}

group = "fr.pickaria"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.dmulloy2.net/repository/public/") // protocol lib
    maven("https://jitpack.io") // vault
    //maven("https://dl.bintray.com/nuvotifier/maven/") // votifier
    maven("https://raw.github.com/MascusJeoraly/LanguageUtils/mvn-repo/") // langutils
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")

    // Database
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.5.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.5.0")

    compileOnly("org.spigotmc:spigot:1.18.1-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-bukkit:2.6.0")
    compileOnly("com.meowj:LangUtils:1.9")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
}

tasks.withType<KotlinCompile>{
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<ShadowJar>{
    archiveFileName.set("pickaria.jar")
    mergeServiceFiles()
    manifest{
        attributes(mapOf("Main-Class" to "fr.pickaria.Main"))
    }
}