package com.bqliang.running.diary.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.bqliang.running.diary.database.RunningDiaryDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import javax.inject.Singleton

/**
 * App module
 *
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDb(@ApplicationContext context: Context): RunningDiaryDB = RunningDiaryDB.getInstance(context)


    @Singleton
    @Provides
    fun provideRunDao(db: RunningDiaryDB) = db.getRunDao()


    @Singleton
    @Provides
    fun providePathDao(db: RunningDiaryDB) = db.getPathDao()


    @Singleton
    @Provides
    fun providePointDao(db: RunningDiaryDB) = db.getPointDao()

    @Singleton
    @Provides
    fun provideBodyDao(db: RunningDiaryDB) = db.getBodyDao()

    @Singleton
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context) = NotificationManagerCompat.from(context)

    @Singleton
    @Provides
    fun provideMarkwon(@ApplicationContext context: Context) = Markwon.create(context)
}