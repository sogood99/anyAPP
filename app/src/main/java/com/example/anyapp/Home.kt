package com.example.anyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.appbar.MaterialToolbar

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // For selecting the Menu Items
        val topAppBar = findViewById<MaterialToolbar>(R.id.homeToolbar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miSearch -> {
                    Log.v("Pity", "Clicked Search")
                    true
                }
                R.id.miLogout -> {
                    Log.v("Pity", "Clicked Logout")
                    true
                }
                else -> true
            }
        }

        // For selecting the Home
        val homeButton = findViewById<Button>(R.id.homeButton)
        homeButton.setOnClickListener { button ->
            Log.v("Pity", "Clicked Home Button")
            true
        }

    }
}