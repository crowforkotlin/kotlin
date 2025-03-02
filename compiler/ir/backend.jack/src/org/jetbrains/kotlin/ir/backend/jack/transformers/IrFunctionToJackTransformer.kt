/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

class IrFunctionToJackTransformer : IrElementVisitorVoid {

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        super.visitSimpleFunction(declaration)
        val parentClass = declaration.parent as? IrClass
        translateFunction(parentClass, declaration)
    }

    private val objectFunctions = listOf("hashCode", "equals", "toString")

    private fun translateFunction(irClass: IrClass?, declaration: IrSimpleFunction) {
        if (objectFunctions.contains(declaration.name.asString())) {
            return
        }
        val parentClass = irClass?.name
        val functionName = declaration.name
        val argumentCount = declaration.valueParameters.size
        println("function $parentClass.$functionName $argumentCount")
        declaration.body?.accept(IrElementToJackStatementTransformer(), null)
    }
}