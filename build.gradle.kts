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

    maven("https://jitpack.io")
    maven("https://dl.bintray.com/nuvotifier/maven/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")

    compileOnly("org.spigotmc:spigot:1.18-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.github.NuVotifier.NuVotifier:nuvotifier-bukkit:2.6.0")
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