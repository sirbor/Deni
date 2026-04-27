package com.loki.deni.di

import android.content.Context
import androidx.room.Room
import com.loki.deni.BuildConfig
import com.loki.deni.data.local.DeniDatabase
import com.loki.deni.data.local.DominicBorTestSeed
import com.loki.deni.data.local.dao.DeniDao
import com.loki.deni.util.core.DatabaseSecurity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private fun buildDatabase(context: Context): DeniDatabase {
        return Room.databaseBuilder(
            context,
            DeniDatabase::class.java,
            DeniDatabase.DATABASE_NAME
        )
            .openHelperFactory(SupportOpenHelperFactory(DatabaseSecurity.passphrase(context)))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDeniDatabase(@ApplicationContext context: Context): DeniDatabase {
        var db = buildDatabase(context)
        val opened = runCatching {
            db.openHelper.writableDatabase
        }.isSuccess
        if (!opened) {
            db.close()
            context.deleteDatabase(DeniDatabase.DATABASE_NAME)
            db = buildDatabase(context)
        }
        if (BuildConfig.DEBUG) {
            val seeded = runCatching {
                runBlocking(Dispatchers.IO) { DominicBorTestSeed.seedIfNeeded(db.deniDao) }
            }.isSuccess
            if (!seeded) {
                db.close()
                context.deleteDatabase(DeniDatabase.DATABASE_NAME)
                db = buildDatabase(context)
                runBlocking(Dispatchers.IO) { DominicBorTestSeed.seedIfNeeded(db.deniDao) }
            }
        }
        return db
    }

    @Provides
    @Singleton
    fun provideDeniDao(database: DeniDatabase): DeniDao {
        return database.deniDao
    }
}
