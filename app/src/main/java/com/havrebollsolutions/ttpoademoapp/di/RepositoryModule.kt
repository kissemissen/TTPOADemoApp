package com.havrebollsolutions.ttpoademoapp.di

import com.havrebollsolutions.ttpoademoapp.data.repository.NetworkTerminalRepository
import com.havrebollsolutions.ttpoademoapp.data.repository.TerminalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTerminalRepository(
        networkTerminalRepository: NetworkTerminalRepository // Bind the remote network implementation
    ) : TerminalRepository
}