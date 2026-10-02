package com.codit.cryptowatchwallet.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.codit.cryptowatchwallet.data.local.AppDatabase
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.local.WalletDao
import com.codit.cryptowatchwallet.data.remote.CoinGeckoApi
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
    // Same endpoints as Crypto-Converter-Android (NetworkModule).
    private const val BASE_URL_MARKET = "https://min-api.cryptocompare.com/"
    private const val BASE_URL_COINGECKO = "https://api.coingecko.com/api/v3/"

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
    @Named("coingecko")
    fun provideCoinGeckoRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_COINGECKO)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApi(@Named("coingecko") retrofit: Retrofit): CoinGeckoApi =
        retrofit.create(CoinGeckoApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = openDatabase(context)

    /**
     * Opens [AppDatabase.NAME], repairing it if needed.
     *
     * Room only falls back to a destructive migration when the schema *version* changes.
     * A database left behind by an older release whose schema differs without a version
     * bump (see [AppDatabase]) makes Room throw from the very first query and take the
     * whole app down on launch, so the file is opened eagerly here and, if it cannot be
     * opened/repaired at all, it is deleted and recreated. As a last resort an in-memory
     * database is used so a broken file can never stop the app from starting.
     */
    private fun openDatabase(context: Context): AppDatabase {
        return try {
            buildDatabase(context).also { it.openHelper.writableDatabase }
        } catch (t: Throwable) {
            Log.w("app", "Unusable database, recreating it", t)
            try {
                context.deleteDatabase(AppDatabase.NAME)
                buildDatabase(context).also { it.openHelper.writableDatabase }
            } catch (t2: Throwable) {
                Log.e("app", "Database unusable, falling back to in-memory storage", t2)
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(*AppDatabase.MIGRATIONS)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWalletDao(db: AppDatabase): WalletDao = db.walletDao()

    @Provides
    fun provideMarketDao(db: AppDatabase): MarketDao = db.marketDao()
}
