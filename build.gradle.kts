plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.allopen") version "1.9.23"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(platform("$quarkusPlatformGroupId:quarkus-amazon-services-bom:$quarkusPlatformVersion"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-extension-aws")
    implementation("io.opentelemetry.contrib:opentelemetry-aws-xray:1.35.0")
    implementation("io.quarkiverse.amazonservices:quarkus-amazon-sns")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("software.amazon.awssdk:url-connection-client")
    testImplementation("io.rest-assured:kotlin-extensions:5.4.0")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkiverse.amazonservices:quarkus-amazon-sqs")
    testImplementation("io.quarkus:quarkus-junit5")
}

group = "org.acme"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_21.toString()
    kotlinOptions.javaParameters = true
}
//    ./gradlew clean quarkusIntTest -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.test.integration-test-profile=test
