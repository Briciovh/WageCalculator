package com.softeen.wagecalculator.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.softeen.wagecalculator.BuildConfig
import com.softeen.wagecalculator.data.network.ExchangeRateApi
import com.softeen.wagecalculator.data.network.NetworkExchangeRateRepository
import com.softeen.wagecalculator.data.repository.ExchangeRateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        impl: NetworkExchangeRateRepository
    ): ExchangeRateRepository

    companion object {
        private const val BASE_URL = "https://api.frankfurter.dev/"

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }
                .build()
        }

        @Provides
        @Singleton
        fun provideExchangeRateApi(okHttpClient: OkHttpClient, json: Json): ExchangeRateApi {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(ExchangeRateApi::class.java)
        }
    }
}
