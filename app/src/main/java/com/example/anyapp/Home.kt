package com.example.anyapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.ActivityHomeBinding
import com.example.anyapp.util.Constants.Companion.BASE_URL
import com.example.anyapp.util.Constants.Companion.USER_TOKEN
import com.example.anyapp.util.FeedType
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

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
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

        // put in feed fragment
        val pagerAdapter = BottomNavPagerAdapter(this)
        binding.fragPager.adapter = pagerAdapter

        binding.botNavBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.navHome -> {
                    binding.fragPager.currentItem = 0
                    true
                }
                R.id.navProfile -> {
                    binding.fragPager.currentItem = 1
                    true
                }
                else -> {
                    false
                }
            }
        }


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

        setupTweet()

    }

    private fun setupTweet() {
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
            val choosePicture = Intent(Intent.ACTION_GET_CONTENT)
            choosePicture.type = "image/*"

            try {
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
                imageFile = photoFile

                MaterialAlertDialogBuilder(
                    this,
                    com.google.android.material.R.style.Theme_Material3_Dark_Dialog
                )
                    .setTitle("Image")
                    .setMessage("Choose Method")
                    .setNegativeButton("Take Picture") { dialog, which ->
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_GALLERY_CODE || requestCode == TAKE_PICTURE_CODE) {

                if (requestCode == CHOOSE_GALLERY_CODE) {
                    // send the file to temp_file aka imageFile
                    data?.data?.let {
                        val inputStream = contentResolver.openInputStream(it)
                        val outputStream = FileOutputStream(imageFile)
                        if (inputStream != null) {
                            IOUtils.copy(inputStream, outputStream)
                        }
                    }
                }

                // send file to backend
                imageFile?.let {
                    val requestBody =
                        RequestBody.create(MediaType.parse("multipart/form-data"), it)
                    val fileToUpload =
                        MultipartBody.Part.createFormData("image", it.name, requestBody)
//                    val filename = RequestBody.create(MediaType.parse("text/plain"), it.name)
                    val username = RequestBody.create(MediaType.parse("text/plain"), "abc")

                    val call = tweetApi.tweet(
                        USER_TOKEN,
                        username,
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
        }
    }

    private inner class BottomNavPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        private val NUM_PAGES = 2

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            Log.v("Pity", position.toString())
            return if (position == 0) {
                FeedFragment.newInstance(FeedType.Popular)
            } else {
                ProfileFragment.newInstance(0)
            }
        }
    }

}