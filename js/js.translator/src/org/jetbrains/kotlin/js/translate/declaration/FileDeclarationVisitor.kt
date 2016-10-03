/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.kotlin.js.translate.declaration

import com.google.dart.compiler.backend.js.ast.JsScope
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.general.TranslatorVisitor
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getFunctionDescriptor
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext

class FileDeclarationVisitor(
        val context: TranslationContext,
        val scope: JsScope
) : TranslatorVisitor<Unit>() {
    override fun emptyResult(context: TranslationContext) {}

    override fun visitClassOrObject(declaration: KtClassOrObject, context: TranslationContext) {
        ClassTranslator.translate(declaration, context)
        val classDescriptor = BindingUtils.getClassDescriptor(context.bindingContext(), declaration)
        context.export(classDescriptor)
    }

    override fun visitProperty(expression: KtProperty, context: TranslationContext) {
        val propertyDescriptor = BindingUtils.getPropertyDescriptor(context.bindingContext(), expression)

        if (context.bindingContext()[BindingContext.BACKING_FIELD_REQUIRED, propertyDescriptor] ?: false) {
            val initializer = expression.initializer?.let { Translation.translateAsExpression(it, context) }
            val innerName = context.getInnerNameForDescriptor(propertyDescriptor)
            context.addRootStatement(JsAstUtils.newVar(innerName, initializer))
        }

        if (!JsDescriptorUtils.isSimpleFinalProperty(propertyDescriptor)) {
            val propertyContext = context.newDeclaration(propertyDescriptor)
            val getter = propertyDescriptor.getter!!
            if (!getter.isDefault) {
                translateFunction(getter, expression.getter!!, propertyContext)
            }
            val setter = propertyDescriptor.setter
            if (setter != null) {
                if (!setter.isDefault) {
                    translateFunction(setter, expression.setter!!, propertyContext)
                }
            }
        }
        context.export(propertyDescriptor)
    }

    override fun visitNamedFunction(expression: KtNamedFunction, context: TranslationContext) {
        val descriptor = getFunctionDescriptor(context.bindingContext(), expression)
        translateFunction(descriptor, expression, context)
        context.export(descriptor)
    }

    private fun translateFunction(descriptor: FunctionDescriptor, expression: KtDeclarationWithBody, context: TranslationContext) {
        val innerContext = context.newDeclaration(descriptor)
        val function = context.declareTopLevelFunction(descriptor)
        val functionExpression = Translation.functionTranslator(expression, innerContext, function).translateAsMethod()
        if (functionExpression == function) {
            context.addRootStatement(function.makeStmt())
        }
        else {
            context.addRootStatement(JsAstUtils.newVar(function.name, functionExpression))
        }
    }
}
