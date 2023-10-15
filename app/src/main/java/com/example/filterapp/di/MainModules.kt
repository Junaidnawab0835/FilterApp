package com.example.filterapp.di

import android.content.Context
import com.example.filterapp.repository.EditImageRepository
import com.example.filterapp.repository.EditImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModules {


    @Singleton
    @Provides
    fun providesEditRepo(@ApplicationContext appContext: Context):EditImageRepository
    {
        return EditImageRepositoryImpl(appContext)
    }
    
}