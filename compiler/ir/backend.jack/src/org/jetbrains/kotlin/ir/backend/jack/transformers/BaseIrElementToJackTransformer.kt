/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.transformers

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.jack.utils.JackGenerationContext
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

interface BaseIrElementToJackTransformer : IrElementVisitor<Unit, JackGenerationContext> {
    override fun visitElement(element: IrElement, context: JackGenerationContext) {
        // TODO
    }
}