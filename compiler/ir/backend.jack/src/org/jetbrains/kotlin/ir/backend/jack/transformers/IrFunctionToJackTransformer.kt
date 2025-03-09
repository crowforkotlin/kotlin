/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
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
        val localCount = declaration.body?.statements?.filterIsInstance<IrVariableImpl>()?.count() ?: 0
        declaration.valueParameters.forEachIndexed { index, irValueParameter ->
            context.addArgument(irValueParameter, index)
        }
        context.writeCode("function $parentClass.$functionName $localCount")
        declaration.body?.accept(IrElementToJackStatementTransformer(), context)
        context.clearArgument()
        context.clearLocalVariables()
    }
}