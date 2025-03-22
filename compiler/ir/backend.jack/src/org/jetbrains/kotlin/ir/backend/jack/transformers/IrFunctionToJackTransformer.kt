/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.impl.IrWhileLoopImpl
import org.jetbrains.kotlin.ir.util.statements

class IrFunctionToJackTransformer : BaseIrElementToJackTransformer {

    override fun visitSimpleFunction(declaration: IrSimpleFunction, context: JackGenerationContext) {
        super.visitSimpleFunction(declaration, context)
        val parentClass = declaration.parent as? IrClass
        translateFunction(parentClass, declaration, context)
    }

    private val objectFunctions = listOf("hashCode", "equals", "toString")

    private fun translateFunction(irClass: IrClass?, declaration: IrSimpleFunction, context: JackGenerationContext) {
        if (objectFunctions.contains(declaration.name.asString())) {
            return
        }
        val parentClass = irClass?.name
        val functionName = declaration.name
        val localCount = getLocalCount(declaration.body?.statements ?: emptyList())
        declaration.valueParameters.forEachIndexed { index, irValueParameter ->
            context.addArgument(irValueParameter, index)
        }
        context.writeCode("function $parentClass.$functionName $localCount")
        declaration.body?.accept(IrElementToJackStatementTransformer(), context)
        context.clearArgument()
        context.clearLocalVariables()
    }

    private fun getLocalCount(statements: List<IrStatement>): Int {
        var result = 0
        statements.forEach { item ->
            when (item) {
                is IrVariableImpl -> {
                    result++
                }
                is IrBlock -> {
                    result += getLocalCount(item.statements)
                }
                is IrWhileLoopImpl -> {
                    item.body?.let {
                        result += getLocalCount(listOf(it))
                    }
                }
            }
        }
        return result
    }
}