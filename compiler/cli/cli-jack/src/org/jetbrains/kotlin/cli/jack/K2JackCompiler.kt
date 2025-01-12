/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jack

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.ExitCode.OK
import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.library.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.utils.KotlinPaths

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
        println("execute Kotlinc jack")
        return OK
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