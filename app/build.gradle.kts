plugins {
	application
	//java
	jacoco
	id("io.freefair.lombok") version "8.6"
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.ben-manes.versions") version "0.50.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application { mainClass.set("hexlet.code.demo.AppApplication") }

//java {
	//toolchain {
		//languageVersion = JavaLanguageVersion.of(21)
	//}
//}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("org.mapstruct:mapstruct:1.6.0.Beta1")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0.Beta1")

	// implementation("io.github.wimdeblauwe:error-handling-spring-boot-starter:4.2.0")

	implementation("org.instancio:instancio-junit:3.6.0")
	implementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
	implementation("net.datafaker:datafaker:2.0.2")

	runtimeOnly("com.h2database:h2:2.2.224")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation(platform("org.junit:junit-bom:5.10.1"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
tasks.jacocoTestReport { reports { xml.required.set(true) } }