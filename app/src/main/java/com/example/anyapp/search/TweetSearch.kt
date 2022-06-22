package com.example.anyapp.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.example.anyapp.R
import com.example.anyapp.databinding.ActivityTweetSearchBinding
import com.example.anyapp.feed.FeedFragment
import com.example.anyapp.util.FeedType
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class TweetSearch : AppCompatActivity() {
    private lateinit var binding: ActivityTweetSearchBinding
    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTweetSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.slight_light_blue)

        // set replies
        val feedFragment = FeedFragment.newInstance(FeedType.Search)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.replyFeedLayout, feedFragment)
            commit()
        }

        binding.apply {
            // text only and contains * mutually exclusive
            textOnlyCheck.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    containsImageCheck.isChecked = false
                    containsVideoCheck.isChecked = false
                }
            }
            val containsCheckListener =
                CompoundButton.OnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        textOnlyCheck.isChecked = false
                    }
                }
            containsImageCheck.setOnCheckedChangeListener(containsCheckListener)
            containsVideoCheck.setOnCheckedChangeListener(containsCheckListener)

            selectDateRange.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    val dateRangePicker =
                        MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select dates")
                            .build()
                    dateRangePicker.addOnPositiveButtonClickListener {
                        val startCalendar = Calendar.getInstance()
                        val endCalendar = Calendar.getInstance()
                        startCalendar.timeInMillis = it.first
                        endCalendar.timeInMillis = it.second

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd");
                        dateFormat.timeZone = startCalendar.timeZone

                        startDate = dateFormat.format(startCalendar.time)
                        endDate = dateFormat.format(endCalendar.time)
                    }
                    dateRangePicker.show(supportFragmentManager, "DateRange")
                } else {
                    startDate = null
                    endDate = null
                }
            }

            submitButton.setOnClickListener {
                // format arg
                var searchArg = "-s "
                searchArg += searchTerms.editText?.text.toString()
                searchArg += " "
                if (textOnlyCheck.isChecked) {
                    searchArg += "-to"
                }
                if (containsImageCheck.isChecked) {
                    searchArg += "-i "
                }
                if (containsVideoCheck.isChecked) {
                    searchArg += "-v "
                }
                if (selectDateRange.isChecked) {
                    searchArg += "-df $startDate -dt $endDate"
                }

                feedFragment.search(searchArg)
            }

            toolBar.setNavigationOnClickListener {
                finishAfterTransition()
            }
        }
    }
}