package com.smaran.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /** Injected everywhere "now" is needed, so tests can fix the time. */
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
