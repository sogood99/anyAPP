package com.example.anyapp.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.anyapp.R
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ActivitySettingsBinding
import com.example.anyapp.databinding.ItemChangePasswordBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.UserToken
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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


        private var alertDialog: AlertDialog? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val alertDialogBuilder = context?.let {
                MaterialAlertDialogBuilder(
                    it,
                    com.google.android.material.R.style.Base_Theme_MaterialComponents_Light_Dialog_Alert
                )
            }

            // setup alert dialog
            alertDialogBuilder?.let {
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

            alertDialog = alertDialogBuilder?.create()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val changePassword = findPreference<Preference>("key")
            changePassword?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                alertDialog?.show()
                true
            }
        }
    }
}