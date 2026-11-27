package com.smaran.app.di

import com.smaran.core.domain.speech.AppLanguage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {
    /** v1: English as spoken in India. Becomes the person's chosen language once more are added. */
    @Provides
    fun provideAppLanguage(): AppLanguage = object : AppLanguage {
        override val speechLanguageTag: String = "en-IN"
    }
}
