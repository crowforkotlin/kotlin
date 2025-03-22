/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class JackClassGenerator(private val irClass: IrClass) {
    fun generate(context: JackGenerationContext) {
        for (declaration in irClass.declarations) {
            when (declaration) {
                is IrSimpleFunction -> {
                    declaration.accept(IrFunctionToJackTransformer(), context)
                }
            }
        }
    }
}