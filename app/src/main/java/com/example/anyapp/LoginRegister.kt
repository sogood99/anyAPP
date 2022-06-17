package com.example.anyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.anyapp.databinding.ActivityLoginRegisterBinding
import com.google.android.material.tabs.TabLayoutMediator

class LoginRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginRegisterBinding

    companion object {
        const val LOGIN_POS = 0
        const val REGISTER_POS = 1
    }

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
                if (position == LOGIN_POS) {
                    tab.text = "Login"
                } else if (position == REGISTER_POS) {
                    tab.text = "Register"
                }
            }.attach()

            // set back button to finish
            toolBar.setNavigationOnClickListener {
                finish()
            }
        }
    }
}