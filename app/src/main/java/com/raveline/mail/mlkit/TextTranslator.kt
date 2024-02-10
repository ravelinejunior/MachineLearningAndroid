/*
 * Copyright (c) 2024. Copyright from Junior Raveline
 */

package com.raveline.mail.mlkit

import android.util.Log
import com.raveline.mail.model.LanguageDataModel
import com.raveline.mail.util.FileUtil
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TextTranslator(private val fileUtil: FileUtil) {

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
            }
    }

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
    }

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

    fun downloadModel(
        modelName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(modelName).build()

        val conditions = DownloadConditions.Builder().build()

        modelManager.download(model, conditions)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message.toString())
            }
    }

}


