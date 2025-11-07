package com.havrebollsolutions.ttpoademoapp.di

import com.adyen.ipp.api.authentication.AuthenticationProvider
import com.havrebollsolutions.ttpoademoapp.data.repository.AdyenAuthenticationProvider
import com.havrebollsolutions.ttpoademoapp.data.repository.TerminalRepository
import com.havrebollsolutions.ttpoademoapp.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdyenSdkAuthenticationModule {

    @Provides
    @Singleton
    fun provideAuthenticationProvider(
        terminalRepository: TerminalRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): AuthenticationProvider {
        return AdyenAuthenticationProvider(terminalRepository, userPreferencesRepository)
    }
}