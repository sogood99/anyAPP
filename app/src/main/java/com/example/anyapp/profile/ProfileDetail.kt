package com.example.anyapp.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.anyapp.R
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ActivityProfileDetailBinding
import com.example.anyapp.feed.TweetAdapter
import com.example.anyapp.util.Constants
import com.example.anyapp.util.ProfileDetailResponse
import com.example.anyapp.util.UserToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// for when user clicks on userprofile
class ProfileDetail : AppCompatActivity() {
    private lateinit var binding: ActivityProfileDetailBinding

    // lateinit
    private var userId = -1

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // back button
        binding.toolBar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        userId = intent.getIntExtra(TweetAdapter.EXTRA_USER_ID, -1)
        assert(userId >= 0) { "Bug, UserId should be specified" }

        // primarily to check if user is self
        val call = accountApi.getProfileDetail(UserToken(this).readToken(), userId)
        call.enqueue(object : Callback<ProfileDetailResponse> {
            override fun onResponse(
                call: Call<ProfileDetailResponse>,
                response: Response<ProfileDetailResponse>
            ) {
                // set profile
                response.body()?.let { profile ->
                    val profileFragment = ProfileFragment.newInstance(profile.isSelf, userId)
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.profileDetailFragment, profileFragment)
                        commit()
                    }
                }
            }

            override fun onFailure(call: Call<ProfileDetailResponse>, t: Throwable) {
                Log.v("Pity", t.toString())
            }
        })
    }
}