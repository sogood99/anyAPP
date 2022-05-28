package com.example.anyapp

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ActivityHomeBinding
import com.example.anyapp.util.Constants.Companion.BASE_URL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).build()
    private val tweetApi: TweetApi = retrofit.create(TweetApi::class.java)

    private val TAKE_PICTURE_CODE = 1
    private val CHOOSE_GALLERY_CODE = 2
    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Create binding to activity_home
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // For selecting the Menu Items
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    Log.v("Pity", "Clicked Search")
                    true
                }
                R.id.miLogout -> {
                    Log.v("Pity", "Clicked Logout")
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            Log.v("Pity", "Clicked Home Button")
            true
        }

        // for creating new tweets
        binding.newTweetButton.setOnClickListener { button ->
            binding.newTweetButton.visibility = View.GONE
            binding.newTweet.visibility = View.VISIBLE
            true
        }

        // for choosing new button
        binding.choosePhotoBtn.setOnClickListener { button ->
            // take picture intent
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // choose picture intent
            val choosePicture = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            try {
                MaterialAlertDialogBuilder(
                    this,
                    com.google.android.material.R.style.Theme_Material3_Dark_Dialog
                )
                    .setTitle("Image")
                    .setMessage("Choose Method")
                    .setNegativeButton("Take Picture") { dialog, which ->
                        val photoFile = File.createTempFile(
                            "temp_image",
                            ".jpg",
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        )
                        val photoURI = FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile
                        )
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        imageFile = photoFile

                        startActivityForResult(takePicture, TAKE_PICTURE_CODE)
                    }
                    .setPositiveButton("Choose Gallery") { dialog, which ->
                        startActivityForResult(choosePicture, CHOOSE_GALLERY_CODE)
                    }
                    .show()
            } catch (e: ActivityNotFoundException) {

            }
            true
        }

        // Testing out tweets
        var tweetList = mutableListOf(
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                BASE_URL + "/image/test.jpg",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            ),
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://i.stack.imgur.com/DLadx.png",
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            ),
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                "https://i.imgur.com/DvpvklR.png",
                null
            ),
            Tweet("1223", "nothaId", "Same Bruh", null, null)
        )
        val adapter = TweetAdapter(tweetList)
        binding.homeTweets.adapter = adapter
        binding.homeTweets.layoutManager = LinearLayoutManager(this)


        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            val title = "New One Bites the Dusto"
            val tweet = Tweet("1223", "nothaId", "Same Bruh", null, null)
            tweetList.add(tweet)
            adapter.notifyItemInserted(tweetList.size)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            Log.v("Pity", requestCode.toString())
            if (requestCode == TAKE_PICTURE_CODE) {
                // send file to backend
                imageFile?.let {
                    val requestBody =
                        RequestBody.create(MediaType.parse("multipart/form-data"), it)
                    val fileToUpload =
                        MultipartBody.Part.createFormData("image", it.name, requestBody)
                    val filename = RequestBody.create(MediaType.parse("text/plain"), it.name)

                    val call = tweetApi.tweet(filename, fileToUpload)

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            Log.v("Pity", response.toString())
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Log.v("Pity", t.toString())
                        }
                    }
                    )
                }
            } else if (requestCode == CHOOSE_GALLERY_CODE) {

            }
        }
    }
}