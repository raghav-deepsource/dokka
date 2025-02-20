package org.jetbrains.dokka.kotlinAsJava

import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.WithExtraProperties

internal fun <T : Documentable> WithExtraProperties<T>.jvmStatic(): Annotations.Annotation? =
    extra[Annotations]?.directAnnotations?.entries?.firstNotNullOfOrNull { (_, annotations) -> annotations.jvmStaticAnnotation() }

internal fun List<Annotations.Annotation>.jvmStaticAnnotation(): Annotations.Annotation? =
    firstOrNull { it.dri.packageName == "kotlin.jvm" && it.dri.classNames == "JvmStatic" }

