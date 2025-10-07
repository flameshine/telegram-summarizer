plugins {
    java
    application
}

group = "org.flameshine"
version = "1.0-SNAPSHOT"

application {
    mainClass = "org.flameshine.summarizer.Main"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://mvn.mchv.eu/repository/mchv")
}

val typesafeVersion: String by project
val picocliVersion: String by project
val jacksonVersion: String by project
val tdLightVersion: String by project
val openAiVersion: String by project
val guavaVersion: String by project
val slf4jVersion: String by project
val lombokVersion: String by project

dependencies {

    // General

    implementation("com.typesafe:config:$typesafeVersion")
    implementation("info.picocli:picocli:$picocliVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.openai:openai-java:$openAiVersion")
    implementation("com.google.guava:guava:$guavaVersion")

    // TDLight

    implementation(platform("it.tdlight:tdlight-java-bom:$tdLightVersion"))
    implementation("it.tdlight:tdlight-java")

    implementation("it.tdlight:tdlight-natives") {
        artifact {
            classifier = "macos_arm64"
        }
    }

    // Slf4j

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    // Lombok

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}