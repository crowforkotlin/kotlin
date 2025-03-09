/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.isElseBranch

class IrElementToJackExpressionTransformer : BaseIrElementToJackTransformer {

    override fun visitConst(expression: IrConst, context: JackGenerationContext) {
        super.visitConst(expression, context)
        context.writeCode("push constant ${expression.value}")
    }

    override fun visitExpressionBody(body: IrExpressionBody, data: JackGenerationContext) {
        super.visitExpressionBody(body, data)
        println("here visitExpressionBody")
    }

    override fun visitStringConcatenation(expression: IrStringConcatenation, data: JackGenerationContext) {
        super.visitStringConcatenation(expression, data)
        println("here visitStringConcatenation")
    }

    override fun visitConstructorCall(expression: IrConstructorCall, data: JackGenerationContext) {
        super.visitConstructorCall(expression, data)
        println("here visitConstructorCall")
    }

    override fun visitCall(expression: IrCall, data: JackGenerationContext) {
        super.visitCall(expression, data)
        println("here visitCall")
        translateCall(expression, this, data)
    }

    override fun visitSetValue(expression: IrSetValue, data: JackGenerationContext) {
        super.visitSetValue(expression, data)
    }

    override fun visitGetValue(expression: IrGetValue, context: JackGenerationContext) {
        super.visitGetValue(expression, context)
        val owner = expression.symbol.owner
        context.getValue(owner)
        println("here get value")
    }

    override fun visitWhen(expression: IrWhen, context: JackGenerationContext) {
        super.visitWhen(expression, context)
        println("inner visit when")
    }
}