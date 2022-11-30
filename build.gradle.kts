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
    maven("https://jitpack.io" )
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
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okio:okio:3.2.0")
    implementation ("com.github.peihua8858:KotlinUtil:1.0.0")
    implementation ("com.github.peihua8858:GsonAdapter-java:1.0.2")
    //阿里云短信群发sdk
    implementation ("com.aliyun:aliyun-java-sdk-core:4.6.1")
//    implementation 'com.aliyun:dyvmsapi20170525:2.1.4'
    implementation ("com.aliyun:dysmsapi20170525:2.0.18")
//    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-io:commons-io:2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
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
