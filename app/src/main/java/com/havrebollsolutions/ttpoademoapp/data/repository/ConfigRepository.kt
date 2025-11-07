package com.havrebollsolutions.ttpoademoapp.data.repository

import com.havrebollsolutions.ttpoademoapp.data.database.daos.ConfigDao
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Config
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val configDao: ConfigDao
) {
    fun getConfig() = configDao.getConfig()

    suspend fun insertConfig(config: Config) = configDao.insertConfig(config)

    suspend fun deleteConfig(config: Config) = configDao.deleteConfig(config)

}