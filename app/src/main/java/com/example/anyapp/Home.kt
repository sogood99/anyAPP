package com.example.anyapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.anyapp.databinding.ActivityHomeBinding
import com.example.anyapp.util.FeedType
import com.google.android.material.bottomsheet.BottomSheetBehavior

class Home : AppCompatActivity() {
    private val HOME_POS = 0
    private val PROFILE_POS = 1

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // set token for testing
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString(
                getString(R.string.token_key),
                "Token cb8bfb36c9f35898284afbb2f38636d1035aff4a"
            ).apply()

        // Create binding to activity_home
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // put in feed fragment
        val pagerAdapter = BottomNavPagerAdapter(this)
        binding.fragPager.adapter = pagerAdapter

        // put in new tweet fragment
        val newTweetFragment = NewTweetFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.newTweet, newTweetFragment)
        transaction.addToBackStack("NewTweet")
        transaction.commit()

        // its bottomsheet style
        BottomSheetBehavior.from(binding.newTweet).apply {
            peekHeight = 100
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        BottomSheetBehavior.from(binding.newTweet)
            .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(view: View, newState: Int) {
                    val newTweetFragment: NewTweetFragment =
                        supportFragmentManager.findFragmentById(R.id.newTweet) as NewTweetFragment
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            newTweetFragment.show()
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            newTweetFragment.hide()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

            })


        // For selecting the Menu Items
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    Log.v("Pity", "Clicked Search")
                    true
                }
                R.id.miLogout -> {
                    // setup everything logout related
                    // set token_key to null
                    getPreferences(Context.MODE_PRIVATE).edit()
                        .putString(getString(R.string.token_key), null).apply()

                    // reset adapter
                    val adapter = binding.fragPager.adapter
                    binding.fragPager.adapter = null
                    binding.fragPager.adapter = adapter
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            Log.v("Pity", "Clicked Home Button")
        }

        // fragPager stuff: page change
        binding.fragPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // set the homeButton text when change position
                if (position == HOME_POS) {
                    binding.homeButton.text = "Home"
                    binding.bottomNav.selectedItemId = R.id.navHome
                } else if (position == PROFILE_POS) {
                    // get user token
                    val token = getPreferences(Context.MODE_PRIVATE)?.getString(
                        getString(R.string.token_key),
                        null
                    )
                    if (token == null) {
                        // intent into login page
                        binding.fragPager.setCurrentItem(HOME_POS, false)
                    } else {
                        // change to profile
                        binding.homeButton.text = "Profile"
                        binding.bottomNav.selectedItemId = R.id.navProfile
                    }
                }
            }
        })

        // setup botNavBar when clicked
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navHome -> {
                    binding.fragPager.currentItem = HOME_POS
                    true
                }
                R.id.navProfile -> {
                    binding.fragPager.currentItem = PROFILE_POS
                    true
                }
                else -> {
                    false
                }
            }
        }
    }


    private inner class BottomNavPagerAdapter(
        fa: FragmentActivity,
    ) : FragmentStateAdapter(fa) {
        private val NUM_PAGES = 2

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            if (position == HOME_POS) {
                return FeedFragment.newInstance(FeedType.Popular)
            } else if (position == PROFILE_POS) {
                return ProfileFragment.newInstance(true)
            }

            assert(false) { "Creating fragment for unknown position" }
            // default if error
            return FeedFragment.newInstance(FeedType.Popular)
        }
    }
}