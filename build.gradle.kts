import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
	`java-library`
	alias(libs.plugins.spotless)
	alias(libs.plugins.lombok)
	alias(libs.plugins.runpaper)
	alias(libs.plugins.userdev)
	alias(libs.plugins.shadow)
	alias(libs.plugins.paperyml)
	alias(libs.plugins.minotaur)
	alias(libs.plugins.hangar)
}

val versions = listOf("1.20", "1.20.1");

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
	paperweight.paperDevBundle(libs.versions.minecraft)

	compileOnly(libs.guice)
	compileOnly(libs.classgraph)

	// plugin dependencies
	compileOnly(libs.luckperms)

	implementation(libs.bstats)
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
		indentWithTabs()
		endWithNewline()
	}
}

tasks {
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name()
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name()
	}
	assemble {
		dependsOn(reobfJar)
	}
	jar {
		enabled = false
	}
	shadowJar {
		archiveClassifier.set("")
		relocate("org.bstats", "de.rexlmanu.fairytab.dependencies.bstats")
		from(file("LICENSE"))

		dependencies {
			exclude("META-INF/NOTICE")
			exclude("META-INF/maven/**")
			exclude("META-INF/versions/**")
			exclude("META-INF/**.kotlin_module")
		}
		minimize()
	}
}

paper {
	main = "de.rexlmanu.fairytab.FairyTabPlugin"
	loader = "de.rexlmanu.fairytab.FairyTabLoader"
	website = "https://github.com/rexlManu/FairyTab"
	author = "rexlManu"
	foliaSupported = true
	apiVersion = "1.20"
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
	prefix = "FairyTab"
	serverDependencies {
		register("LuckPerms") {
			load = PaperPluginDescription.RelativeLoadOrder.BEFORE
			required = true
		}
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

	uploadFile.set(buildDir.resolve("libs").resolve("FairyTab-${rootProject.version}.jar"))
	gameVersions.addAll(versions)
	loaders.addAll(listOf("paper", "purpur", "folia"))
	changelog.set(System.getenv("MODRINTH_CHANGELOG"))
	dependencies {
		required.project("luckperms")
	}
}

hangarPublish {
	publications.register("plugin") {
		version.set(project.version as String)
		namespace("rexlManu", "fairytab")
		channel.set("Release")
		changelog.set(System.getenv("HANGAR_CHANGELOG"))
		apiKey.set(System.getenv("HANGAR_TOKEN"))

		// register platforms
		platforms {
			register(Platforms.PAPER) {
				jar.set(buildDir.resolve("libs").resolve("FairyTab-${rootProject.version}.jar"))
				platformVersions.set(versions)
			}
		}
	}
}
