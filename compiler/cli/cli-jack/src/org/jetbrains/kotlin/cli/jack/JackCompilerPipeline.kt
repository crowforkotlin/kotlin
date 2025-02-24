/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jack

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.BinaryModuleData
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.ModuleCompilerAnalyzedOutput
import org.jetbrains.kotlin.fir.pipeline.buildResolveAndCheckFirViaLightTree
import org.jetbrains.kotlin.fir.pipeline.runPlatformCheckers
import org.jetbrains.kotlin.fir.session.KlibIcData
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.js.IncrementalDataProvider
import org.jetbrains.kotlin.ir.backend.js.ModulesStructure
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.platform.wasm.WasmPlatforms
import org.jetbrains.kotlin.platform.wasm.WasmTarget
import org.jetbrains.kotlin.wasm.config.WasmConfigurationKeys
import java.nio.file.Paths

inline fun <F> compileModuleToAnalyzedFir(
    moduleStructure: ModulesStructure,
    files: List<F>,
    libraries: List<String>,
    friendLibraries: List<String>,
    incrementalDataProvider: IncrementalDataProvider?,
    lookupTracker: LookupTracker?,
    noinline isCommonSource: (F) -> Boolean,
    noinline fileBelongsToModule: (F, String) -> Boolean,
    buildResolveAndCheckFir: (FirSession, List<F>) -> ModuleCompilerAnalyzedOutput,
    useWasmPlatform: Boolean,
): List<ModuleCompilerAnalyzedOutput> {
    // FIR
    val extensionRegistrars = FirExtensionRegistrar.getInstances(moduleStructure.project)

    val mainModuleName = moduleStructure.compilerConfiguration.get(CommonConfigurationKeys.MODULE_NAME)!!
    val escapedMainModuleName = Name.special("<$mainModuleName>")
    val platform = if (useWasmPlatform) {
        when (moduleStructure.compilerConfiguration.get(WasmConfigurationKeys.WASM_TARGET, WasmTarget.JS)) {
            WasmTarget.JS -> WasmPlatforms.wasmJs
            WasmTarget.WASI -> WasmPlatforms.wasmWasi
        }
    } else JsPlatforms.defaultJsPlatform
    val binaryModuleData = BinaryModuleData.initialize(escapedMainModuleName, platform)
    val dependencyList = DependencyListForCliModule.build(binaryModuleData) {
        dependencies(libraries.map { Paths.get(it).toAbsolutePath() })
        friendDependencies(friendLibraries.map { Paths.get(it).toAbsolutePath() })
        // TODO: !!! dependencies module data?
    }

    val resolvedLibraries = moduleStructure.allDependencies

    val sessionsWithSources = prepareJsSessions(
        files, moduleStructure.compilerConfiguration, escapedMainModuleName,
        resolvedLibraries, dependencyList, extensionRegistrars,
        isCommonSource = isCommonSource,
        fileBelongsToModule = fileBelongsToModule,
        lookupTracker,
        icData = null,
    )


    val outputs = sessionsWithSources.map {
        buildResolveAndCheckFir(it.session, it.files)
    }

    return outputs
}

fun compileModulesToAnalyzedFirWithLightTree(
    moduleStructure: ModulesStructure,
    groupedSources: GroupedKtSources,
    ktSourceFiles: List<KtSourceFile>,
    libraries: List<String>,
    friendLibraries: List<String>,
    diagnosticsReporter: BaseDiagnosticsCollector,
    incrementalDataProvider: IncrementalDataProvider?,
    lookupTracker: LookupTracker?,
    useWasmPlatform: Boolean,
): AnalyzedFirOutput {
    val output = compileModuleToAnalyzedFir(
        moduleStructure,
        ktSourceFiles,
        libraries,
        friendLibraries,
        incrementalDataProvider,
        lookupTracker,
        isCommonSource = { groupedSources.isCommonSourceForLt(it) },
        fileBelongsToModule = { file, it -> groupedSources.fileBelongsToModuleForLt(file, it) },
        buildResolveAndCheckFir = { session, files ->
            buildResolveAndCheckFirViaLightTree(session, files, diagnosticsReporter, null)
        },
        useWasmPlatform = useWasmPlatform,
    )
    output.runPlatformCheckers(diagnosticsReporter)
    return AnalyzedFirOutput(output)
}

open class AnalyzedFirOutput(val output: List<ModuleCompilerAnalyzedOutput>) {
    protected open fun checkSyntaxErrors(messageCollector: MessageCollector) = false

    fun reportCompilationErrors(
        moduleStructure: ModulesStructure,
        diagnosticsReporter: BaseDiagnosticsCollector,
        messageCollector: MessageCollector,
    ): Boolean {
        if (checkSyntaxErrors(messageCollector) || diagnosticsReporter.hasErrors) {
            println("reportCompilationErrors")
//            reportCollectedDiagnostics(moduleStructure.compilerConfiguration, diagnosticsReporter, messageCollector)
            return true
        }

        return false
    }
}