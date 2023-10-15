package com.example.filterapp


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.filterapp.databinding.ActivityEditImageBinding
import com.example.filterapp.utilities.FileHelper
import com.example.filterapp.viewmodel.EditImageViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class EditImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditImageBinding
    private val viewModel:EditImageViewModel by lazy {
         ViewModelProvider(this)[EditImageViewModel::class.java]
    }
    private lateinit var originalBitmap: Bitmap
    private lateinit var originalCaptureBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObservers()
        intent.getParcelableExtra<Uri>(MainActivity.KEY_IMAGE_URI)
            ?.let { viewModel.prepareImagePreview(it) }
        binding.adjustOption.setOnClickListener {
            binding.settingsLyt.visibility = View.GONE
            viewModel.applyEffect(originalBitmap)
            showBottomSheetOptions()
        }

        binding.grayscaleOption.setOnClickListener{
            applyGreyScale()
        }
        binding.saveImageBtn.setOnClickListener{
            viewModel.filteredBitmap.value?.let { it1 ->

                lifecycleScope.launch {
                    FileHelper.saveBitmap(it1).let {
                       Toast.makeText(this@EditImageActivity,"Image Saved",Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        binding.shareImageBtn.setOnClickListener{
            shareImage()
        }
        binding.saturationOption.setOnClickListener{
            applySaturation()
        }
        binding.restoreOriginalImageBtn.setOnClickListener{
            viewModel.filteredBitmap.postValue(originalCaptureBitmap)
        }

    }


    private fun applyGreyScale()
    {
        if (binding.grayscaleOption.text.equals(resources.getString(R.string.greyscale)))
        {
            binding.grayscaleOption.text = resources.getString(R.string.rgb)
            binding.grayscaleOption.compoundDrawableTintList = null
            val drawable = resources.getDrawable(R.drawable.rgb_icon)
            binding.grayscaleOption.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            viewModel.applyGreyScale()
        }
        else
        {
            binding.grayscaleOption.text = resources.getString(R.string.greyscale)
            binding.grayscaleOption.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            val drawable = resources.getDrawable(R.drawable.greyscale)
            binding.grayscaleOption.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            viewModel.applyRGB(originalBitmap)
        }
    }

    private fun applySaturation()
    {
        if(!binding.imagePreview.saturation.equals(2F)) {
            binding.imagePreview.saturation = 2F
            binding.imagePreview.isDrawingCacheEnabled = true
            viewModel.filteredBitmap.value = binding.imagePreview.drawingCache
        }
    }

    private fun showBottomSheetOptions()
    {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentBottomSheet)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        val lyt = bottomSheetDialog.findViewById<LinearLayout>(R.id.lyt_linear)
        lyt!!.setBackgroundResource(android.R.color.transparent)
        bottomSheetDialog.setCancelable(false)
        val brightnessSeekBar = bottomSheetDialog.findViewById<SeekBar>(R.id.brightnessSeekBar)
        val contrastSeekBar = bottomSheetDialog.findViewById<SeekBar>(R.id.contrastSeekBar)
        val blurSeekBar = bottomSheetDialog.findViewById<SeekBar>(R.id.blurSeekBar)
        val btnOk = bottomSheetDialog.findViewById<ImageView>(R.id.btnOk)
        val btnClose = bottomSheetDialog.findViewById<ImageView>(R.id.btnClose)
        bottomSheetDialog.show()
        brightnessSeekBar!!.progress = viewModel.brightnessValue
        brightnessSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?)
            {
                if (seekBar!!.progress == 0)
                {
                    viewModel.brightnessValue = -1
                } else
                {
                    viewModel.brightnessValue = seekBar.progress
                }
                viewModel.applyEffect(originalBitmap)
            }
        })
        contrastSeekBar!!.progress = viewModel.contrastValue
        contrastSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?)
            {
                viewModel.contrastValue = seekBar!!.progress
                if (viewModel.contrastValue == 0)
                {
                    viewModel.contrastValue = 1
                }
                viewModel.applyEffect(originalBitmap)
            }
        })
        blurSeekBar!!.progress = viewModel.brightnessValue
        blurSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?)
            {
                viewModel.blur = seekBar!!.progress
                if (viewModel.blur == 0)
                {
                    viewModel.blur = 1
                }
                viewModel.applyEffect(originalBitmap)
            }
        })

        btnOk!!.setOnClickListener {
            viewModel.brightnessValue = -1
            viewModel.contrastValue = 1
            viewModel.blur = 1
            originalBitmap = viewModel.filteredBitmap.value!!
            binding.settingsLyt.visibility = View.VISIBLE
            bottomSheetDialog.dismiss()
        }

        btnClose!!.setOnClickListener {
            viewModel.filteredBitmap.value = originalBitmap
            binding.settingsLyt.visibility = View.VISIBLE
            bottomSheetDialog.dismiss()
        }
    }

    private fun setObservers(){
        viewModel.uiState.observe(this) {
            val dataState = it ?: return@observe
            binding.progressBar.visibility = if (dataState.isLoading) View.VISIBLE else View.GONE
            dataState.bitmap?.let { bitmap ->
                originalBitmap = bitmap
                originalCaptureBitmap = bitmap
                viewModel.filteredBitmap.value = bitmap
                binding.imagePreview.setImageBitmap(bitmap)
            } ?: kotlin.run {
                dataState.error.let {
                        error ->
                        Log.d("error",error.toString())
                }
            }
        }
        viewModel.filteredBitmap.observe(
            this
        ) { bitmap ->
            binding.imagePreview.setImageBitmap(bitmap)
        }
    }

    private fun shareImage(){

        lifecycleScope.launch(Dispatchers.IO){
            kotlin.runCatching {
                FileHelper.saveBitmap(viewModel.filteredBitmap.value!!)
            }.onFailure {
                withContext(Dispatchers.Main)
                {
                    showErrorDialog()
                }
            }.onSuccess {
                withContext(Dispatchers.Main)
                {
                    val contentUri = FileProvider.getUriForFile(this@EditImageActivity, "com.example.filterapp.fileprovider", it)
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/jpeg"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share Image"))
                }

            }


        }
    }

    private fun showErrorDialog()
    {
        AlertDialog.Builder(this).setMessage(resources.getString(R.string.error_msg)).setPositiveButton("Ok",null).show()
    }

}