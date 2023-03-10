package org.p2p.ethereumkit.internal.api.storage

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import org.p2p.ethereumkit.internal.api.models.AccountState
import org.p2p.ethereumkit.internal.api.models.LastBlockHeight


@Database(entities = [AccountState::class, LastBlockHeight::class], version = 3, exportSchema = false)
@TypeConverters(RoomTypeConverters::class)
abstract class ApiDatabase : RoomDatabase() {

    abstract fun balanceDao(): AccountStateDao
    abstract fun lastBlockHeightDao(): LastBlockHeightDao

    companion object {

        fun getInstance(context: Context, databaseName: String): ApiDatabase {
            return Room.databaseBuilder(context, ApiDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }

    }

}