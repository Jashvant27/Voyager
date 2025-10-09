package com.jashvantsewmangal.voyager.repository

import android.content.Context
import androidx.room.Room
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_NAME
import com.jashvantsewmangal.voyager.database.ActivityDao
import com.jashvantsewmangal.voyager.database.AppDatabase
import com.jashvantsewmangal.voyager.database.DayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()
    }

    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideDayDao(db: AppDatabase): DayDao = db.dayDao()

    @Provides
    @Singleton
    fun provideDatabaseMapper(): DatabaseMapper = DatabaseMapper()

    @Provides
    fun provideDatabaseRepository(activityDao: ActivityDao, dayDao: DayDao, databaseMapper: DatabaseMapper): DatabaseRepository =
        DatabaseRepository(activityDao, dayDao, databaseMapper)
}
