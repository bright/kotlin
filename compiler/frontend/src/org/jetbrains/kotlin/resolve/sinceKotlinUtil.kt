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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import java.util.*

private val SINCE_KOTLIN_FQ_NAME = FqName("kotlin.SinceKotlin")

// TODO: use-site targeted annotations
internal fun DeclarationDescriptor.getSinceKotlinAnnotation(): AnnotationDescriptor? =
        annotations.findAnnotation(SINCE_KOTLIN_FQ_NAME)

/**
 * If the descriptor is inaccessible according to [languageVersionSettings], returns the API version, since which it can be accessed.
 * Otherwise returns null.
 */
internal fun DeclarationDescriptor.getSinceVersionIfInaccessible(languageVersionSettings: LanguageVersionSettings): ApiVersion? {
    val version =
            if (this is CallableMemberDescriptor && !kind.isReal) getSinceKotlinVersionByOverridden(this)
            else getOwnSinceKotlinVersion()

    // Allow access in the following cases:
    // 1) There's no @SinceKotlin annotation for this descriptor
    // 2) There's a @SinceKotlin annotation but its value is some unrecognizable nonsense
    // 3) The value as a version is not greater than our API version
    return if (version != null && languageVersionSettings.apiVersion < version) version else null
}

/**
 * @return null if there are no overridden members or if there's at least one declaration in the hierarchy not annotated with [SinceKotlin],
 *         or the minimal value of the version from all declarations annotated with [SinceKotlin] otherwise.
 */
private fun getSinceKotlinVersionByOverridden(root: CallableMemberDescriptor): ApiVersion? {
    val visited = HashSet<CallableMemberDescriptor>()
    val versions = LinkedHashSet<ApiVersion>()
    var hasNoSinceKotlinVersion = false

    fun traverse(node: CallableMemberDescriptor) {
        if (!visited.add(node)) return

        if (!node.kind.isReal) {
            node.original.overriddenDescriptors.forEach(::traverse)
        }
        else {
            val ourVersion = node.getOwnSinceKotlinVersion()
            if (ourVersion != null) {
                versions.add(ourVersion)
            }
            else {
                hasNoSinceKotlinVersion = true
            }
        }
    }

    traverse(root)

    if (hasNoSinceKotlinVersion || versions.isEmpty()) return null

    return versions.min()
}

private fun DeclarationDescriptor.getOwnSinceKotlinVersion(): ApiVersion? {
    fun DeclarationDescriptor.loadAnnotationValue(): ApiVersion? =
            (getSinceKotlinAnnotation()?.allValueArguments?.values?.singleOrNull()?.value as? String)?.let(ApiVersion.Companion::parse)

    val ownVersion = loadAnnotationValue()
    val ctorClass = (this as? ConstructorDescriptor)?.containingDeclaration?.loadAnnotationValue()
    val property = (this as? PropertyAccessorDescriptor)?.correspondingProperty?.loadAnnotationValue()

    return listOfNotNull(ownVersion, ctorClass, property).min()
}
