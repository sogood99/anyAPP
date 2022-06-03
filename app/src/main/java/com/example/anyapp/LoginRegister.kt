package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.anyapp.databinding.ActivityLoginRegisterBinding
import com.google.android.material.tabs.TabLayoutMediator

class LoginRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // attach adapter to ViewPager2
            val adapter = LoginRegisterAdapter()
            loginRegisterFieldPager.adapter = adapter

            // link tablayout to ViewPager2
            TabLayoutMediator(tabLayout, loginRegisterFieldPager) { tab, position ->
                tab.text = "Tab ${position}"
            }.attach()

            // set back button to finish
            backButton.setOnClickListener {
                finish()
            }
        }
    }
}