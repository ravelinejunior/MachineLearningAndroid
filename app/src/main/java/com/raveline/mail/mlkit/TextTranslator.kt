/*
 * Copyright (c) 2024. Copyright from Junior Raveline
 */

/**
 * This file contains the implementation of the TextTranslator class, which is responsible for language identification and text translation using ML Kit.
 *
 * The TextTranslator class provides the following functionalities:
 * - Identifying the language of a given text
 * - Translating a text from a source language to a target language
 * - Verifying if a translation model is downloaded on the device
 * - Downloading a translation model to the device
 *
 * The class utilizes ML Kit's LanguageIdentification, Translation, and RemoteModelManager APIs for language identification, text translation, and model management respectively.
 *
 * Note: This file is subject to copyright and is authored by Junior Raveline.
 */

package com.raveline.mail.mlkit

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.raveline.mail.model.DownloadState
import com.raveline.mail.model.LanguageDataModel
import com.raveline.mail.model.LanguageModel
import com.raveline.mail.util.FileUtil
import java.util.Locale

class TextTranslator(private val fileUtil: FileUtil) {


    /**
     * Identifies the language of the given text using a language identifier model.
     *
     * @param text The text to be analyzed.
     * @param onSuccess Callback function to be called when the language is successfully identified.
     *                  It receives a [LanguageDataModel] object containing the language code and name.
     * @param onFailure Callback function to be called when the language cannot be identified.
     *                  It receives an error message as a [String].
     */
    fun getDetectedLanguage(
        text: String,
        onSuccess: (LanguageDataModel) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val languageName: String = translatableLanguageModels[languageCode]
                    ?: LanguageIdentifier.UNDETERMINED_LANGUAGE_TAG

                if (languageName != LanguageIdentifier.UNDETERMINED_LANGUAGE_TAG) {
                    onSuccess(
                        LanguageDataModel(
                            code = languageCode,
                            name = languageName
                        )
                    )
                } else {
                    onFailure("No language detected")
                }
            }.addOnFailureListener {
                onFailure("No language detected")
            }.addOnCompleteListener {
                languageIdentifier.close()
            }
    }

    /**
     * Translates the given text from the source language to the target language.
     *
     * @param text The text to be translated.
     * @param sourceLanguage The language of the source text.
     * @param targetLanguage The language to which the text should be translated.
     * @param onSuccess The callback function to be called when the translation is successful. It receives the original text and the translated text as parameters.
     * @param onFailure The callback function to be called when the translation fails. It receives an error message as a parameter.
     */
    fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLanguage).toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguage).toString())
            .build()

        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        onSuccess(text, translatedText)
                        Log.i(
                            "TAGTranslateText",
                            "Original text:$text\nTranslated text:$translatedText"
                        )
                    }
                    .addOnFailureListener {
                        onFailure("TAGTranslateText failed")
                        Log.e("TAGTranslateText", "Error translating:${it.message}")
                    }
            }
            .addOnFailureListener {
                onFailure("Translation failed")
            }
            .addOnCompleteListener {
                translator.close()
            }
    }

    /**
     * Verifies if the specified translation model is downloaded on the device.
     *
     * @param modelCode The code of the translation model to be verified.
     * @param onSuccess The callback function to be called when the model is downloaded.
     * @param onFailure The callback function to be called when the model is not downloaded. It receives an error message as a parameter.
     */
    fun verifyDownloadModel(
        modelCode: String,
        onSuccess: () -> Unit,
        onFailure: (String?) -> Unit
    ) {
        val modelManager = RemoteModelManager.getInstance()

        // Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                if (models.any { it.language == modelCode }) {
                    onSuccess()
                } else {
                    onFailure(null)
                }
            }
            .addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

    /**
     * Downloads the specified translation model.
     *
     * @param modelName The name of the translation model to be downloaded.
     * @param onSuccess The callback function to be called when the model is downloaded.
     * @param onFailure The callback function to be called when the model download fails. It receives an error message as a parameter.
     */
    fun downloadModel(
        modelName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(modelName).build()

        val conditions = DownloadConditions.Builder().requireWifi().build()

        modelManager.download(model, conditions)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

    fun removeModel(
        modelName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(modelName).build()

        modelManager.deleteDownloadedModel(model)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message.toString())
            }

    }

    fun getAllModels(): List<LanguageModel> {
        return TranslateLanguage.getAllLanguages().map { model ->
            LanguageModel(
                id = model,
                name = translatableLanguageModels[model]?.capitalize(Locale.getDefault())
                    ?: model.capitalize(Locale.getDefault()),
                downloadState = DownloadState.NOT_DOWNLOADED,
                size = fileUtil.getSizeModel(model)
            )
        }
    }

    fun getDownloadedModels(
        onSuccess: (List<LanguageModel>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val remoteModelManager = RemoteModelManager.getInstance()
        remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { translatedModels ->
                val languageModels = mutableListOf<LanguageModel>()

                translatedModels.forEach { translatedModel ->
                    try {
                        languageModels.add(
                            LanguageModel(
                                id = translatedModel.language,
                                name = translatableLanguageModels[translatedModel.language]?.capitalize()
                                    ?: translatedModel.language.capitalize(),
                                downloadState = DownloadState.DOWNLOADED,
                                size = fileUtil.getSizeModel(translatedModel.modelNameForBackend)
                            )
                        )
                    } catch (e: Exception) {
                        onFailure(e.message.toString())
                    }
                }

                onSuccess(languageModels)
            }.addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

}


