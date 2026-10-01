package com.codit.cryptowatchwallet.di

import android.content.Context
import androidx.room.Room
import com.codit.cryptowatchwallet.data.local.AppDatabase
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.local.WalletDao
import com.codit.cryptowatchwallet.data.remote.MarketApi
import com.codit.cryptowatchwallet.data.remote.WalletApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL_BLOCKCYPHER = "https://api.blockcypher.com/v1/"
    private const val BASE_URL_MARKET = "https://min-api.cryptocompare.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("wallet")
    fun provideWalletRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_BLOCKCYPHER)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("market")
    fun provideMarketRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_MARKET)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideWalletApi(@Named("wallet") retrofit: Retrofit): WalletApi =
        retrofit.create(WalletApi::class.java)

    @Provides
    @Singleton
    fun provideMarketApi(@Named("market") retrofit: Retrofit): MarketApi =
        retrofit.create(MarketApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWalletDao(db: AppDatabase): WalletDao = db.walletDao()

    @Provides
    fun provideMarketDao(db: AppDatabase): MarketDao = db.marketDao()
}
