package com.example.anyapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ActivityEditProfileBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.ImageFetcher
import com.example.anyapp.util.ProfileResponse
import com.example.anyapp.util.UserToken
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class EditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)

    // images for profileIcon and profileBkgImg w/ corresponding fetchers
    private lateinit var profileIconFetcher: ImageFetcher
    private lateinit var profileBkgImgFetcher: ImageFetcher
    private var profileIconFile: File? = null
    private var profileBkgImgFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup fetchers
        profileBkgImgFetcher = object : ImageFetcher(this@EditProfile, activityResultRegistry) {
            override fun successCallback() {
                // get the successfully fetched image using getImageFile(), then set to NewTweetFragment.imageFile
                profileBkgImgFile = getImageFile()
                Log.v("PityFile", profileIconFile.toString())
                profileBkgImgFile?.let { Picasso.get().load(it).into(binding.profileBkgImg) }
            }
        }
        profileIconFetcher = object : ImageFetcher(this@EditProfile, activityResultRegistry) {
            override fun successCallback() {
                // get the successfully fetched image using getImageFile(), then set to NewTweetFragment.imageFile
                profileIconFile = getImageFile()
                Log.v("PityFile", profileIconFile.toString())
                profileIconFile?.let { Picasso.get().load(it).into(binding.profileIcon) }
            }
        }
        lifecycle.addObserver(profileBkgImgFetcher)
        lifecycle.addObserver(profileIconFetcher)

        binding.apply {
            // bind backButton
            toolBar.setNavigationOnClickListener {
                finishAfterTransition()
            }

            // hide keyboard
            root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    profileName.clearFocus()
                    profileInfo.clearFocus()

                    val inputMethodManager =
                        view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            // imageView onclicks
            profileIcon.setOnClickListener {
                profileIconFetcher.run()
            }
            profileBkgImg.setOnClickListener {
                profileBkgImgFetcher.run()
            }

        }

        // get account detail from backend
        val userToken = UserToken(this).readToken()
        userToken?.let { token ->
            // bind submitButton
            binding.apply {
                submitButton.setOnClickListener {
                    val profileNameText = RequestBody.create(
                        MediaType.parse("text/plain"),
                        profileName.editText?.text.toString()
                    )
                    val profileInfoText = RequestBody.create(
                        MediaType.parse("text/plain"),
                        profileInfo.editText?.text.toString()
                    )

                    // user icon
                    val profileIconBody =
                        profileIconFile?.let { file ->
                            RequestBody.create(
                                MediaType.parse("multipart/form-data"),
                                file
                            )
                        }
                    val profileIconMultipartBody =
                        profileIconBody?.let { body ->
                            MultipartBody.Part.createFormData(
                                "userIcon",
                                profileIconFile?.name,
                                body
                            )
                        }

                    // user bkg img
                    val profileBkgImgBody =
                        profileBkgImgFile?.let { file ->
                            RequestBody.create(
                                MediaType.parse("multipart/form-data"),
                                file
                            )
                        }
                    val profileBkgImgMultipartBody =
                        profileBkgImgBody?.let { body ->
                            MultipartBody.Part.createFormData(
                                "userBkgImg",
                                profileBkgImgFile?.name,
                                body
                            )
                        }

                    val call = accountApi.updateProfile(
                        token,
                        profileNameText,
                        profileInfoText,
                        profileIconMultipartBody,
                        profileBkgImgMultipartBody
                    )

                    call.enqueue(object : Callback<ProfileResponse> {
                        override fun onResponse(
                            call: Call<ProfileResponse>,
                            response: Response<ProfileResponse>
                        ) {
                            Log.v("Pity", response.toString())
                            Log.v("Pity", response.body().toString())

                            // transition back
                            finishAfterTransition()
                        }

                        override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                            Log.v("Pity", t.toString())
                        }
                    })
                }
            }

            val call = accountApi.getProfile(token)
            call.enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    Log.v("Pity", response.body().toString())
                    response.body()?.let {
                        binding.apply {
                            profileName.editText?.setText(it.profileName)
                            if (it.profileInfo == null) {
                                profileInfo.editText?.setText("Still New.")
                            } else {
                                profileInfo.editText?.setText(it.profileInfo)
                            }

                            // load images
                            val iconUrl = Constants.BASE_URL + "/" + it.userIconUrl
                            Picasso.get().load(iconUrl).into(profileIcon)
                            val bkgUrl = Constants.BASE_URL + "/" + it.userBkgUrl
                            Picasso.get().load(bkgUrl).into(profileBkgImg)
                        }
                    }
                    startPostponedEnterTransition()
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.v("Pity", t.toString())
                }
            })
        }
    }
}