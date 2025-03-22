/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyClass
import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.isElseBranch
import org.jetbrains.kotlin.js.backend.ast.JsEmpty
import org.jetbrains.kotlin.js.backend.ast.JsWhile
import org.jetbrains.kotlin.utils.toSmartList

class IrElementToJackStatementTransformer : BaseIrElementToJackTransformer {
    override fun visitBlockBody(body: IrBlockBody, data: JackGenerationContext) {
        super.visitBlockBody(body, data)
        println("here visitBlockBody")
        body.statements.map { it.accept(this, data) }
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock, data: JackGenerationContext) {
        super.visitReturnableBlock(expression, data)
        println("here visitReturnableBlock")
    }

    override fun visitBlock(expression: IrBlock, data: JackGenerationContext) {
        super.visitBlock(expression, data)
        println("here visitBlock")
        expression.statements.map { it.accept(this, data) }
    }

    override fun visitWhen(expression: IrWhen, context: JackGenerationContext) {
        println("here visitWhen")
        if (expression.origin == IrStatementOrigin.IF) {
            val elseLabel = context.getLabelName()
            expression.branches.forEach { br ->
                var branchLabel = ""
                if (!isElseBranch(br)) {
                    br.condition.accept(IrElementToJackExpressionTransformer(), context)
                    context.writeCode("not")
                    branchLabel = context.getLabelName()
                    context.writeCode("if-goto $branchLabel")
                }
                br.result.accept(this, context)
                if (!isElseBranch(br)) {
                    context.writeCode("goto $elseLabel")
                    context.writeCode("label $branchLabel")
                }
            }
            context.writeCode("label $elseLabel")
        }
    }

    override fun visitWhileLoop(loop: IrWhileLoop, context: JackGenerationContext) {
        val whileStartLabel = context.getLabelName()
        val whileEndLabel = context.getLabelName()
        context.writeCode("label $whileStartLabel")
        loop.condition.accept(IrElementToJackExpressionTransformer(), context)
        context.writeCode("not")
        context.writeCode("if-goto $whileEndLabel")
        loop.body?.accept(this, context)
        context.writeCode("goto $whileStartLabel")
        context.writeCode("label $whileEndLabel")
    }

    override fun visitExpression(expression: IrExpression, data: JackGenerationContext) {
        println("here visitExpression")
        expression.accept(IrElementToJackExpressionTransformer(), data)
    }

    override fun visitVariable(declaration: IrVariable, context: JackGenerationContext) {
        println("here visitVariable")
        val localIndex = context.getLocalIndex(declaration)
        val value = declaration.initializer
        value?.accept(IrElementToJackExpressionTransformer(), context)
        context.writeCode("pop local $localIndex")
    }

    override fun visitFunctionExpression(expression: IrFunctionExpression, data: JackGenerationContext) {
        super.visitFunctionExpression(expression, data)
        println("here visitFunctionExpression")
    }

    override fun visitReturn(expression: IrReturn, context: JackGenerationContext) {
        val type = expression.value.type.classFqName
        if (type?.asString() == "kotlin.Unit") {
            context.writeCode("push constant 0")
        } else {
            expression.value.accept(this, context)
        }
        context.writeCode("return")
    }
}