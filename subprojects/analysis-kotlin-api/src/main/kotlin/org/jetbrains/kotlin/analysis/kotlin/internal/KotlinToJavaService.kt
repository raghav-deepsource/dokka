package org.jetbrains.kotlin.analysis.kotlin.internal

import org.jetbrains.dokka.InternalDokkaApi
import org.jetbrains.dokka.links.DRI

@InternalDokkaApi
interface KotlinToJavaService {
    fun findAsJava(kotlinDri: DRI): DRI?
}
