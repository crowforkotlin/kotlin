/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jack.utils

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import java.io.File

class JackGenerationContext(private val outputDirPath: String, private val outputName: String) {

    private val functionArgumentMap = hashMapOf<IrElement, Int>()
    private var labelIndex = 0
    private val localNameCache: MutableMap<IrElement, Int> = hashMapOf()

    private val outputFile by lazy {
        val file = File(outputDirPath, "${outputName}.jack")
        if (file.exists()) {
            file.delete()
        }
        file
    }

    fun writeCode(code: String) {
        outputFile.appendText("$code\n")
    }

    fun addArgument(name: IrDeclarationWithName, index: Int) {
        functionArgumentMap[name] = index
    }

    fun clearArgument() {
        functionArgumentMap.clear()
    }

    fun clearLocalVariables() {
        localNameCache.clear()
    }

    fun getValue(name: IrDeclarationWithName) {
        if (functionArgumentMap.containsKey(name)) {
            writeCode("push argument ${functionArgumentMap[name]}")
        }
        if (localNameCache.containsKey(name)) {
            writeCode("push local ${localNameCache[name]}")
        }
    }

    fun getLabelName(): String {
        val labelName = "${outputName}_${labelIndex}"
        labelIndex++
        return labelName
    }

    fun getLocalIndex(declaration: IrDeclarationWithName): Int {
        return localNameCache.getOrPut(declaration) {
            localNameCache.size
        }
    }
}