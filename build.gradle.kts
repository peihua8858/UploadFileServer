import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2021.0.4"
extra["awsSpringVersion"]= "2.4.2"
dependencies {
//    implementation(files("libs/aws-java-sdk-1.12.328.jar"))
//    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation ("software.amazon.awssdk:bom:2.18.1")
//    implementation ("software.amazon.awssdk:s3:2.18.1")
    // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
    implementation("com.amazonaws:aws-java-sdk:1.12.328")
    implementation("com.google.code.gson:gson:2.10")
//    implementation ("org.springframework.cloud:spring-cloud-starter-aws")
//    implementation ("org.springframework.cloud:spring-cloud-starter-aws-messaging")
//    implementation ("org.springframework.cloud:spring-cloud-starter-aws-parameter-store-config")
}

dependencyManagement {
    imports {
//        mavenBom( "io.awspring.cloud:spring-cloud-aws-dependencies:${property("awsSpringVersion")}")
//        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
