plugins {
    id("java")
    application
}

application {
    mainClass.set("dev.xerohero.Main")
}

group = "dev.xerohero"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // PDF generation
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    // Image processing
    implementation("org.apache.pdfbox:pdfbox-tools:3.0.2")
    // File utilities
    implementation("commons-io:commons-io:2.15.1")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.run<JavaExec> {
    jvmArgs = listOf(
        "--add-exports", "java.desktop/com.apple.laf=ALL-UNNAMED",
        "--add-exports", "java.desktop/com.apple.eawt=ALL-UNNAMED"
    )
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.xerohero.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}