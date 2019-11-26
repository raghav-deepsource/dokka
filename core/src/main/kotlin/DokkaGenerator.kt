package org.jetbrains.dokka

import org.jetbrains.dokka.Model.Module
import org.jetbrains.dokka.Utilities.pretty
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.DefaultMarkdownToContentConverter
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.single
import org.jetbrains.dokka.renderers.FileWriter
import org.jetbrains.dokka.renderers.HtmlRenderer
import org.jetbrains.dokka.resolvers.DefaultLocationProvider
import org.jetbrains.dokka.resolvers.LocationProvider
import org.jetbrains.dokka.transformers.documentation.DefaultDocumentationToPageTranslator
import org.jetbrains.dokka.transformers.descriptors.DokkaDescriptorVisitor
import org.jetbrains.dokka.transformers.documentation.DefaultDocumentationNodeMerger
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

class DokkaGenerator(
    private val configuration: DokkaConfiguration,
    private val logger: DokkaLogger
) {

    fun generate() {
        logger.debug("Setting up analysis environments")
        val platforms: Map<PlatformData, EnvironmentAndFacade> = configuration.passesConfigurations.map {
            PlatformData(it.analysisPlatform, it.targets) to createEnvironmentAndFacade(it)
        }.toMap()

        logger.debug("Initializing plugins")
        val context = DokkaContext.create(configuration.pluginsClasspath, logger, platforms)

        logger.debug("Creating documentation models")
        val modulesFromPlatforms = platforms.map { (pdata, _) -> translateDescriptors(pdata, context) }

        logger.debug("Merging documentation models")
        val documentationModel = context.single(CoreExtensions.documentationMerger)
            .invoke(modulesFromPlatforms, context)

        logger.debug("Transforming documentation model")
        val transformedDocumentation = context[CoreExtensions.documentationTransformer]
            .fold(documentationModel) { acc, t -> t(acc, context) }

        logger.debug("Creating pages")
        val pages = context.single(CoreExtensions.documentationToPageTranslator)
            .invoke(transformedDocumentation, context)

        logger.debug("Transforming pages")
        val transformedPages = context[CoreExtensions.pageTransformer]
            .fold(pages) { acc, t -> t(acc, context) }

        logger.debug("Rendering")
        val fileWriter = FileWriter(configuration.outputDir, "")
        val locationProvider = context.single(CoreExtensions.locationProvider)
            .invoke(transformedPages, configuration, context)
        val renderer = context.single(CoreExtensions.renderer)
            .invoke(fileWriter, locationProvider, context)

        renderer.render(transformedPages)
    }

//    fun generate(int: Int) {
//
//        logger.debug("Initializing plugins")
//        val context = DokkaContext.create(configuration.pluginsClasspath, logger, platforms)
//
//        configuration.passesConfigurations.map { pass ->
//            AnalysisEnvironment(DokkaMessageCollector(logger), pass.analysisPlatform).run {
//                if (analysisPlatform == Platform.jvm) {
//                    addClasspath(PathUtil.getJdkClassesRootsFromCurrentJre())
//                }
//                for (element in pass.classpath) {
//                    addClasspath(File(element))
//                }
//
//                addSources(pass.sourceRoots.map { it.path })
//
//                loadLanguageVersionSettings(pass.languageVersion, pass.apiVersion)
//
//                val environment = createCoreEnvironment()
//                val (facade, _) = createResolutionFacade(environment)
//
//                environment.getSourceFiles().asSequence()
//                        .map { it.packageFqName }
//                        .distinct()
//                        .mapNotNull { facade.resolveSession.getPackageFragment(it) }
//                        .map {
//                        DokkaDescriptorVisitor(
//                            PlatformData(
//                                pass.analysisPlatform,
//                                pass.targets
//                            ), facade
//                        )
//                            .visitPackageFragmentDescriptor(it, DRI.topLevel)
//                    }
//                    .toList()
//                    .let { Module(it) }
//                    .let { DefaultDocumentationNodeMerger(it) }
//                    .also { println("${pass.analysisPlatform}:\n${it.pretty()}\n\n") }
//            }
//        }.let {
//            val markdownConverter = DefaultMarkdownToContentConverter(logger)
//            DefaultDocumentationToPageTranslator(
//                markdownConverter,
//                logger
//            ).transform(
//                DefaultDocumentationNodeMerger(
//                    it
//                )
//            )
//        }.let {
//            context[CoreExtensions.pageTransformer].fold(it) { pn, t -> t(pn, context) }
//        }.also {
//            HtmlRenderer(
//                FileWriter(configuration.outputDir, ""),
//                DefaultLocationProvider(it, configuration, ".${configuration.format}")
//            ).render(it)
//        }
//    }

    private fun createEnvironmentAndFacade(pass: DokkaConfiguration.PassConfiguration): EnvironmentAndFacade =
        AnalysisEnvironment(DokkaMessageCollector(logger), pass.analysisPlatform).run {
            if (analysisPlatform == Platform.jvm) {
                addClasspath(PathUtil.getJdkClassesRootsFromCurrentJre())
            }
            pass.classpath.forEach { addClasspath(File(it)) }

            addSources(pass.sourceRoots.map { it.path })

            loadLanguageVersionSettings(pass.languageVersion, pass.apiVersion)

            val environment = createCoreEnvironment()
            val (facade, _) = createResolutionFacade(environment)
            EnvironmentAndFacade(environment, facade)
        }

    private fun translateDescriptors(platformData: PlatformData, context: DokkaContext): Module {
        val (environment, facade) = context.platforms.getValue(platformData)

        val packageFragments = environment.getSourceFiles().asSequence()
            .map { it.packageFqName }
            .distinct()
            .mapNotNull { facade.resolveSession.getPackageFragment(it) }
            .toList()

        return context.single(CoreExtensions.descriptorToDocumentationTranslator)
            .invoke(packageFragments, platformData, context)
    }

    private class DokkaMessageCollector(private val logger: DokkaLogger) : MessageCollector {
        override fun clear() {
            seenErrors = false
        }

        private var seenErrors = false

        override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
            if (severity == CompilerMessageSeverity.ERROR) {
                seenErrors = true
            }
            logger.error(MessageRenderer.PLAIN_FULL_PATHS.render(severity, message, location))
        }

        override fun hasErrors() = seenErrors
    }
}

// It is not data class due to ill-defined equals
class EnvironmentAndFacade(val environment: KotlinCoreEnvironment, val facade: DokkaResolutionFacade) {
    operator fun component1() = environment
    operator fun component2() = facade
}