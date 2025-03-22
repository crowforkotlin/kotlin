/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack

import org.jetbrains.kotlin.ir.backend.jack.transformers.IrFileToJackTransformer
import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class IrModuleToJackTransformer {
    fun generateCode(irModule: IrModuleFragment, outputDirPath: String, outputName: String) {
        println("Generating code for module: ${irModule.name}")
        irModule.files.forEach { file ->
            val context = JackGenerationContext(outputDirPath, outputName)
            file.accept(
                IrFileToJackTransformer(),
                data = context
            )
        }
    }
}