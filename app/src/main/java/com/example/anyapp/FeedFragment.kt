package com.example.anyapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anyapp.api.TweetApi
import com.example.anyapp.databinding.FragmentFeedBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.FeedType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val OPTION_PARAM = "option"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedFragment : Fragment() {
    private var option: FeedType? = null

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
            option = it.getSerializable(OPTION_PARAM) as FeedType?
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
        val call = tweetApi.getFeed()
        call.enqueue(object : Callback<List<Tweet>> {
            override fun onResponse(call: Call<List<Tweet>>, response: Response<List<Tweet>>) {
                val tweetList = response.body()
                tweetList?.let {
                    adapter.tweets = it
                    adapter.notifyDataSetChanged()

                    binding.homeTweets.adapter = adapter
                    binding.homeTweets.layoutManager = LinearLayoutManager(view.context)
                }
            }

            override fun onFailure(call: Call<List<Tweet>>, t: Throwable) {
                Log.v("Pity", t.toString())
            }
        })
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