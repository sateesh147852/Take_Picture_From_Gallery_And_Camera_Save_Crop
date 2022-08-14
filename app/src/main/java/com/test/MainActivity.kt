package com.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.test.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imagePath: String

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.imageView.setImageURI(Uri.parse(imagePath))
            }
        }

    var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                saveImageIntoGallery(result.data?.data)
            }
        }

    var cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val result = CropImage.getActivityResult(result.data)
                binding.imageView.setImageURI(result.uri)
            }
        }

    private fun saveImageIntoGallery(data: Uri?) {
        createFileDirectory()
        val fileName = "MyGalleryPictures/"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val galleryFile =
            File("$storageDir/$fileName", "takenImage" + System.currentTimeMillis() + ".png")
        val fileOutputStream = FileOutputStream(galleryFile)
        val inputStream: InputStream = contentResolver.openInputStream(data!!)!!
        copyStream(inputStream, fileOutputStream)
        inputStream.close()
        fileOutputStream.close()

        val cropIntent = CropImage.activity(data)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(16,9)
            .getIntent(this)

        cropLauncher.launch(cropIntent)

    }

    private fun createFileDirectory() {
        val fileName = "MyGalleryPictures"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val directory = File("$storageDir/$fileName")
        if (!directory.isDirectory) {
            directory.mkdirs()
        }

    }

    private fun copyStream(inputStream: InputStream, fileOutputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var data: Int
        while ((inputStream.read(buffer).also { data = it }) != -1) {
            fileOutputStream.write(buffer, 0, data)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btGet.setOnClickListener {
            //takePicture()
            takePictureFromGallery()
        }

    }

    private fun takePictureFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        resultLauncherGallery.launch(intent)
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File
        try {
            file = createImageFile()
            val photoUri = FileProvider.getUriForFile(this, "com.test.provider", file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            resultLauncher.launch(intent)

        } catch (e: Exception) {

        }
    }

    private fun createImageFile(): File {
        val fileName = "MyPictures"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(fileName, ".jpg", storageDir)
        imagePath = file.absolutePath
        return file
    }
}
