/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jack

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.ExitCode.COMPILATION_ERROR
import org.jetbrains.kotlin.cli.common.ExitCode.OK
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.config.getModuleNameForSource
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirViaLightTree
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.ir.backend.js.MainModule
import org.jetbrains.kotlin.ir.backend.js.ModulesStructure
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.KotlinPaths
import java.io.File

class K2JackCompiler() : CLICompiler<K2JSCompilerArguments>() {
    class K2JSCompilerPerformanceManager : CommonCompilerPerformanceManager("Kotlin to Jack Compiler")

    override val defaultPerformanceManager: CommonCompilerPerformanceManager = K2JSCompilerPerformanceManager()
    override fun createMetadataVersion(versionArray: IntArray): BinaryVersion {
        return KlibMetadataVersion(*versionArray)
    }

    override fun createArguments(): K2JSCompilerArguments {
        return K2JSCompilerArguments()
    }

    override fun executableScriptFileName(): String {
        return "kotlinc-jack"
    }

    override fun MutableList<String>.addPlatformOptions(arguments: K2JSCompilerArguments) {
    }

    override fun doExecute(
        arguments: K2JSCompilerArguments,
        configuration: CompilerConfiguration,
        rootDisposable: Disposable,
        paths: KotlinPaths?,
    ): ExitCode {
        println("execute Kotlinc jack2")
        val outputName = arguments.moduleName
        val commonSourcesArray = arguments.commonSources
        val commonSources = commonSourcesArray?.toSet() ?: emptySet()
        val hmppCliModuleStructure = configuration.get(CommonConfigurationKeys.HMPP_MODULE_STRUCTURE)
        for (arg in arguments.freeArgs) {
            configuration.addKotlinSourceRoot(arg, commonSources.contains(arg), hmppCliModuleStructure?.getModuleNameForSource(arg))
        }
        val environmentForJS = KotlinCoreEnvironment.createForProduction(rootDisposable, configuration, EnvironmentConfigFiles.JS_CONFIG_FILES)
        val mainModule = MainModule.SourceFiles(environmentForJS.getSourceFiles())

        val libraries: List<String> = configureLibraries(arguments.libraries) + listOfNotNull(arguments.includes)
        val friendLibraries = listOf<String>()
        val moduleStructure = ModulesStructure(environmentForJS.project, mainModule, configuration, libraries, friendLibraries)
        val messageCollector = configuration.getNotNull(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter(messageCollector)
        val groupedSources = collectSources(configuration, environmentForJS.project, messageCollector)
        val lookupTracker = configuration.get(CommonConfigurationKeys.LOOKUP_TRACKER) ?: LookupTracker.DO_NOTHING

        if (outputName == null) {
            messageCollector.report(ERROR, "IR: Specify output name via -ir-output-name", null)
            return COMPILATION_ERROR
        }

        val configurationJs = environmentForJS.configuration
        val moduleName = arguments.irModuleName ?: outputName
        configurationJs.put(CommonConfigurationKeys.MODULE_NAME, moduleName)

        val firOutput = compileModulesToAnalyzedFirWithLightTree(
            moduleStructure = moduleStructure,
            groupedSources = groupedSources,
            // TODO: Only pass groupedSources, because
            //  we will need to have them separated again
            //  in createSessionsForLegacyMppProject anyway
            ktSourceFiles = groupedSources.commonSources + groupedSources.platformSources,
            libraries = libraries,
            friendLibraries = friendLibraries,
            diagnosticsReporter = diagnosticsReporter,
            incrementalDataProvider = configuration[JSConfigurationKeys.INCREMENTAL_DATA_PROVIDER],
            lookupTracker = lookupTracker,
            useWasmPlatform = arguments.wasm,
        )
        val fir2IrActualizedResult = transformFirToIr(moduleStructure, firOutput.output, diagnosticsReporter)
        println("ir2firActualizedResult: $fir2IrActualizedResult")
        return OK
    }


    private fun configureLibraries(libraryString: String?): List<String> =
        libraryString?.splitByPathSeparator() ?: emptyList()

    private fun String.splitByPathSeparator(): List<String> {
        return this.split(File.pathSeparator.toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
            .filterNot { it.isEmpty() }
    }

    override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration,
        arguments: K2JSCompilerArguments,
        services: Services,
    ) {
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            doMain(K2JackCompiler(), args)
        }
    }
}