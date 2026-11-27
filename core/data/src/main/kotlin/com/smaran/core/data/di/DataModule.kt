package com.smaran.core.data.di

import android.content.Context
import androidx.room.Room
import com.smaran.core.data.db.NoteDao
import com.smaran.core.data.db.SmaranDatabase
import com.smaran.core.data.repository.OfflineNoteRepository
import com.smaran.core.domain.repository.NoteReader
import com.smaran.core.domain.repository.NoteWriter
import com.smaran.core.security.DatabasePassphraseProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds abstract fun bindNoteReader(impl: OfflineNoteRepository): NoteReader
    @Binds abstract fun bindNoteWriter(impl: OfflineNoteRepository): NoteWriter

    companion object {
        private const val DATABASE_NAME = "smaran.db"

        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
            passphraseProvider: DatabasePassphraseProvider,
        ): SmaranDatabase {
            System.loadLibrary("sqlcipher")
            val factory = SupportOpenHelperFactory(passphraseProvider.getPassphrase())
            return Room.databaseBuilder(context, SmaranDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(factory)
                .build()
        }

        @Provides
        fun provideNoteDao(database: SmaranDatabase): NoteDao = database.noteDao()
    }
}
