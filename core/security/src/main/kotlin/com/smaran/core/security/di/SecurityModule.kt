package com.smaran.core.security.di

import com.smaran.core.security.DatabasePassphraseProvider
import com.smaran.core.security.KeystoreDatabasePassphraseProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SecurityModule {
    @Binds
    abstract fun bindPassphraseProvider(impl: KeystoreDatabasePassphraseProvider): DatabasePassphraseProvider
}
