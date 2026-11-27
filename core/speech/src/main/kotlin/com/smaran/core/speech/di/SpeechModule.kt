package com.smaran.core.speech.di

import com.smaran.core.domain.speech.ReminderPhraseParser
import com.smaran.core.domain.speech.EnglishReminderPhraseParser
import com.smaran.core.domain.speech.SpeechToText
import com.smaran.core.speech.AndroidSpeechToText
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SpeechModule {
    @Binds abstract fun bindSpeechToText(impl: AndroidSpeechToText): SpeechToText

    // v1 is English only. When a language is added, choose the parser from AppLanguage here.
    @Binds abstract fun bindPhraseParser(impl: EnglishReminderPhraseParser): ReminderPhraseParser
}
