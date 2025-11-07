package com.havrebollsolutions.ttpoademoapp.di.service

import com.adyen.ipp.api.authentication.AuthenticationProvider
import com.adyen.ipp.api.authentication.MerchantAuthenticationService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdyenAuthenticationService : MerchantAuthenticationService() {
    @Inject
    override lateinit var authenticationProvider: AuthenticationProvider
}