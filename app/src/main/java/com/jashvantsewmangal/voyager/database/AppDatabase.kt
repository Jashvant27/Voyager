package com.jashvantsewmangal.voyager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jashvantsewmangal.voyager.models.ActivityEntity
import com.jashvantsewmangal.voyager.models.DayEntity

@Database(entities = [ActivityEntity::class, DayEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun dayDao(): DayDao
}
