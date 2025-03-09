/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol

fun translateCall(
    expression: IrCall,
    transformer: IrElementToJackExpressionTransformer,
    context: JackGenerationContext,
) {
    val dispatchReceiver = expression.dispatchReceiver
    dispatchReceiver?.accept(transformer, context)
    translateCallArguments(expression, transformer, context)
    when (expression.origin) {
        IrStatementOrigin.PLUS -> {
            context.writeCode("add")
        }
        IrStatementOrigin.MUL -> {
            context.writeCode("call Math.multiply 2")
        }
        IrStatementOrigin.MINUS -> {
            context.writeCode("sub")
        }
        IrStatementOrigin.LTEQ -> {
            context.writeCode("lt")
            translateCallArguments(expression, transformer, context)
            context.writeCode("eq")
            context.writeCode("or")
        }
        IrStatementOrigin.LT -> {
            context.writeCode("lt")
        }
        else -> {
            if (expression.symbol.owner.name.asString() == "println") {
                context.writeCode("call Output.printInt 1")
                context.writeCode("pop temp 0")
            } else {
                val className = (expression.symbol.owner.parent as IrClassImpl).name
                val functionName = expression.symbol.owner.name.asString()
                context.writeCode("call ${className}.${functionName} ${expression.valueArgumentsCount}")
            }
        }
    }
}

fun translateCallArguments(
    expression: IrMemberAccessExpression<IrFunctionSymbol>,
    transformer: IrElementToJackExpressionTransformer,
    context: JackGenerationContext,
) {
    val size = expression.valueArgumentsCount
    for (i in 0 until size) {
        val argument = expression.getValueArgument(i)
        argument?.accept(transformer, context)
    }
}