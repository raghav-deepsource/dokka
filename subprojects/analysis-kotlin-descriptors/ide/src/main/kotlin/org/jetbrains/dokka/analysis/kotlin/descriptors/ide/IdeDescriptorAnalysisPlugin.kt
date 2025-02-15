package org.jetbrains.dokka.analysis.kotlin.descriptors.ide

import org.jetbrains.dokka.InternalDokkaApi
import org.jetbrains.dokka.analysis.kotlin.descriptors.compiler.CompilerDescriptorAnalysisPlugin
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

@InternalDokkaApi
class IdeDescriptorAnalysisPlugin : DokkaPlugin() {

    internal val ideKdocFinder by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().kdocFinder providing ::IdePluginKDocFinder
    }

    internal val ideDescriptorFinder by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().descriptorFinder providing { IdeDescriptorFinder() }
    }

    internal val ideKlibService by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().klibService providing { IdeKLibService() }
    }

    internal val ideCompilerExtensionPointProvider by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().compilerExtensionPointProvider providing { IdeCompilerExtensionPointProvider() }
    }

    internal val ideApplicationHack by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().mockApplicationHack providing { IdeMockApplicationHack() }
    }

    internal val ideAnalysisContextCreator by extending {
        plugin<CompilerDescriptorAnalysisPlugin>().analysisContextCreator providing { IdeAnalysisContextCreator() }
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement
}
