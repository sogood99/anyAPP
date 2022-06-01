package com.example.anyapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.FragmentNewTweetBinding
import com.example.anyapp.util.Constants
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.apache.commons.io.IOUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

/**
 * A simple [Fragment] subclass.
 * Use the [NewTweetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewTweetFragment : Fragment() {
    private lateinit var binding: FragmentNewTweetBinding

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val tweetApi: TweetApi = retrofit.create(TweetApi::class.java)

    private var imageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewTweetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup tweet button
        setupTweet()
    }

    private fun setupTweet() {
        binding.apply {
            // for creating new tweets
//            newTweetButton.setOnClickListener {
//                newTweetButton.visibility = View.GONE
//                newTweet.visibility = View.VISIBLE
//            }

            sendTweetButton.setOnClickListener {
                sendTweet()
//                newTweet.visibility = View.GONE // maybe check if send correctly
//                newTweetButton.visibility = View.VISIBLE
            }

            // for choosing new button
            choosePhotoBtn.setOnClickListener {
                // take picture intent
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                // choose picture intent
                val choosePicture = Intent(Intent.ACTION_GET_CONTENT)
                choosePicture.type = "image/*"

                try {
                    val photoFile = File.createTempFile(
                        "temp_image",
                        ".jpg",
                        context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    )
                    val photoURI = FileProvider.getUriForFile(
                        this.root.context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile
                    )
                    imageFile = photoFile

                    MaterialAlertDialogBuilder(
                        this.root.context,
                        R.style.Theme_Material3_Dark_Dialog
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

                }
            }
        }
    }

    private fun sendTweet() {
        Constants.USER_TOKEN?.let {
            // send file to backend
            val requestBody =
                imageFile?.let { RequestBody.create(MediaType.parse("multipart/form-data"), it) }
            val fileToUpload =
                requestBody?.let { MultipartBody.Part.createFormData("image", imageFile?.name, it) }
            val text = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.newTweetTextLayout.editText?.text.toString()
            )

            val call = tweetApi.tweet(
                it,
                text,
                fileToUpload
            )

            call.enqueue(object : Callback<Tweet> {
                override fun onResponse(
                    call: Call<Tweet>,
                    response: Response<Tweet>
                ) {
                    Log.v("Pity", response.toString())
                    Log.v("Pity", response.body().toString())
                    response.body()?.videoUrl?.let { it1 -> Log.v("Pity", it1) }
                }

                override fun onFailure(call: Call<Tweet>, t: Throwable) {
                    Log.v("Pity", t.toString())
                }
            })
        }
    }

    private val takePictureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // do something
            }
        }

    private val chooseImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // send the file to temp_file aka imageFile
                result.data?.data?.let {
                    val inputStream = context?.contentResolver?.openInputStream(it)
                    val outputStream = FileOutputStream(imageFile)
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, outputStream)
                    }
                }
            }
        }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment NewTweetFragment.
         */
        @JvmStatic
        fun newInstance() = NewTweetFragment().apply {}
    }
}