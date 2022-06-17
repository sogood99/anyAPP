package com.example.anyapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.FragmentProfileBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.ProfileResponse
import com.example.anyapp.util.UserToken
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // if isSelf == true, access profile through user token (and display self related buttons eg edit), else show regular visitor accessing profile
    private var isSelf: Boolean? = null
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
            isSelf = it.getBoolean(ARG_PARAM1)
            userId = it.getInt(ARG_PARAM2)

            if (isSelf == false && userId!! < 0) {
                assert(false) { "Bug, please use profile fragment correctly" }
            }
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

        // if not logged in, set to invisible
        val token = UserToken(this.activity).readToken()
        if (isSelf == false) {
            binding.editProfileButton.visibility = View.GONE
        } else {
            if (token == null) {
                // isSelf == true but token == null => not logged in yet
                binding.root.visibility = View.INVISIBLE
                return
            }

            // set onClick for editProfile
            binding.apply {
                editProfileButton.setOnClickListener {
                    // intent to profileEdit
                    val intent = Intent(root.context, EditProfile::class.java)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        root.context as Activity,
                        Pair.create(profileBkgImg as View, "profileBkgImg"),
                        Pair.create(profileIcon as View, "profileIcon"),
                    )
                    startActivity(intent, options.toBundle())
                }
            }
        }

        getProfile()
        val feedFragment = FeedFragment.newInstance(FeedType.Profile)
        childFragmentManager.beginTransaction().apply {
            replace(R.id.feedFrameLayout, feedFragment)
            commit()
        }

    }

    private fun getProfile() {
        // get account detail from backend
        val userToken = UserToken(this.activity).readToken()
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
                            profileNickname.text = it.profileName
                            profileUsername.text = "@" + it.username
                            if (it.profileInfo == null) {
                                profileInfo.text = "Still New."
                            } else {
                                profileInfo.text = it.profileInfo
                            }
                            profileCreatedDate.text = "Date Created: " + it.createDate

                            // load images
                            val iconUrl = Constants.BASE_URL + "/" + it.userIconUrl
                            Picasso.get().load(iconUrl).into(profileIcon)
                            val bkgUrl = Constants.BASE_URL + "/" + it.userBkgUrl
                            Picasso.get().load(bkgUrl).into(profileBkgImg)
                        }
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Log.v("Pity", t.toString())
                }
            })
        }
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
        fun newInstance(isSelf: Boolean, userId: Int = -1) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, isSelf)
                    putInt(ARG_PARAM2, userId)
                }
            }
    }
}