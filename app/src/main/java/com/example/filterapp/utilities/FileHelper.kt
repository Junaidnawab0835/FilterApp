package com.example.filterapp.utilities

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileHelper
{

     fun saveBitmap(bitmap: Bitmap): File
    {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath+ "/${timeStamp}.jpg"
        val imageFile = File(storageDir)
        try {
            val fOut = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return imageFile
    }

}