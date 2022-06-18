package com.example.anyapp.feed

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.FragmentFeedBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.UserToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val OPTION_PARAM = "option"
private const val REPLIESID_PARAM = "repliesId"
private const val USERID_PARAM = "userId"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {
    // lateinit
    private var option = FeedType.NotSet
    private var repliesId = -1
    private var userId = -1

    // databinding
    private lateinit var binding: FragmentFeedBinding
    private lateinit var adapter: TweetAdapter

    // for accessing backend api using retrofit
    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val tweetApi: TweetApi = retrofit.create(TweetApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            option = it.getSerializable(OPTION_PARAM) as FeedType
            repliesId = it.getInt(REPLIESID_PARAM)
            userId = it.getInt(USERID_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initializer adapter
        adapter = TweetAdapter(listOf())

        // get feed from backend
        getFeed(view)

        // set onrefreshlistener
        binding.feedSwipeRefreshLayout.setOnRefreshListener {
            getFeed(view)
            binding.feedSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getFeed(view: View) {
        // get the data from backend
        val call = tweetApi.getFeed(
            UserToken(activity).readToken(),
            option,
            if (option == FeedType.Replies) repliesId else null,
            if (option == FeedType.ProfileDetail) userId else null
        )
        call.enqueue(object : Callback<List<Tweet>> {
            override fun onResponse(call: Call<List<Tweet>>, response: Response<List<Tweet>>) {
                val tweetList = response.body()
                Log.v("Pity", tweetList.toString())
                tweetList?.let {
                    adapter.tweets = it
                    adapter.notifyDataSetChanged()

                    binding.homeTweets.adapter = adapter
                    binding.homeTweets.layoutManager = LinearLayoutManager(view.context)
                }
            }

            override fun onFailure(call: Call<List<Tweet>>, t: Throwable) {
                Toast.makeText(context, "Server Connection Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param option Parameter which specifies which feed type (popular, following, notification, replies, or profile)
         * @param repliesId Param which when combined with FeedType.Replies specifies which tweetId to see replies of
         * @return A new instance of fragment FeedFragment.
         */
        @JvmStatic
        fun newInstance(option: FeedType, repliesId: Int = -1, userId: Int = -1) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    assert(!(option == FeedType.Replies && repliesId < 0)) { "Error, negative replies id for FeedType.Replies" }
                    assert(!(option == FeedType.ProfileDetail && userId < 0)) { "Error, negative user id for FeedType.ProfileDetail" }
                    putSerializable(OPTION_PARAM, option)
                    putInt(REPLIESID_PARAM, repliesId)
                    putInt(USERID_PARAM, userId)
                }
            }
    }
}