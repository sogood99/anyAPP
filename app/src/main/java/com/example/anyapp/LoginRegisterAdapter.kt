package com.example.anyapp

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.anyapp.api.AccountApi
import com.example.anyapp.databinding.ItemLoginRegisterBinding
import com.example.anyapp.util.Constants
import com.example.anyapp.util.CreateUserResponse
import com.example.anyapp.util.LoginResponse
import com.example.anyapp.util.UserToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class LoginRegisterAdapter : RecyclerView.Adapter<LoginRegisterAdapter.ViewPagerViewHolder>() {
    inner class ViewPagerViewHolder(val binding: ItemLoginRegisterBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val retrofit = Retrofit
        .Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL)
        .build()
    private val accountApi = retrofit.create(AccountApi::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoginRegisterBinding.inflate(layoutInflater, parent, false)
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.binding.apply {
            root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    usernameInput.clearFocus()
                    emailInput.clearFocus()
                    passwordInput.clearFocus()

                    val inputMethodManager =
                        view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            if (position == LoginRegister.LOGIN_POS) {
                // login
                emailInput.visibility = View.GONE

                submitButton.setOnClickListener {
                    val activity = it.context as Activity
                    Log.v("Pity", usernameInput.editText?.text.toString())

                    val call = accountApi.login(
                        usernameInput.editText?.text.toString(),
                        passwordInput.editText?.text.toString()
                    )
                    call.enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.code() != HttpURLConnection.HTTP_OK) {
                                Toast.makeText(it.context, "Login Failed", Toast.LENGTH_SHORT)
                                    .show()
                                return
                            }
                            // otherwise set token and return to home activity
                            UserToken(activity).setToken("Token " + response.body()?.token)
                            activity.finish()
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Log.v("Pity", t.toString())
                        }
                    })
                }

            } else if (position == LoginRegister.REGISTER_POS) {
                // register
                submitButton.setOnClickListener {
                    val activity = it.context as Activity

                    val call = accountApi.createUser(
                        usernameInput.editText?.text.toString(),
                        passwordInput.editText?.text.toString(),
                        emailInput.editText?.text.toString()
                    )
                    call.enqueue(object : Callback<CreateUserResponse> {
                        override fun onResponse(
                            call: Call<CreateUserResponse>,
                            response: Response<CreateUserResponse>
                        ) {
                            Log.v("Pity", response.body().toString())
                            if (response.code() != HttpURLConnection.HTTP_CREATED) {
                                Toast.makeText(it.context, "Register Failed", Toast.LENGTH_SHORT)
                                    .show()
                                return
                            }
                            UserToken(activity).setToken("Token " + response.body()?.token)
                            activity.finish()
                        }

                        override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                            Log.v("Pity", t.toString())
                        }
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}