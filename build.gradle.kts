plugins {
    id("java")
    application
}

application {
    mainClass.set("dev.xerohero.Main")
}

group = "dev.xerohero"
version = "1.0.0"  // Using semantic versioning for release

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

// Configure the main JAR task
tasks.jar {
    archiveBaseName.set("JImg2PDF")
    archiveVersion.set("")
    archiveClassifier.set("")
    
    manifest {
        attributes["Main-Class"] = "dev.xerohero.Main"
        attributes["Implementation-Title"] = "JImg2PDF"
        attributes["Implementation-Version"] = archiveVersion
        attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
    }
    
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Create a distribution ZIP with the JAR and README
tasks.register<Zip>("dist") {
    dependsOn("jar")
    
    from("$buildDir/libs/JImg2PDF.jar")
    from("README.md")
    from("LICENSE")
    
    archiveFileName.set("JImg2PDF-${version}.zip")
    destinationDirectory.set(file("$buildDir/distributions"))
}