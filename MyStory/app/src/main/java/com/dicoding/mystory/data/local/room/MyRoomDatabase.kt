package com.dicoding.mystory.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.mystory.data.model.RemoteKeys
import com.dicoding.mystory.data.model.Story

@Database(
    entities = [Story::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object{
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): MyRoomDatabase{
            if(INSTANCE == null){
                synchronized(MyRoomDatabase::class.java){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MyRoomDatabase::class.java,"my_story_database")
                        .build()
                }
            }
            return INSTANCE as MyRoomDatabase
        }
    }
}