package com.havrebollsolutions.ttpoademoapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.havrebollsolutions.ttpoademoapp.network.TerminalServiceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.havrebollsolutions.ttpoademoapp.network.CloudDeviceApiBaseUrl
import com.havrebollsolutions.ttpoademoapp.network.SoftposConfigBaseUrl
import java.util.concurrent.TimeUnit


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
//    private const val BASE_URL = "https://m80ogq4n74.execute-api.eu-north-1.amazonaws.com/dev/"

    // Define the base URLs (Replace with your actual URLs)
    private const val SOFTPOS_CONFIG_URL = "https://softposconfig-test.adyen.com/softposconfig/v3/"
    private const val CLOUD_DEVICE_API_URL = "https://device-api-test.adyen.com/v1/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response body
        }
    }

//    @Provides
//    @Singleton
//    fun provideAuthorizationInterceptor(): Interceptor {
//        return Interceptor { chain ->
//            val requestWithHeader = chain.request().newBuilder()
//                .header("x-api-key", BuildConfig.TERMINAL_SERVICE_API_KEY)
//                .build()
//            chain.proceed(requestWithHeader)
//        }
//    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
//        authorizationInterceptor: Interceptor
    ): OkHttpClient {
        val longTimeoutSeconds = 150L // 150 seconds

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
//            .addInterceptor(authorizationInterceptor)
            // Add other interceptors like authentication if needed
            // Add time out params
            .connectTimeout(longTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(longTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(longTimeoutSeconds, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    // --- Provide Retrofit for SOFTPOS CONFIG Endpoint ---
    @Provides
    @Singleton
    @SoftposConfigBaseUrl
    fun provideSoftposConfigRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SOFTPOS_CONFIG_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- Provide Retrofit for CLOUD DEVICE API Endpoint ---
    @Provides
    @Singleton
    @CloudDeviceApiBaseUrl
    fun provideCloudDeviceApiRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CLOUD_DEVICE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- Provide Retrofit for TERMINAL SERVICE AWS API Endpoint ---
//    @Provides
//    @Singleton
//    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }


    // --- Provide the TerminalServiceApi INSTANCES ---

    // Provide the instance used for POS SDK Sessions (using the Auth base URL)
    @Provides
    @Singleton
    @SoftposConfigBaseUrl // Qualifier for the Auth instance of the interface
    fun provideSoftposConfigTerminalServiceApi(@SoftposConfigBaseUrl retrofit: Retrofit): TerminalServiceApi {
        return retrofit.create(TerminalServiceApi::class.java)
    }

    // Provide the instance used for getting Terminals (using the Terminal base URL)
    @Provides
    @Singleton
    @CloudDeviceApiBaseUrl // Qualifier for the Terminal instance of the interface
    fun provideCloudDeviceApiTerminalServiceApi(@CloudDeviceApiBaseUrl retrofit: Retrofit): TerminalServiceApi {
        return retrofit.create(TerminalServiceApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideTerminalService(retrofit: Retrofit): TerminalServiceApi {
//        return retrofit.create(TerminalServiceApi::class.java)
//    }
}