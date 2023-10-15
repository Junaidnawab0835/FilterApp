package com.example.filterapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.filterapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val KEY_IMAGE_URI = "imageURI"
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var mCurrentPhotoPath:String
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //
        galleryActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
              result.data.let {
                  imageUri -> Intent(applicationContext,EditImageActivity::class.java).also {
                      editImageIntent ->
                  editImageIntent.putExtra(KEY_IMAGE_URI, imageUri?.data)
                  startActivity(editImageIntent)
                }
              }
            }
        }
        //
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                        val intent = Intent(this@MainActivity, EditImageActivity::class.java)
                        intent.putExtra(KEY_IMAGE_URI, photoURI)
                        startActivity(intent)
            }
        }
        setListener()
    }

    private fun setListener() {
        binding.btnChooseImage.setOnClickListener {
           showSelectImageDialog()
        }
    }

    private fun showSelectImageDialog()
    {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.camera_gallery_select_dialog)
        val cameraLyt = bottomSheetDialog.findViewById<LinearLayout>(R.id.camera_lyt)
        val galleryLyt = bottomSheetDialog.findViewById<LinearLayout>(R.id.gallery_lyt)
        cameraLyt!!.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takesPictureIntent ->
                if (takesPictureIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    try {
                        val photoFile:File? = createImageFile()
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoURI = FileProvider.getUriForFile(
                                this,
                                "com.example.filterapp.fileprovider",
                                photoFile
                            )
                            takesPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            cameraActivityResultLauncher?.launch(takesPictureIntent)
                            bottomSheetDialog.dismiss()
                        }
                    } catch (e: java.lang.Exception) {
                          Toast.makeText(this, "Error$e", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        galleryLyt!!.setOnClickListener {
            Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .also {
                    pickerIntent->
                    pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    bottomSheetDialog.dismiss()
                    galleryActivityResultLauncher!!.launch(pickerIntent)
                }
        }
        bottomSheetDialog.show()
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val df = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss_mm", Locale.getDefault())
        val formattedDate = df.format(Date())
        val imageFileName = "Image_" + formattedDate + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

}