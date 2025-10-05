plugins {
    java
}

group = "org.flameshine"
version = "1.0-SNAPSHOT"

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

dependencies {
    implementation("com.typesafe:config:$typesafeVersion")
    implementation("info.picocli:picocli:$picocliVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("it.tdlight:tdlight-java-bom:$tdLightVersion")
    implementation("it.tdlight:tdlight-java:$tdLightVersion")
    implementation("it.tdlight:tdlight-natives:$tdLightVersion")
    implementation("com.openai:openai-java:$openAiVersion")
}