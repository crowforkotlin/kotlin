/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class IrElementToJackStatementTransformer : IrElementVisitorVoid {
    override fun visitBlockBody(body: IrBlockBody) {
        super.visitBlockBody(body)
        println("here visitBlockBody")
        body.statements.map { it.accept(this, null) }
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) {
        super.visitReturnableBlock(expression)
        println("here visitReturnableBlock")
    }

    override fun visitBlock(expression: IrBlock) {
        super.visitBlock(expression)
        println("here visitBlock")
    }

    override fun visitExpression(expression: IrExpression) {
        super.visitExpression(expression)
        println("here visitExpression")
        expression.accept(IrElementToJackExpressionTransformer(), null)
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression) {
        super.visitFunctionExpression(expression)
        println("here visitFunctionExpression")
    }

    override fun visitReturn(expression: IrReturn) {
        super.visitReturn(expression)
        println("here visitReturn")
    }
}