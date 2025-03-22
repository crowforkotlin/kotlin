/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class IrDeclarationToJackTransformer : BaseIrElementToJackTransformer {
    override fun visitClass(declaration: IrClass, data: JackGenerationContext) {
        super.visitClass(declaration, data)
        println("here visitClass")
        JackClassGenerator(declaration).generate(data)
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: JackGenerationContext) {
        super.visitSimpleFunction(declaration, data)
        println("here visitSimpleFunction")
    }

    override fun visitConstructor(declaration: IrConstructor, data: JackGenerationContext) {
        super.visitConstructor(declaration, data)
        println("here visitConstructor")
    }
}