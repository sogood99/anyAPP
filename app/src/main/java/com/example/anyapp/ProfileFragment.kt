package com.example.anyapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.FragmentProfileBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.Constants.Companion.USER_TOKEN
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.ProfileResponse
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var userId: Int? = null

    private lateinit var binding: FragmentProfileBinding

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProfile()
        val feedFragment = FeedFragment.newInstance(FeedType.Profile)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.feedFrameLayout, feedFragment)
            commit()
        }
    }

    private fun getProfile() {
        // get account detail from backend
        val call = accountApi.getProfile(USER_TOKEN)
        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                Log.v("Pity", response.body().toString())
                response.body()?.let {
                    binding.apply {
                        profileNickname.text = it.profileName
                        profileUsername.text = it.username
                        if (it.profileInfo == null){
                            profileInfo.text = "Still New."
                        }else{
                            profileInfo.text = it.profileInfo
                        }
                        profileCreatedDate.text = it.createDate

                        val url = Constants.BASE_URL + "/" + it.userIconUrl
                        Picasso.get().load(url).into(profileIcon);
                    }
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.v("Pity", t.toString())
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param userId Integer to access which profile.
         * @return A new instance of fragment ProfileFragment.
         */
        @JvmStatic
        fun newInstance(userId: Int) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, userId)
                }
            }
    }
}