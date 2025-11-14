package com.havrebollsolutions.ttpoademoapp.di

import android.content.Context
import androidx.room.Room
import com.havrebollsolutions.ttpoademoapp.data.database.AppDatabase
import com.havrebollsolutions.ttpoademoapp.data.database.MIGRATION_3_4
import com.havrebollsolutions.ttpoademoapp.data.database.daos.ConfigDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.MenuItemDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.OrderDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database"
            )
            .addMigrations(MIGRATION_3_4)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideMenuItemDao(database: AppDatabase): MenuItemDao {
        return database.menuItemDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideConfigDao(database: AppDatabase): ConfigDao {
        return database.configDao()

    }
}