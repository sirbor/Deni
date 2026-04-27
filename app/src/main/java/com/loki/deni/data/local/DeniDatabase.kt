package com.loki.deni.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.loki.deni.data.local.dao.DeniDao
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        LoanEntity::class,
        TransactionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class DeniDatabase : RoomDatabase() {
    abstract val deniDao: DeniDao

    companion object {
        const val DATABASE_NAME = "deni_db"
    }
}
