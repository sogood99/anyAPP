package com.example.anyapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.databinding.FragmentFeedBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType

private const val OPTION_PARAM = "option"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {
    private var option: FeedType? = null

    private lateinit var binding: FragmentFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            option = it.getSerializable(OPTION_PARAM) as FeedType?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v("Pity", option.toString())

        // Testing out tweets
        var tweetList = mutableListOf(
            Tweet(
                "ABC",
                "id",
                "Fuck Republicans and Democrats",
                Constants.BASE_URL + "/image/test.jpg",
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
        binding.homeTweets.layoutManager = LinearLayoutManager(this.context)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param option Parameter which specifies which feed type (popular, following, notification, or profile)
         * @return A new instance of fragment FeedFragment.
         */
        @JvmStatic
        fun newInstance(option: FeedType) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(OPTION_PARAM, option)
                }
            }
    }
}