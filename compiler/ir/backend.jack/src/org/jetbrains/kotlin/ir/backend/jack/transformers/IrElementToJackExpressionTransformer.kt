/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class IrElementToJackExpressionTransformer : IrElementVisitorVoid {

    override fun visitConst(expression: IrConst) {
        super.visitConst(expression)
        println("here visitConst")
    }

    override fun visitExpressionBody(body: IrExpressionBody) {
        super.visitExpressionBody(body)
        println("here visitExpressionBody")
    }

    override fun visitStringConcatenation(expression: IrStringConcatenation) {
        super.visitStringConcatenation(expression)
        println("here visitStringConcatenation")
    }

    override fun visitConstructorCall(expression: IrConstructorCall) {
        super.visitConstructorCall(expression)
        println("here visitConstructorCall")
    }

    override fun visitCall(expression: IrCall) {
        super.visitCall(expression)
        println("here visitCall")
        translateCall(expression, this)
    }
}