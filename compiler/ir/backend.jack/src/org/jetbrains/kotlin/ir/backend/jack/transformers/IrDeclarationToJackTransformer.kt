/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class IrDeclarationToJackTransformer: IrElementVisitorVoid {
    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        println("here visitClass")
        JackClassGenerator(declaration).generate()
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        super.visitSimpleFunction(declaration)
        println("here visitSimpleFunction")
    }

    override fun visitConstructor(declaration: IrConstructor) {
        super.visitConstructor(declaration)
        println("here visitConstructor")
    }
}