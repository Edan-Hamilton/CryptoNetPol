plugins {
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.serialization") version "2.0.0"
}; repositories { mavenCentral() }

dependencies{
	implementation("io.ktor:ktor-client-cio:2.3.10")
	implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
	implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
	implementation("org.slf4j:slf4j-nop:2.0.13")
	implementation("com.fleeksoft.ksoup:ksoup:0.1.2")
	implementation("com.fleeksoft.ksoup:ksoup-network:0.1.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
}

sourceSets.main{kotlin.srcDirs("src"); resources.srcDirs("res")}
