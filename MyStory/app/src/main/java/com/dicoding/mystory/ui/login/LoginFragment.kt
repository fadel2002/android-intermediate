package com.dicoding.mystory.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.mystory.R
import com.dicoding.mystory.data.local.datastore.UserPreference
import com.dicoding.mystory.data.model.User
import com.dicoding.mystory.data.remote.response.auth.LoginResponse
import com.dicoding.mystory.data.remote.retrofit.ApiConfig
import com.dicoding.mystory.databinding.FragmentLoginBinding
import com.dicoding.mystory.helper.AuthViewModelFactory
import com.dicoding.mystory.views.CustomLoadingDialog
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var preference: UserPreference
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loadingDialog: CustomLoadingDialog
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = CustomLoadingDialog(requireContext())
        binding.tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        configurationViewModel(requireContext())
        binding.buttonSignIn.setOnClickListener {
            if (binding.etEmail.text.isNullOrEmpty() || binding.etPassword.text.isNullOrEmpty()){
                Toast.makeText(requireContext(), resources.getString(R.string.please_fill_in_the_fields_correctly), Toast.LENGTH_SHORT).show()
            }else if(!binding.etEmail.error.isNullOrEmpty() || !binding.etPassword.error.isNullOrEmpty()){
                Toast.makeText(requireContext(), resources.getString(R.string.please_fill_in_the_fields_correctly), Toast.LENGTH_SHORT).show()
            }else {
                loginAuthenticationUser()
            }
        }
        setAnimation()

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                requireActivity().finishAffinity()
                return@setOnKeyListener true
            }
            false
        }

        dataStore = requireContext().dataStore
        preference = UserPreference.getInstance(dataStore)
        checkLoginStatus()
    }

    private fun setAnimation() {
        ObjectAnimator.ofFloat(binding.circleImageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val dur = 200L
        val loginText = ObjectAnimator.ofFloat(binding.tvPleaseSignIn, View.ALPHA, 1f).setDuration(dur)
        val emailField =
            ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(dur)
        val passwordField =
            ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(dur)
        val loginButton = ObjectAnimator.ofFloat(binding.buttonSignIn, View.ALPHA, 1f).setDuration(dur)
        val registerText =
            ObjectAnimator.ofFloat(binding.tvToRegister, View.ALPHA, 1f).setDuration(dur)
        val together = AnimatorSet().apply {
            playTogether(loginButton, registerText)
        }

        AnimatorSet().apply {
            playSequentially(loginText, emailField, passwordField, together)
            start()
        }
    }

    private fun loginAuthenticationUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        loadingDialog.showDialog()
        val client = ApiConfig
            .getApiService()
            .postLogin(
                email,
                password
            )
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val user = User(
                        userId = responseBody.loginResult.userId,
                        name = responseBody.loginResult.name,
                        token = responseBody.loginResult.token,
                        isLogin = true
                    )
                    loginViewModel.saveUser(user)
                    Toast.makeText(requireContext(), resources.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.user_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                loadingDialog.dismissDialog()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loadingDialog.dismissDialog()
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.user_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun configurationViewModel(context: Context) {
        val dataStore: DataStore<Preferences> = context.dataStore
        loginViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(
                UserPreference
                    .getInstance(dataStore)
            )
        )[LoginViewModel::class.java]
    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            preference.getUser().collect { user ->
                if (user.isLogin) {
                    findNavController().navigate(R.id.action_loginFragment_to_storyFragment)
                }
            }
        }
    }
}