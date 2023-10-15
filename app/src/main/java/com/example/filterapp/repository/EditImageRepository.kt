package com.example.filterapp.repository

import android.graphics.Bitmap
import android.net.Uri

interface EditImageRepository  {
    suspend fun prepareImagePreview(uri:Uri):Bitmap?
}