package com.example.anyapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.FragmentNewTweetBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.ImageFetcher
import com.example.anyapp.util.UserToken
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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

    private val imageFetcher = object : ImageFetcher(this) {
        override fun successCallback() {
            // get the successfully fetched image using getImageFile(), then set to NewTweetFragment.imageFile
            this@NewTweetFragment.imageFile = getImageFile()
        }
    }

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

        // hide keyboard
        binding.apply {
            root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    newTweetTextLayout.clearFocus()

                    val inputMethodManager =
                        view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
    }

    private fun setupTweet() {
        binding.apply {
            sendTweetButton.setOnClickListener {
                sendTweet()
            }

            // for choosing new button
            choosePhotoBtn.setOnClickListener {
                imageFetcher.run()
            }
        }
    }

    private fun sendTweet() {
        val userToken = UserToken(this.activity).readToken()

        userToken?.let { token ->
            // send file to backend
            val requestBody =
                imageFile?.let { file ->
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                    )
                }
            val fileToUpload =
                requestBody?.let {
                    MultipartBody.Part.createFormData(
                        "image",
                        imageFile?.name,
                        it
                    )
                }
            val text = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.newTweetTextLayout.editText?.text.toString()
            )

            val call = tweetApi.tweet(
                token,
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

    fun show() {
        // animate showing
        binding.root.animate().alpha(1.0f).setDuration(100)
    }

    fun hide() {
        binding.root.animate().alpha(0.0f).setDuration(100)
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