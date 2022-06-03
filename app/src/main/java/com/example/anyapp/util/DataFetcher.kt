package com.example.anyapp.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.anyapp.BuildConfig
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

// interface for data fetching used in NewTweet among other things sus
// saves the fetched data into temp file, and callback
// takes in a callback after data has been fetched
abstract class DataFetcher(val fragment: Fragment) {
    abstract fun run()
    abstract fun successCallback()
}

// must put as member or initialize in onCreate
// since registerForActivityResult needs to have fragment in state Starting
abstract class ImageFetcher(fragment: Fragment) : DataFetcher(fragment) {
    private var fetchedImageFile: File? = null

    // callbacks for intent results
    private val takePictureResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                successCallback()
            }
        }

    val getImageFile: () -> File? = { fetchedImageFile }

    private val chooseImageResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // send the file to temp_file aka imageFile
                result.data?.data?.let {
                    val inputStream = fragment.context?.contentResolver?.openInputStream(it)
                    val outputStream = FileOutputStream(fetchedImageFile)
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, outputStream)
                    }
                }

                successCallback()
            }
        }

    override fun run() {
        // take picture intent
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // choose picture intent
        val choosePicture = Intent(Intent.ACTION_GET_CONTENT)
        choosePicture.type = "image/*"

        try {
            val photoFile = File.createTempFile(
                "temp_image",
                ".jpg",
                fragment.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            val photoURI = fragment.context?.let {
                FileProvider.getUriForFile(
                    it,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
            }
            fetchedImageFile = photoFile

            fragment.context?.let {
                MaterialAlertDialogBuilder(
                    it,
                    R.style.Base_Theme_Material3_Light_Dialog
                )
                    .setTitle("Image")
                    .setMessage("Choose Method")
                    .setNegativeButton("Take Picture") { dialog, which ->
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        takePictureResult.launch(takePicture)
                    }
                    .setPositiveButton("Choose Gallery") { dialog, which ->
                        chooseImageResult.launch(choosePicture)
                    }
                    .show()
            }

        } catch (e: ActivityNotFoundException) {

        }
    }

}