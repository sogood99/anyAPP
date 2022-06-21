package com.example.anyapp.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.anyapp.R
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ActivitySettingsBinding
import com.example.anyapp.databinding.ItemChangePasswordBinding
import com.example.anyapp.databinding.ItemUserBinding
import com.example.anyapp.feed.TweetAdapter
import com.example.anyapp.profile.ProfileDetail
import com.example.anyapp.util.Constants
import com.example.anyapp.util.ProfileResponse
import com.example.anyapp.util.UserToken
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val settingsFragment = SettingsFragment()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()
        }

        binding.toolBar.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val retrofit = Retrofit
            .Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL)
            .build()
        private val accountApi = retrofit.create(AccountApi::class.java)

        private var changePasswordDialog: AlertDialog? = null
        private var blockedUserDialog: AlertDialog? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val changePasswordDialogBuilder = context?.let {
                MaterialAlertDialogBuilder(
                    it,
                    com.google.android.material.R.style.Base_Theme_MaterialComponents_Light_Dialog_Alert
                )
            }
            val blockedUserDialogBuilder = context?.let {
                MaterialAlertDialogBuilder(
                    it,
                    com.google.android.material.R.style.Base_Theme_MaterialComponents_Light_Dialog_Alert
                )
            }

            // setup alert dialog
            changePasswordDialogBuilder?.let {
                val itemChangePasswordBinding = ItemChangePasswordBinding.inflate(layoutInflater)
                fun clearDialog() {
                    itemChangePasswordBinding.oldPassword.editText?.text?.clear()
                    itemChangePasswordBinding.newPassword.editText?.text?.clear()
                    itemChangePasswordBinding.confirmPassword.editText?.text?.clear()
                }

                it.setTitle("Change Password")
                it.setView(itemChangePasswordBinding.root)
                it.setPositiveButton("Confirm") { dialog, id ->
                    if (itemChangePasswordBinding.confirmPassword.editText?.text.toString()
                        != itemChangePasswordBinding.newPassword.editText?.text.toString()
                    ) {
                        Toast.makeText(context, "Password Not The Same", Toast.LENGTH_LONG).show()
                    }
                    accountApi.updateUser(
                        UserToken(requireActivity()).readToken(),
                        itemChangePasswordBinding.oldPassword.editText?.text.toString(),
                        itemChangePasswordBinding.newPassword.editText?.text.toString()
                    ).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                Toast.makeText(context, "Unauthorized", Toast.LENGTH_LONG).show()
                                return
                            } else if (response.code() == HttpURLConnection.HTTP_OK) {
                                Toast.makeText(
                                    context,
                                    "Password Change Success",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            } else {
                                Toast.makeText(context, "Bad Request", Toast.LENGTH_LONG).show()
                                return
                            }

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Log.v("Pity", t.toString())
                        }

                    })

                    dialog.cancel()
                    clearDialog()
                }
                it.setNegativeButton("Cancel") { dialog, id ->
                    dialog.cancel()
                    clearDialog()
                }
                it.setOnDismissListener {
                    clearDialog()
                }
            }

            // setup blocked user dialog
            blockedUserDialogBuilder?.let {
                it.setTitle("Blocked Users")
                val linearLayout = LinearLayout(context)
                linearLayout.orientation = LinearLayout.VERTICAL
                accountApi.blockDetail(UserToken(context as Activity).readToken())
                    .enqueue(object : Callback<List<ProfileResponse>> {
                        override fun onResponse(
                            call: Call<List<ProfileResponse>>,
                            response: Response<List<ProfileResponse>>
                        ) {
                            response.body()?.let { profileList ->
                                for (profile in profileList) {
                                    val itemUserBinding =
                                        ItemUserBinding.inflate(layoutInflater)
                                    context?.let { ctx ->
                                        itemUserBinding.userMenuButton.setBackgroundColor(
                                            ContextCompat.getColor(
                                                ctx,
                                                R.color.white
                                            )
                                        )
                                    }

                                    // sync values
                                    itemUserBinding.profileName.text = profile.profileName
                                    itemUserBinding.username.text = profile.username
                                    Picasso.get()
                                        .load(Constants.BASE_URL + "/" + profile.userIconUrl)
                                        .fit()
                                        .into(itemUserBinding.userIcon)
                                    linearLayout.addView(itemUserBinding.root)

                                    // on click go to ProfileDetail
                                    itemUserBinding.userMenuButton.setOnClickListener {
                                        val intent =
                                            Intent(context, ProfileDetail::class.java).apply {
                                                putExtra(TweetAdapter.EXTRA_USER_ID, profile.userId)
                                            }
                                        startActivity(intent)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<ProfileResponse>>, t: Throwable) =
                            Unit
                    })
                val scrollView = ScrollView(context)
                scrollView.addView(linearLayout)
                it.setView(scrollView)
            }


            changePasswordDialog = changePasswordDialogBuilder?.create()
            blockedUserDialog = blockedUserDialogBuilder?.create()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val changePassword = findPreference<Preference>("changePassword")
            changePassword?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                changePasswordDialog?.show()
                true
            }

            val blockedUsers = findPreference<Preference>("blockedUser")
            blockedUsers?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                blockedUserDialog?.show()
                true
            }
        }
    }
}