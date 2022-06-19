package com.example.anyapp.loginregister

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.anyapp.R
import com.example.anyapp.databinding.ActivityLoginRegisterBinding
import com.google.android.material.tabs.TabLayout
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
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        binding.apply {
            // attach adapter to ViewPager2
            val adapter = LoginRegisterAdapter()
            loginRegisterFieldPager.adapter = adapter

            // link tabLayout to ViewPager2
            TabLayoutMediator(tabLayout, loginRegisterFieldPager) { tab, position ->
                var showText = ""
                if (position == LOGIN_POS) {
                    showText = "Login"
                } else if (position == REGISTER_POS) {
                    showText = "Register"
                }
                tab.text = showText
            }.attach()

            // tabLayout onchange
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab == null) {
                        return
                    }
                    var showText = ""
                    if (tab.position == LOGIN_POS) {
                        showText = "Login"
                    } else if (tab.position == REGISTER_POS) {
                        showText = "Register"
                    }
                    toolBar.title = showText
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            })


            // set back button to finish
            toolBar.setNavigationOnClickListener {
                finish()
            }
        }
    }
}