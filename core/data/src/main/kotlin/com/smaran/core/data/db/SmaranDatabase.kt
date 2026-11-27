package com.smaran.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class SmaranDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
