package com.example.anyapp.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.anyapp.BuildConfig
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

// interface for data fetching used in NewTweet among other things sus
// saves the fetched data into temp file, and callback
// takes in a callback after data has been fetched
abstract class DataFetcher(
    protected val activity: Activity, protected val registry: ActivityResultRegistry
) : DefaultLifecycleObserver {

    abstract fun run()
    abstract fun successCallback()
}

// must put as member or initialize in onCreate
// since registerForActivityResult needs to have ActivityResultRegistry in state Starting
// and need activity for Dialog and External File and stuff
abstract class ImageFetcher(activity: Activity, registry: ActivityResultRegistry) :
    DataFetcher(activity, registry) {
    private lateinit var takePictureResult: ActivityResultLauncher<Intent>
    private lateinit var chooseImageResult: ActivityResultLauncher<Intent>

    private var fetchedImageFile: File? = null
    val getImageFile: () -> File? = { fetchedImageFile }

    // for generating unique key
    companion object {
        private val count: AtomicInteger = AtomicInteger(1)
    }

    private val id = count.incrementAndGet()

    override fun onCreate(owner: LifecycleOwner) {
        // initialize the ActivityResultLaunchers
        takePictureResult = registry.register(
            "takePicture$id",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Handle the returned Uri
            successCallback()
        }
        chooseImageResult = registry.register(
            "chooseImage$id",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the returned Uri
            if (result.resultCode == Activity.RESULT_OK) {
                // send the file to temp_file aka imageFile
                result.data?.data?.let {
                    val inputStream = activity.contentResolver.openInputStream(it)
                    val outputStream = FileOutputStream(fetchedImageFile)
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, outputStream)
                    }
                }
                successCallback()
            }
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
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            val photoURI = FileProvider.getUriForFile(
                activity,
                BuildConfig.APPLICATION_ID + ".provider",
                photoFile
            )

            fetchedImageFile = photoFile

            MaterialAlertDialogBuilder(
                activity,
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

        } catch (e: ActivityNotFoundException) {
            Log.v("Pity", e.toString())
        }
    }
}

// must put as member or initialize in onCreate
// since registerForActivityResult needs to have ActivityResultRegistry in state Starting
// and need activity for Dialog and External File and stuff
abstract class VideoFetcher(activity: Activity, registry: ActivityResultRegistry) :
    DataFetcher(activity, registry) {
    private lateinit var takeVideoResult: ActivityResultLauncher<Intent>
    private lateinit var chooseVideoResult: ActivityResultLauncher<Intent>

    private var fetchedVideoFile: File? = null
    val getVideoFile: () -> File? = { fetchedVideoFile }

    // for generating unique key
    companion object {
        private val count: AtomicInteger = AtomicInteger(1)
    }

    private val id = count.incrementAndGet()

    override fun onCreate(owner: LifecycleOwner) {
        // initialize the ActivityResultLaunchers
        takeVideoResult = registry.register(
            "takeVideo$id",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Handle the returned Uri
            successCallback()
        }
        chooseVideoResult = registry.register(
            "chooseVideo$id",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the returned Uri
            if (result.resultCode == Activity.RESULT_OK) {
                // send the file to temp_file aka imageFile
                result.data?.data?.let {
                    val inputStream = activity.contentResolver.openInputStream(it)
                    val outputStream = FileOutputStream(fetchedVideoFile)
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, outputStream)
                    }
                }
                successCallback()
            }
        }
    }

    override fun run() {
        // take video intent
        val takeVideo = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        // choose video intent
        val chooseVideo = Intent(Intent.ACTION_GET_CONTENT)
        chooseVideo.type = "video/*"

        try {
            val videoFile = File.createTempFile(
                "temp_video",
                ".mp4",
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            val videoUri = FileProvider.getUriForFile(
                activity,
                BuildConfig.APPLICATION_ID + ".provider",
                videoFile
            )

            fetchedVideoFile = videoFile

            MaterialAlertDialogBuilder(
                activity,
                R.style.Base_Theme_Material3_Light_Dialog
            )
                .setTitle("Video")
                .setMessage("Choose Method")
                .setNegativeButton("Take Video") { dialog, which ->
                    takeVideo.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                    takeVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    takeVideoResult.launch(takeVideo)
                }
                .setPositiveButton("Choose Gallery") { dialog, which ->
                    chooseVideoResult.launch(chooseVideo)
                }
                .show()

        } catch (e: ActivityNotFoundException) {
            Log.v("Pity", e.toString())
        }
    }
}
