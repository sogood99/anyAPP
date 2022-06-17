package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ActivityEditProfileBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.ProfileResponse
import com.example.anyapp.util.UserToken
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // bind backbutton
        binding.toolBar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        // get account detail from backend
        val userToken = UserToken(this).readToken()
        userToken?.let { token ->
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