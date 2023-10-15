package com.example.filterapp.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

class EditImageRepositoryImpl(private val context: Context):EditImageRepository{
    private fun getInputStreamFromUri(uri:Uri): InputStream? {
            return context.contentResolver.openInputStream(uri)
    }

    override suspend fun prepareImagePreview(uri: Uri): Bitmap? {
        getInputStreamFromUri(uri)?.let {
            inputStream ->
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val width = context.resources.displayMetrics.widthPixels
            val height = originalBitmap.height*width / originalBitmap.width
            return Bitmap.createScaledBitmap(originalBitmap,width,height,false)
        }
        return null
    }

}