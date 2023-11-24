@file:Suppress("UNUSED_VARIABLE")

import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform")
    `maven-publish`
    signing
}

description = "Kotlin Test Library"
base.archivesName = "kotlin-test"

configureJvmToolchain(JdkMajorVersion.JDK_1_8)

val kotlinTestCapability = "$group:${base.archivesName.get()}:$version" // add to variants with explicit capabilities when the default one is needed, too
val baseCapability = "$group:kotlin-test-framework:$version"
val implCapability = "$group:kotlin-test-framework-impl:$version"

enum class JvmTestFramework {
    JUnit,
    JUnit5,
    TestNG;

    fun lowercase() = name.lowercase()
}
val jvmTestFrameworks = JvmTestFramework.values().toList()

kotlin {

    jvm {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.empty() // avoid common options set from the root project
                    }
                }
            }
            val main by getting
            val test by getting
            configureJava9Compilation(
                "kotlin.test",
                listOf(main.output.allOutputs),
                main.configurations.compileDependencyConfiguration,
                project.sourceSets.create("jvmJava9") {
                    java.srcDir("jvm/src/java9/java")
                }.name,
            )
            jvmTestFrameworks.forEach { framework ->
                val frameworkMain = create("$framework") {
                    associateWith(main)
                }
                create("${framework}Test") {
                    associateWith(frameworkMain)
                }
                val frameworkJava9SourceSet = project.sourceSets.create("jvm${framework}Java9") {
                    java.srcDir("${framework.lowercase()}/src/java9/java")
                }
                configureJava9Compilation(
                    "kotlin.test.${framework.lowercase()}",
                    listOf(frameworkMain.output.allOutputs),
                    frameworkMain.configurations.compileDependencyConfiguration,
                    frameworkJava9SourceSet.name,
                )
                val java9CompileOnly = configurations[frameworkJava9SourceSet.compileOnlyConfigurationName]
                project.dependencies {
                    java9CompileOnly(project)
                    if (framework == JvmTestFramework.TestNG) {
                        java9CompileOnly("org.testng:testng:7.0.0")
                    }
                }
            }
            test.associateWith(getByName("JUnit"))
        }
    }
    js {
        if (!kotlinBuildProperties.isTeamcityBuild) {
            browser {}
        }
        nodejs {}
        compilations["main"].compilerOptions.configure {
            freeCompilerArgs.add("-Xir-module-name=kotlin-test")
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        compilations["main"].compilerOptions.configure {
            freeCompilerArgs.add("-Xir-module-name=kotlin-test")
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        compilations["main"].compilerOptions.configure {
            freeCompilerArgs.add("-Xir-module-name=kotlin-test")
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    optIn.add("kotlin.contracts.ExperimentalContracts")
                    freeCompilerArgs.addAll(
                        "-Xallow-kotlin-package",
                        "-Xexpect-actual-classes",
                    )
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlinStdlib())
            }
        }
        val annotationsCommonMain by creating {
            dependsOn(commonMain)
            kotlin.srcDir("annotations-common/src/main/kotlin")
        }
        val assertionsCommonMain by creating {
            dependsOn(commonMain)
            kotlin.srcDir("common/src/main/kotlin")
        }
        val commonTest by getting {
            kotlin.srcDir("annotations-common/src/test/kotlin")
            kotlin.srcDir("common/src/test/kotlin")
        }
        val jvmMain by getting {
            dependsOn(assertionsCommonMain)
            kotlin.srcDir("jvm/src/main/kotlin")
        }
        val jvmTest by getting {
            kotlin.srcDir("jvm/src/test/kotlin")
        }
        val jvmJUnit by getting {
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("junit/src/main/kotlin")
            resources.srcDir("junit/src/main/resources")
            dependencies {
                api("junit:junit:4.13.2")
            }
        }
        val jvmJUnitTest by getting {
            kotlin.srcDir("junit/src/test/kotlin")
        }
        val jvmJUnit5 by getting {
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("junit5/src/main/kotlin")
            resources.srcDir("junit5/src/main/resources")
            dependencies {
                compileOnly("org.junit.jupiter:junit-jupiter-api:5.0.0")
            }
        }
        val jvmJUnit5Test by getting {
            kotlin.srcDir("junit5/src/test/kotlin")
            dependencies {
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
        val jvmTestNG by getting {
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("testng/src/main/kotlin")
            resources.srcDir("testng/src/main/resources")
            dependencies {
                api("org.testng:testng:6.13.1")
            }
        }
        val jvmTestNGTest by getting {
            kotlin.srcDir("testng/src/test/kotlin")
        }
        val jsMain by getting {
            dependsOn(assertionsCommonMain)
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("js/src/main/kotlin")
        }
        val jsTest by getting {
            kotlin.srcDir("js/src/test/kotlin")
        }
        val wasmCommonMain by creating {
            dependsOn(assertionsCommonMain)
            dependsOn(annotationsCommonMain)
            kotlin.srcDir("wasm/src/main/kotlin")
        }
        val wasmJsMain by getting {
            dependsOn(wasmCommonMain)
            kotlin.srcDir("wasm/js/src/main/kotlin")
        }
        val wasmWasiMain by getting {
            dependsOn(wasmCommonMain)
            kotlin.srcDir("wasm/wasi/src/main/kotlin")
        }
    }
}

tasks {
    val allMetadataJar by existing(Jar::class) {
        archiveClassifier = "all"
    }
    val jvmJar by existing(Jar::class) {
        archiveAppendix = null
        from(project.sourceSets["jvmJava9"].output)
        manifestAttributes(manifest, "Test", multiRelease = true)
    }
    val jvmSourcesJar by existing(Jar::class) {
        archiveAppendix = null
        kotlin.sourceSets["annotationsCommonMain"].let { sourceSet ->
            into(sourceSet.name) {
                from(sourceSet.kotlin)
            }
        }
    }
    val jvmJarTasks = jvmTestFrameworks.map { framework ->
        named("jvm${framework}Jar", Jar::class) {
            archiveBaseName = base.archivesName
            archiveAppendix = framework.lowercase()
            from(project.sourceSets["jvm${framework}Java9"].output)
            manifestAttributes(manifest, "Test", multiRelease = true)
            manifest.attributes("Implementation-Title" to "${archiveBaseName.get()}-${archiveAppendix.get()}")
        }
    }
    val jvmSourcesJarTasks = jvmTestFrameworks.map { framework ->
        register("jvm${framework}SourcesJar", Jar::class) {
            archiveAppendix = framework.lowercase()
            archiveClassifier = "sources"
            kotlin.jvm().compilations[framework.name].allKotlinSourceSets.forEach {
                from(it.kotlin.sourceDirectories) { into(it.name) }
                from(it.resources.sourceDirectories) { into(it.name) }
            }
        }
    }
    val jsJar by existing(Jar::class) {
        manifestAttributes(manifest, "Test")
        manifest.attributes("Implementation-Title" to "${archiveBaseName.get()}-${archiveAppendix.get()}")
    }
    val wasmJsJar by existing(Jar::class) {
        manifestAttributes(manifest, "Test")
        manifest.attributes("Implementation-Title" to "${archiveBaseName.get()}-${archiveAppendix.get()}")
    }
    val wasmWasiJar by existing(Jar::class) {
        manifestAttributes(manifest, "Test")
        manifest.attributes("Implementation-Title" to "${archiveBaseName.get()}-${archiveAppendix.get()}")
    }
    val assemble by existing {
        dependsOn(jvmJarTasks)
    }

    val jvmTestTasks = jvmTestFrameworks.map { framework ->
        register("jvm${framework}Test", Test::class) {
            group = "verification"
            val compilation = kotlin.jvm().compilations["${framework}Test"]
            classpath = compilation.runtimeDependencyFiles + compilation.output.allOutputs
            testClassesDirs = compilation.output.classesDirs
            when (framework) {
                JvmTestFramework.JUnit -> useJUnit()
                JvmTestFramework.JUnit5 -> useJUnitPlatform()
                JvmTestFramework.TestNG -> useTestNG()
            }
        }
    }
    val allTests by existing {
        dependsOn(jvmTestTasks)
    }
}

configurations {
    val metadataApiElements by getting
    metadataApiElements.outgoing.capability(kotlinTestCapability)

    for (framework in jvmTestFrameworks) {
        val frameworkCapability = "$group:kotlin-test-framework-${framework.lowercase()}:$version"
        metadataApiElements.outgoing.capability(frameworkCapability)

        val runtimeDeps = create("jvm${framework}RuntimeDependencies") {
            isCanBeResolved = true
            isCanBeConsumed = false
        }
        val (apiElements, runtimeElements, sourcesElements) = listOf(KotlinUsages.KOTLIN_API, KotlinUsages.KOTLIN_RUNTIME, KotlinUsages.KOTLIN_SOURCES).map { usage ->
            val name = "jvm$framework${usage.substringAfter("kotlin-").replaceFirstChar { it.uppercase() }}Elements"
            create(name) {
                isCanBeResolved = false
                isCanBeConsumed = true
                outgoing.capability(baseCapability)
                outgoing.capability(frameworkCapability)
                attributes {
                    attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
                    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                    attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
                    when (usage) {
                        KotlinUsages.KOTLIN_SOURCES -> {
                            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
                            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
                            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                        }
                        KotlinUsages.KOTLIN_API -> {
                            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                            extendsFrom(getByName("jvm${framework}Api"))
                        }
                        KotlinUsages.KOTLIN_RUNTIME -> {
                            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                            runtimeDeps.extendsFrom(getByName("jvm${framework}Api"))
                            runtimeDeps.extendsFrom(getByName("jvm${framework}Implementation"))
                            runtimeDeps.extendsFrom(getByName("jvm${framework}RuntimeOnly"))
                            extendsFrom(runtimeDeps)
                        }
                        else -> error(usage)
                    }
                }
            }
        }
        dependencies {
            apiElements(project)
            runtimeDeps(project)
            when (framework) {
                JvmTestFramework.JUnit -> {}
                JvmTestFramework.JUnit5 -> {
                    apiElements("org.junit.jupiter:junit-jupiter-api:5.6.3")
                    runtimeDeps("org.junit.jupiter:junit-jupiter-engine:5.6.3")
                }
                JvmTestFramework.TestNG -> {}
            }
        }
        artifacts {
            add(apiElements.name, tasks.named<Jar>("jvm${framework}Jar"))
            add(runtimeElements.name, tasks.named<Jar>("jvm${framework}Jar"))
            add(sourcesElements.name, tasks.named<Jar>("jvm${framework}SourcesJar"))
        }
    }

    for (configurationName in listOf("kotlinTestCommon", "kotlinTestAnnotationsCommon")) {
        val legacyConfigurationDeps = create("${configurationName}Dependencies") {
            isCanBeResolved = true
            isCanBeConsumed = false
        }
        val legacyConfiguration = create("${configurationName}Elements") {
            isCanBeResolved = false
            isCanBeConsumed = false
            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            }
            extendsFrom(legacyConfigurationDeps)
        }
        dependencies {
            legacyConfigurationDeps(project)
        }
    }
}
