package com.raveline.mail.di

import com.raveline.mail.mlkit.TextTranslator
import com.raveline.mail.util.FileUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TextTranslatorModule {
    @Provides
    fun provideTextTranslator(fileUtil: FileUtil): TextTranslator {
        return TextTranslator(fileUtil)
    }
}