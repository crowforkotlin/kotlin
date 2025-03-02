/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol

fun translateCall(
    expression: IrCall,
    transformer: IrElementToJackExpressionTransformer,
) {
    println("here translateCall")
    translateCallArguments(expression, transformer)
}

fun translateCallArguments(
    expression: IrMemberAccessExpression<IrFunctionSymbol>,
    transformer: IrElementToJackExpressionTransformer,
){
    val size = expression.valueArgumentsCount
    for (i in 0 until size) {
        val argument = expression.getValueArgument(i)
        println("argument: $argument")
        argument?.accept(transformer, null)
    }
}