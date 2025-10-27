package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.dao.CharacterDao
import com.example.data.local.db.AppDatabase
import com.example.data.mapper.CharacterMapper
import com.example.data.remote.APILoggingInterceptor
import com.example.data.remote.api.RickAndMortyApi
import com.example.data.repository.CharactersRepositoryImpl
import com.example.domain.repository.CharactersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CharacterDataModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): APILoggingInterceptor {
        return APILoggingInterceptor()
    }

    // OkHttpClient с Interceptor
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: APILoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRickAndMortyApi(retrofit: Retrofit): RickAndMortyApi {
        return retrofit.create(RickAndMortyApi::class.java)
    }

    // Room Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "characters_db"
        ).build()
    }

    @Provides
    fun provideCharacterDao(db: AppDatabase): CharacterDao {
        return db.characterDao()
    }

    // Mapper
    @Provides
    fun provideCharacterMapper(): CharacterMapper {
        return CharacterMapper()
    }

    // Repository
    @Provides
    fun provideCharactersRepository(
        api: RickAndMortyApi,
        db: AppDatabase,
        mapper: CharacterMapper
    ): CharactersRepository {
        return CharactersRepositoryImpl(api, db, mapper)
    }


}
