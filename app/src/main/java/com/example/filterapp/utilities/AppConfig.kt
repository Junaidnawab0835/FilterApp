package com.example.filterapp.utilities

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import org.opencv.android.OpenCVLoader


@HiltAndroidApp
class AppConfig: Application() {
    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initDebug()
    }
}