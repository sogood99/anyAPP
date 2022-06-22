package com.example.anyapp

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.anyapp.feed.Tweet
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.FragmentNewTweetBinding
import com.example.anyapp.draft.Draft
import com.example.anyapp.draft.DraftList
import com.example.anyapp.util.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

private const val ISREPLY_PARAM = "isReply"
private const val REPLYID_PARAM = "replyId"

/**
 * A simple [Fragment] subclass.
 * Use the [NewTweetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewTweetFragment : Fragment() {
    private lateinit var binding: FragmentNewTweetBinding

    private var isReply: Boolean? = null
    private var replyId: Int? = null

    private var currentReplyId: Int? = null // for draft
    private var currentDraft: Draft? = null

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val tweetApi: TweetApi = retrofit.create(TweetApi::class.java)

    private var imageFile: File? = null
    private lateinit var imageFetcher: ImageFetcher
    private var videoFile: File? = null
    private lateinit var videoFetcher: VideoFetcher
    private var audioFile: File? = null
    private lateinit var audioFetcher: AudioFetcher
    private var location: String = ""
    private lateinit var locationFetcher: LocationFetcher

    // when clicked tweet send
    private var onTweetCallback: () -> Unit = {}

    // auto save timer
    private var autoSaveTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isReply = it.getBoolean(ISREPLY_PARAM)
            replyId = it.getInt(REPLYID_PARAM)
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

        imageFetcher =
            object : ImageFetcher(requireActivity(), requireActivity().activityResultRegistry) {
                override fun successCallback() {
                    // get the successfully fetched image using getImageFile(), then set to NewTweetFragment.imageFile
                    this@NewTweetFragment.imageFile = getImageFile()
                    binding.deleteImageButton.visibility = View.VISIBLE
                }
            }
        videoFetcher =
            object : VideoFetcher(requireActivity(), requireActivity().activityResultRegistry) {
                override fun successCallback() {
                    this@NewTweetFragment.videoFile = getVideoFile()
                    binding.deleteVideoButton.visibility = View.VISIBLE
                }
            }
        audioFetcher =
            object : AudioFetcher(requireActivity(), requireActivity().activityResultRegistry) {
                override fun successCallback() {
                    this@NewTweetFragment.audioFile = getAudioFile()
                    binding.deleteAudioButton.visibility = View.VISIBLE
                }
            }
        locationFetcher =
            object : LocationFetcher(requireActivity(), requireActivity().activityResultRegistry) {
                override fun successCallback() {
                    this@NewTweetFragment.location = getLocation()
                    binding.deleteLocationButton.visibility = View.VISIBLE
                }
            }
        lifecycle.addObserver(imageFetcher)
        lifecycle.addObserver(videoFetcher)
        lifecycle.addObserver(audioFetcher)
        lifecycle.addObserver(locationFetcher)

        // setup tweet button
        setupTweet()

        // set currentId based on og value
        resetCurrentReplyId()

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

    override fun onDestroy() {
        super.onDestroy()
        autoSaveTimer?.cancel()
    }

    private fun setupTweet() {
        binding.apply {
            sendTweetButton.setOnClickListener {
                sendTweet()
            }

            saveTweetButton.setOnClickListener {
                saveTweet()
            }

            // for deleteButtons
            deleteImageButton.setOnClickListener {
                imageFile = null
                hideButton(it)
                Toast.makeText(context, "Image Deleted", Toast.LENGTH_SHORT).show()
            }
            deleteVideoButton.setOnClickListener {
                videoFile = null
                hideButton(it)
                Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show()
            }
            deleteAudioButton.setOnClickListener {
                audioFile = null
                hideButton(it)
                Toast.makeText(context, "Audio Deleted", Toast.LENGTH_SHORT).show()
            }
            deleteLocationButton.setOnClickListener {
                location = ""
                hideButton(it)
                Toast.makeText(context, "Location Deleted", Toast.LENGTH_SHORT).show()
            }

            clearButton.setOnClickListener {
                resetTweet()
            }

            // for fetch resource button
            imageButton.setOnClickListener {
                imageFetcher.run()
            }
            videoButton.setOnClickListener {
                videoFetcher.run()
            }
            audioButton.setOnClickListener {
                audioFetcher.run()
            }
            locationButton.setOnClickListener {
                locationFetcher.run()
            }
        }
    }

    private fun saveTweet() {
        currentDraft = DraftList().add(
            Draft(
                binding.newTweetTextLayout.editText?.text.toString(),
                if (isReply == true) replyId else null,
                imageFile,
                videoFile,
                audioFile,
                location
            )
        )
        Toast.makeText(context, "Tweet Saved", Toast.LENGTH_SHORT).show()
    }

    private fun sendTweet() {
        val userToken = UserToken(this.activity).readToken()
        if (userToken == null) {
            Toast.makeText(context, "Login to Send Tweet", Toast.LENGTH_LONG).show()
        }

        userToken?.let { token ->
            // process the tweet Parts/fields
            val textContent = RequestBody.create(
                MediaType.parse("text/plain"),
                binding.newTweetTextLayout.editText?.text.toString()
            )

            val locationContent = RequestBody.create(
                MediaType.parse("text/location"),
                location
            )

            // send image to backend
            val imageBody =
                imageFile?.let { file ->
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                    )
                }
            val imageMultipartBody =
                imageBody?.let { body ->
                    MultipartBody.Part.createFormData(
                        "image",
                        imageFile?.name,
                        body
                    )
                }

            // send video to backend
            val videoBody =
                videoFile?.let { file ->
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                    )
                }
            val videoMultipartBody =
                videoBody?.let { body ->
                    MultipartBody.Part.createFormData(
                        "video",
                        videoFile?.name,
                        body
                    )
                }

            // send audio to backend
            val audioBody =
                audioFile?.let { file ->
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                    )
                }
            val audioMultipartBody =
                audioBody?.let { body ->
                    MultipartBody.Part.createFormData(
                        "audio",
                        audioFile?.name,
                        body
                    )
                }

            val call = tweetApi.tweet(
                token,
                textContent,
                currentReplyId,
                imageMultipartBody,
                videoMultipartBody,
                audioMultipartBody,
                locationContent
            )

            call.enqueue(object : Callback<Tweet> {
                override fun onResponse(
                    call: Call<Tweet>,
                    response: Response<Tweet>
                ) {
                    Toast.makeText(context, "Sent Tweet", Toast.LENGTH_LONG).show()
                    currentDraft?.let { DraftList().remove(it) }
                    resetTweet()
                    Timer().schedule(100) {
                        onTweetCallback()
                    }
                }

                override fun onFailure(call: Call<Tweet>, t: Throwable) {
                    Toast.makeText(context, "Error: " + t.toString(), Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    fun show() {
        // animate showing
        binding.root.animate().alpha(1.0f).duration = 100

        // start automatic saving
        autoSaveTimer = Timer()
        autoSaveTimer?.let {
            it.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        currentDraft?.let { DraftList().remove(it) }
                        saveTweet()
                    }
                }
            }, 10000, 15000)
        }
    }

    fun hide() {
        binding.root.animate().alpha(0.0f).duration = 100

        // stop automatic saving
        autoSaveTimer?.cancel()
        autoSaveTimer = null
    }

    fun setTweetCallback(callback: () -> Unit) {
        onTweetCallback = callback
    }

    fun setNewTweet(draft: Draft) {
        currentDraft = draft
        // set newTweet according to draft
        binding.apply {
            newTweetTextLayout.editText?.setText(draft.text)
            imageFile = draft.imageFile
            videoFile = draft.videoFile
            audioFile = draft.audioFile
            location = draft.location

            setCurrentReplyId(draft.replyId)

            if (imageFile != null) {
                showButton(deleteImageButton)
            } else {
                hideButton(deleteImageButton)
            }
            if (videoFile != null) {
                showButton(deleteVideoButton)
            } else {
                hideButton(deleteVideoButton)
            }
            if (audioFile != null) {
                showButton(deleteAudioButton)
            } else {
                hideButton(deleteAudioButton)
            }
            if (location != "") {
                showButton(deleteLocationButton)
            } else {
                hideButton(deleteLocationButton)
            }
        }
    }

    private fun showButton(view: View) {
        view.visibility = View.VISIBLE
    }

    private fun hideButton(view: View) {
        view.visibility = View.INVISIBLE
    }

    private fun setCurrentReplyId(newId: Int?) {
        // set it based on newId
        currentReplyId = newId

        binding.apply {
            if (currentReplyId != null && currentReplyId!! >= 0) {
                indicatorText.text = "Reply #$currentReplyId"
            } else {
                indicatorText.text = "New Tweet"
            }
        }
    }

    private fun resetCurrentReplyId() {
        // set it based on original value (isReply and replyId)
        if (isReply == false) {
            currentReplyId = null
        } else {
            currentReplyId = replyId
        }

        binding.apply {
            if (currentReplyId != null && currentReplyId!! >= 0) {
                indicatorText.text = "Reply #$currentReplyId"
            } else {
                indicatorText.text = "New Tweet"
            }
        }
    }

    private fun resetTweet() {
        // reset newTweet
        binding.apply {
            newTweetTextLayout.editText?.text?.clear()
            imageFile = null
            videoFile = null
            audioFile = null
            location = ""
            currentDraft = null
            hideButton(deleteImageButton)
            hideButton(deleteVideoButton)
            hideButton(deleteAudioButton)
            hideButton(deleteLocationButton)

            resetCurrentReplyId()

            newTweetTextLayout.clearFocus()
            val inputMethodManager =
                root.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(root.windowToken, 0)
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
        fun newInstance(isReply: Boolean, replyId: Int = -1) = NewTweetFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ISREPLY_PARAM, isReply)
                putInt(REPLYID_PARAM, replyId)
            }
        }
    }
}