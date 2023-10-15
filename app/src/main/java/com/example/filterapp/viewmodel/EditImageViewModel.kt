package com.example.filterapp.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filterapp.repository.EditImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject

@HiltViewModel
class EditImageViewModel @Inject constructor(private val editImageRepository: EditImageRepository) : ViewModel()
{

    val filteredBitmap = MutableLiveData<Bitmap>()
    var brightnessValue = -1
    var contrastValue = 1
    var blur = 1
    private val imagePreviewDataState = MutableLiveData<ImagePreviewDataState>()
    val uiState: LiveData<ImagePreviewDataState> get() = imagePreviewDataState

    fun prepareImagePreview(imageUri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            emitImagePreviewUiState(isLoading = true)
            editImageRepository.prepareImagePreview(imageUri)
        }.onSuccess { bitmap ->
            if (bitmap != null)
            {
                emitImagePreviewUiState(bitmap = bitmap)
            } else
            {
                emitImagePreviewUiState(error = "Unable to Prepare Image")
            }
        }.onFailure {
            emitImagePreviewUiState(error = it.message.toString())
        }
    }

private fun emitImagePreviewUiState(
    isLoading: Boolean = false,
    bitmap: Bitmap? = null,
    error: String? = null,
)
{
    val dataState = ImagePreviewDataState(isLoading, bitmap, error)
    imagePreviewDataState.postValue(dataState)

}

    fun applyGreyScale() = viewModelScope.launch(Dispatchers.IO) {
        val mat = Mat()
        Utils.bitmapToMat(filteredBitmap.value, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        val grayscaleBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, grayscaleBitmap)
        filteredBitmap.postValue(grayscaleBitmap)
    }


    fun applyEffect(originalBitmap:Bitmap)=viewModelScope.launch(Dispatchers.IO) {
        val src = Mat(originalBitmap.width, originalBitmap.height, CvType.CV_8UC1)
        Utils.bitmapToMat(originalBitmap, src) //Brightness and Contrast
        src.convertTo(src, -1, contrastValue.toDouble(), brightnessValue.toDouble()) //Blur Affect
        val s = Size(blur.toDouble(), blur.toDouble())
        Imgproc.blur(src, src, s)
        val result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(src, result)
        filteredBitmap.postValue(result)
    }

    fun applyRGB(originalBitmap:Bitmap)
    {
        filteredBitmap.postValue(originalBitmap)
    }


    data class ImagePreviewDataState(
    val isLoading: Boolean,
    val bitmap: Bitmap?,
    val error: String?,
)

}