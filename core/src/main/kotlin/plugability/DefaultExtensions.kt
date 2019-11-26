package org.jetbrains.dokka.plugability

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.pages.DefaultMarkdownToContentConverter
import org.jetbrains.dokka.renderers.DefaultRenderer
import org.jetbrains.dokka.renderers.HtmlRenderer
import org.jetbrains.dokka.resolvers.DefaultLocationProvider
import org.jetbrains.dokka.transformers.descriptors.DefaultDescriptorToDocumentationTranslator
import org.jetbrains.dokka.transformers.documentation.DefaultDocumentationNodeMerger
import org.jetbrains.dokka.transformers.documentation.DefaultDocumentationToPageTranslator

object DefaultExtensions : DokkaExtensionHandler {
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    override fun <T : Any, E : ExtensionPoint<T>> get(point: E, askDefault: AskDefault): List<T> =
        when (point) {
            CoreExtensions.descriptorToDocumentationTranslator -> DefaultDescriptorToDocumentationTranslator
            CoreExtensions.documentationMerger -> DefaultDocumentationNodeMerger
            CoreExtensions.markdownToContentConverterFactory -> ::DefaultMarkdownToContentConverter
            CoreExtensions.documentationToPageTranslator -> DefaultDocumentationToPageTranslator
            CoreExtensions.renderer -> ::HtmlRenderer
            CoreExtensions.locationProvider -> ::DefaultLocationProvider
            CoreExtensions.fileExtension -> ".html"
            else -> null
        }.let { listOfNotNull(it) as List<T> }
}