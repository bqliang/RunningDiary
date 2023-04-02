package com.bqliang.running.diary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bqliang.running.diary.database.dao.BodyDao
import com.bqliang.running.diary.database.dao.PathDao
import com.bqliang.running.diary.database.dao.PointDao
import com.bqliang.running.diary.database.dao.RunDao
import com.bqliang.running.diary.database.entity.Body
import com.bqliang.running.diary.database.entity.Path
import com.bqliang.running.diary.database.entity.Point
import com.bqliang.running.diary.database.entity.Run

@Database(version = 1, entities = [Run::class, Path::class, Point::class, Body::class])
@TypeConverters(BitMapConverter::class)
abstract class RunningDiaryDB : RoomDatabase() {

    abstract fun getRunDao(): RunDao
    abstract fun getPathDao(): PathDao
    abstract fun getPointDao(): PointDao
    abstract fun getBodyDao(): BodyDao


    companion object {

        @Volatile
        private var INSTANCE: RunningDiaryDB? = null

        fun getInstance(context: Context): RunningDiaryDB =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    /* context = */ context,
                    /* klass = */ RunningDiaryDB::class.java,
                    /* name = */ "running_diary.db"
                )
                    // 设置数据库的迁移策略为 "破坏性迁移": 在进行数据库迁移时，先删除原有的数据库，再重新建立一个新的数据库
                    .fallbackToDestructiveMigration()
                    .createFromAsset("database/running_diary.db")
                    .build()
                INSTANCE = instance
                instance
            }
    }
}