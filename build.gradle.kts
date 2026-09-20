import io.papermc.hangarpublishplugin.model.Platforms

plugins {
	`java-library`
	alias(libs.plugins.spotless)
	alias(libs.plugins.lombok)
	alias(libs.plugins.runpaper)
	alias(libs.plugins.shadow)
	alias(libs.plugins.minotaur)
	alias(libs.plugins.hangar)
}

val versions = listOf(libs.versions.minecraft.get())

repositories {
	mavenCentral()
	maven("https://repo.codemc.io/repository/maven-releases/")
	maven("https://repo.papermc.io/repository/maven-public/")
	// Tests need the same bundled compatibility classes as the installed plugin.
	ivy {
		name = "packetEventsReleases"
		url = uri("https://github.com/retrooper/packetevents/releases/download")
		patternLayout {
			artifact("v[revision]/packetevents-spigot-[revision].[ext]")
		}
		metadataSources { artifact() }
		content { includeModule("com.github.retrooper", "packetevents-runtime") }
	}
}

val packetEventsRuntime = configurations.create("packetEventsRuntime") {
	isCanBeConsumed = false
}

dependencies {
	compileOnly(libs.paper)

	compileOnly(libs.guice)
	compileOnly(libs.classgraph)

	// plugin dependencies
	compileOnly(libs.luckperms)
	compileOnly(libs.packetevents.asProvider())

	implementation(libs.bstats)

	testImplementation(libs.paper)
	testImplementation(libs.packetevents.asProvider())
	packetEventsRuntime(libs.packetevents.runtime)
	testImplementation(platform(libs.junit))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation(libs.mockito)
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

spotless {
	java {
		target("**/*.java")
		googleJavaFormat(libs.versions.googlejavaformat.get())
		removeUnusedImports()
		formatAnnotations()
		trimTrailingWhitespace()
		endWithNewline()
	}
	format("misc") {
		target("*.gradle", "*.gradle.kts", "*.md", ".gitignore")

		trimTrailingWhitespace()
		leadingSpacesToTabs()
		endWithNewline()
	}
}

tasks {
	test {
		useJUnitPlatform()
		// Paper's Adventure classes must take precedence over the plugin's bundled fallback.
		classpath += packetEventsRuntime
	}
	runServer {
		minecraftVersion(libs.versions.minecraft.get())
	}
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(25)
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name()
		val properties = mapOf(
			"version" to project.version,
			"minecraft" to libs.versions.minecraft.get(),
			"guice" to libs.guice.get().toString(),
			"classgraph" to libs.classgraph.get().toString(),
		)
		inputs.properties(properties)
		filesMatching(listOf("paper-plugin.yml", "libraries.properties")) {
			expand(properties)
		}
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name()
	}
	assemble {
		dependsOn(shadowJar)
	}
	jar {
		enabled = false
	}
	shadowJar {
		archiveClassifier.set("")
		relocate("org.bstats", "de.rexlmanu.fairytab.dependencies.bstats")
		from(file("LICENSE"))

		exclude("META-INF/NOTICE", "META-INF/maven/**", "META-INF/**.kotlin_module")
		minimize()
	}
}

tasks.getByName("modrinth").dependsOn(tasks.modrinthSyncBody)

modrinth {
	token.set(System.getenv("MODRINTH_TOKEN"))
	projectId.set("fairytab")

	versionNumber.set(rootProject.version.toString())
	versionName.set("FairyTab ${rootProject.version}")
	versionType.set("release")

	syncBodyFrom.set(rootProject.file("README.md").readText())

	uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
	gameVersions.addAll(versions)
	loaders.addAll(listOf("paper", "purpur", "folia"))
	changelog.set(System.getenv("MODRINTH_CHANGELOG"))
	dependencies {
		required.project("luckperms")
		required.project("packetevents")
	}
}

hangarPublish {
	publications.register("plugin") {
		version.set(project.version as String)
		id.set("fairytab")
		channel.set("Release")
		changelog.set(System.getenv("HANGAR_CHANGELOG"))
		apiKey.set(System.getenv("HANGAR_TOKEN"))

		// register platforms
		platforms {
			register(Platforms.PAPER) {
				jar.set(layout.buildDirectory.file("libs/FairyTab-${rootProject.version}.jar"))
				platformVersions.set(versions)
			}
		}
	}
}
