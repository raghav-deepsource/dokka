package org.jetbrains.dokka.analysis.markdown.jb

import org.intellij.markdown.MarkdownElementTypes
import org.jetbrains.dokka.InternalDokkaApi

// TODO [beresnev] move/rename if it's only used for CustomDocTag. for now left as is for compatibility
@InternalDokkaApi
val MARKDOWN_ELEMENT_FILE_NAME = MarkdownElementTypes.MARKDOWN_FILE.name
