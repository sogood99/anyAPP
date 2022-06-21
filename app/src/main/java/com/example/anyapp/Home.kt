package com.example.anyapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.anyapp.loginregister.LoginRegister
import com.example.anyapp.databinding.ActivityHomeBinding
import com.example.anyapp.draft.Draft
import com.example.anyapp.draft.DraftListFragment
import com.example.anyapp.feed.FeedFragment
import com.example.anyapp.feed.FeedTypeFragment
import com.example.anyapp.notification.Notification
import com.example.anyapp.notification.NotificationServices
import com.example.anyapp.profile.ProfileFragment
import com.example.anyapp.search.TweetSearch
import com.example.anyapp.settings.SettingsActivity
import com.example.anyapp.util.FeedType
import com.example.anyapp.util.UserToken
import com.google.android.material.bottomsheet.BottomSheetBehavior

class Home : AppCompatActivity() {
    companion object {
        const val HOME_POS = 0
        const val DRAFT_POS = 1
        const val PROFILE_POS = 2
    }

    private lateinit var binding: ActivityHomeBinding
    private var notificationServicesIntent: Intent? = null

    // created as member since
    // https://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
    private val userTokenListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
            if (s == getString(R.string.token_key)) {
                resetFragPager()

                if (notificationServicesIntent != null) {
                    stopService(notificationServicesIntent)
                }

                val userToken =
                    sharedPreferences?.getString(this.getString(R.string.token_key), null)
                notificationServicesIntent = Intent(this, NotificationServices::class.java).apply {
                    putExtra("userToken", userToken)
                }
                startService(notificationServicesIntent)
            }
        }

    private var onDraftSetNewTweet: (Draft) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create binding to activity_home
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // on change listener for token & set token to null initially
        val userToken = UserToken(this)
        userToken.sharedPreferences?.registerOnSharedPreferenceChangeListener(userTokenListener)

        // notification services
        setupNotificationChannel()
        notificationServicesIntent = Intent(this, NotificationServices::class.java).apply {
            putExtra("userToken", userToken.readToken())
        }
        startService(notificationServicesIntent)

        // put in feed fragment
        val pagerAdapter = BottomNavPagerAdapter(this)
        binding.fragPager.adapter = pagerAdapter

        // put in new tweet fragment
        val newTweetFragment = NewTweetFragment.newInstance(isReply = false)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.newTweet, newTweetFragment)
        transaction.commit()

        // its bottomSheet style
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
        // hide newTweet when sent
        newTweetFragment.setTweetCallback {
            BottomSheetBehavior.from(binding.newTweet)
                .setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        // set newtweet when draft set
        onDraftSetNewTweet = {
            newTweetFragment.setNewTweet(it)
            BottomSheetBehavior.from(binding.newTweet)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }

        // For selecting the Menu Items
        binding.homeToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    val intent = Intent(this@Home, TweetSearch::class.java)
                    startActivity(intent)
                    true
                }
                R.id.miNotification -> {
                    val intent = Intent(this@Home, Notification::class.java)
                    startActivity(intent)
                    true
                }
                R.id.miSettings -> {
                    val intent = Intent(this@Home, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.miLogout -> {
                    // setup everything logout related
                    // set token_key to null
                    UserToken(this).setToken(null)

                    // reset adapter
                    resetFragPager()
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        binding.homeButton.setOnClickListener { button ->
            // testing
            resetFragPager()
        }

        // fragPager stuff: page change
        binding.fragPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // set the homeButton text when change position
                if (position == HOME_POS) {
                    binding.homeButton.text = "Home"
                    binding.bottomNav.selectedItemId = R.id.navHome
                } else if (position == DRAFT_POS) {
                    binding.homeButton.text = "Draft"
                    binding.bottomNav.selectedItemId = R.id.navDraft
                } else if (position == PROFILE_POS) {
                    // get user token
                    val token = UserToken(this@Home).readToken()
                    if (token == null) {
                        // intent into login page
                        binding.fragPager.setCurrentItem(HOME_POS, false)
                        startLoginRegisterActivity()
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
                R.id.navDraft -> {
                    binding.fragPager.currentItem = DRAFT_POS
                    true
                }
                R.id.navProfile -> {
                    val token = UserToken(this).readToken()
                    if (token != null) {
                        binding.fragPager.currentItem = PROFILE_POS
                        true
                    } else {
                        startLoginRegisterActivity()
                        false
                    }
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onBackPressed() {
        // custom response to back button
        if (BottomSheetBehavior.from(binding.newTweet).state == BottomSheetBehavior.STATE_EXPANDED) {
            BottomSheetBehavior.from(binding.newTweet).state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (binding.bottomNav.selectedItemId != R.id.navHome) {
            binding.bottomNav.selectedItemId = R.id.navHome
        }
    }

    private fun setupNotificationChannel() {
        // Create the NotificationChannel
        val name = "anyAppChannel"
        val descriptionText = "Channel for anyApp"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("anyAppNotification", name, importance)
        mChannel.description = descriptionText

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun resetFragPager() {
        val adapter = BottomNavPagerAdapter(this)
        binding.fragPager.adapter = adapter

    }

    fun startLoginRegisterActivity() {
        val intent = Intent(this@Home, LoginRegister::class.java)
        startActivity(intent)
    }

    private inner class BottomNavPagerAdapter(
        fa: FragmentActivity,
    ) : FragmentStateAdapter(fa) {
        private val NUM_PAGES = 3

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            if (position == HOME_POS) {
                return FeedTypeFragment.newInstance()
            } else if (position == DRAFT_POS) {
                val draftListFragment = DraftListFragment.newInstance()
                draftListFragment.setOnDraftNewTweet(onDraftSetNewTweet)
                return draftListFragment
            } else if (position == PROFILE_POS) {
                return ProfileFragment.newInstance(true)
            }

            assert(false) { "Creating fragment for unknown position" }
            // default if error
            return FeedFragment.newInstance(FeedType.Recent)
        }
    }
}