plugins {
    java
    application
    antlr
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-util:9.2")

    antlr("org.antlr:antlr4:4.9.3")

    testImplementation("org.testng:testng:7.5")
}

application {
    mainClass.set("edu.udel.blc.BLCKt")
}

tasks.compileKotlin {
    dependsOn("generateGrammarSource")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}