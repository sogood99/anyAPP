package com.example.anyapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
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


        // For selecting the Menu Items
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    Log.v("Pity", "Clicked Search")
                    true
                }
                R.id.miLogout -> {
                    USER_TOKEN = null
                    // reset adapter
                    val adapter = binding.fragPager.adapter
                    binding.fragPager.adapter = null
                    binding.fragPager.adapter = adapter
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            Log.v("Pity", "Clicked Home Button")
        }

        // setup tweet button
        setupTweet()

        // fragPager stuff: page change
        binding.fragPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // set the homeButton text when change position
                if (position == 0) {
                    binding.homeButton.text = "Home"
                } else if (position == 1) {
                    binding.homeButton.text = "Profile"
                }
            }
        })

        // setup botNavBar when clicked
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
    }

    private fun setupTweet() {
        binding.apply {
            // for creating new tweets
            newTweetButton.setOnClickListener {
                newTweetButton.visibility = View.GONE
                newTweet.visibility = View.VISIBLE
            }

            sendTweetButton.setOnClickListener {
                sendTweet()
                newTweet.visibility = View.GONE // maybe check if send correctly
                newTweetButton.visibility = View.VISIBLE
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
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    )
                    val photoURI = FileProvider.getUriForFile(
                        this.root.context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile
                    )
                    imageFile = photoFile

                    MaterialAlertDialogBuilder(
                        this.root.context,
                        com.google.android.material.R.style.Theme_Material3_Dark_Dialog
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
                    val inputStream = contentResolver.openInputStream(it)
                    val outputStream = FileOutputStream(imageFile)
                    if (inputStream != null) {
                        IOUtils.copy(inputStream, outputStream)
                    }
                }
            }
        }

    private fun sendTweet() {
        USER_TOKEN?.let {
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

    private inner class BottomNavPagerAdapter(
        fa: FragmentActivity,
    ) : FragmentStateAdapter(fa) {
        private val NUM_PAGES = 2

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                FeedFragment.newInstance(FeedType.Popular)
            } else {
                ProfileFragment.newInstance(0)
            }
        }

    }
}