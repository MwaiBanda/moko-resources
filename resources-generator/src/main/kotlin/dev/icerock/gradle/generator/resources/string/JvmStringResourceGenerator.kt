/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.gradle.generator.resources.string

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import dev.icerock.gradle.generator.Constants
import dev.icerock.gradle.generator.PlatformResourceGenerator
import dev.icerock.gradle.generator.addJvmResourcesClassLoaderProperty
import dev.icerock.gradle.generator.localization.LanguageType
import dev.icerock.gradle.metadata.resource.StringMetadata
import org.apache.commons.text.StringEscapeUtils
import java.io.File

internal class JvmStringResourceGenerator(
    private val flattenClassPackage: String,
    private val className: String,
    private val resourcesGenerationDir: File
) : PlatformResourceGenerator<StringMetadata> {
    override fun imports(): List<ClassName> = emptyList()

    override fun generateInitializer(metadata: StringMetadata): CodeBlock {
        return CodeBlock.of(
            "StringResource(resourcesClassLoader = %L, bundleName = %L, key = %S)",
            Constants.Jvm.resourcesClassLoaderPropertyName,
            stringsBundlePropertyName,
            metadata.key
        )
    }

    override fun generateResourceFiles(data: List<StringMetadata>) {
        data.processLanguages().forEach { (lang, strings) ->
            generateLanguageFile(
                language = LanguageType.fromLanguage(lang),
                strings = strings
            )
        }
    }

    override fun generateBeforeProperties(
        builder: TypeSpec.Builder,
        metadata: List<StringMetadata>
    ) {
        builder.addJvmResourcesClassLoaderProperty(className)

        // FIXME duplication
        val property: PropertySpec = PropertySpec.builder(
            stringsBundlePropertyName,
            STRING,
            KModifier.PRIVATE
        ).initializer(CodeBlock.of("\"%L/%L\"", Constants.Jvm.localizationDir, getBundlePath()))
            .build()

        builder.addProperty(property)
    }

    private fun generateLanguageFile(language: LanguageType, strings: Map<String, String>) {
        val fileDirName = "${getBundlePath()}${language.jvmResourcesSuffix}"

        val localizationDir = File(resourcesGenerationDir, Constants.Jvm.localizationDir)
        localizationDir.mkdirs()

        val stringsFile = File(localizationDir, "$fileDirName.properties")

        val content: String = strings.map { (key, value) ->
            "$key = ${convertXmlStringToJvmLocalization(value)}"
        }.joinToString("\n")

        stringsFile.writeText(content)
    }

    private fun getBundlePath(): String = "${flattenClassPackage}_$stringsBundleName"

    // FIXME duplication
    // TODO should we do that?
    private fun convertXmlStringToJvmLocalization(input: String): String {
        return StringEscapeUtils.unescapeXml(input)
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
    }

    private companion object {
        const val stringsBundlePropertyName = "stringsBundle"
        const val stringsBundleName = "mokoBundle"
    }
}