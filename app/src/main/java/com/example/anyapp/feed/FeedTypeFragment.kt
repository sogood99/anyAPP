package com.example.anyapp.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.anyapp.databinding.FragmentFeedTypeBinding
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.UserToken
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Used to control FeedFragment for when option == Popular
 */
class FeedTypeFragment : Fragment() {
    // dataBinding
    private lateinit var binding: FragmentFeedTypeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // put in feed fragment
        val pagerAdapter = FeedTypeAdapter(this)
        binding.apply {
            feedTypePager.adapter = pagerAdapter
            TabLayoutMediator(tabLayout, feedTypePager) { tab, position ->
                var showText = ""
                if (position == RECENT_POS) {
                    showText = "Recent"
                } else if (position == POPULAR_POS) {
                    showText = "Popular"
                } else if (position == FOLLOWING_POS) {
                    showText = "Following"
                }
                tab.text = showText
            }.attach()
        }
    }

    private inner class FeedTypeAdapter(
        fa: Fragment,
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return if (UserToken(this@FeedTypeFragment.activity).readToken() == null) {
                2
            } else {
                3
            }
        }

        override fun createFragment(position: Int): Fragment {
            if (position == RECENT_POS) {
                return FeedFragment.newInstance(FeedType.Recent)
            } else if (position == POPULAR_POS) {
                return FeedFragment.newInstance(FeedType.Popular)
            } else if (position == FOLLOWING_POS) {
                return FeedFragment.newInstance(FeedType.Following)
            }

            assert(false) { "Creating fragment for unknown position" }
            // default if error
            return FeedFragment.newInstance(FeedType.Popular)
        }
    }

    companion object {

        const val RECENT_POS = 0
        const val POPULAR_POS = 1
        const val FOLLOWING_POS = 2

        @JvmStatic
        fun newInstance() = FeedTypeFragment()
    }
}