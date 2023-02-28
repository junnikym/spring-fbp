//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//plugins {
//	id("org.springframework.boot") version "2.7.8"
//	id("io.spring.dependency-management") version "1.0.15.RELEASE"
//	kotlin("jvm") version "1.6.21"
//	kotlin("plugin.spring") version "1.6.21"
//}
//
//group = "org.junnikym"
//version = "0.0.1-SNAPSHOT"
//java.sourceCompatibility = JavaVersion.VERSION_11
//
//repositories {
//	mavenCentral()
//}
//
//dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
//}
//
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	base
	id("org.springframework.boot") version "2.7.8"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21" apply false
}

java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
	group = "org.junnikym"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
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
}

subprojects {
	apply(plugin = "java")

	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")

	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
}

//dependencies {
//	subprojects.forEach{
//		archives(it)
//	}
//}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	subprojects.forEach{
		implementation(it)
	}
}
