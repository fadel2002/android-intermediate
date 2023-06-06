package com.dicoding.mystory.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.dicoding.mystory.R
import com.dicoding.mystory.data.remote.response.auth.RegisterResponse
import com.dicoding.mystory.data.remote.retrofit.ApiConfig
import com.dicoding.mystory.databinding.FragmentRegisterBinding
import com.dicoding.mystory.views.CustomLoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private lateinit var  binding: FragmentRegisterBinding
    private lateinit var loadingDialog: CustomLoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = CustomLoadingDialog(requireContext())
        binding.tvToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.buttonSignUp.setOnClickListener {
            if (binding.etEmail.text.isNullOrEmpty() || binding.etPassword.text.isNullOrEmpty() || binding.etUsername.text.isNullOrEmpty()){
                Toast.makeText(requireContext(), resources.getString(R.string.please_fill_in_the_fields_correctly), Toast.LENGTH_SHORT).show()
            }else if(!binding.etEmail.error.isNullOrEmpty() || !binding.etPassword.error.isNullOrEmpty()){
                Toast.makeText(requireContext(), resources.getString(R.string.please_fill_in_the_fields_correctly), Toast.LENGTH_SHORT).show()
            }else {
                registerUser()
            }
        }
        setAnimation()
    }

    private fun setAnimation() {
        ObjectAnimator.ofFloat(binding.circleImageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val dur = 200L
        val registrationText =
            ObjectAnimator.ofFloat(binding.tvPleaseSignUp, View.ALPHA, 1f).setDuration(dur)
        val nameField =
            ObjectAnimator.ofFloat(binding.etUsername, View.ALPHA, 1f).setDuration(dur)
        val emailField =
            ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(dur)
        val passwordField =
            ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(dur)
        val loginButton = ObjectAnimator.ofFloat(binding.buttonSignUp, View.ALPHA, 1f).setDuration(dur)
        val loginText = ObjectAnimator.ofFloat(binding.tvToLogin, View.ALPHA, 1f).setDuration(dur)
        val together = AnimatorSet().apply {
            playTogether(loginButton, loginText)
        }

        AnimatorSet().apply {
            playSequentially(
                registrationText,
                nameField,
                emailField,
                passwordField,
                together
            )
            start()
        }
    }

    private fun registerUser() {
        val name = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        loadingDialog.showDialog()
        val client = ApiConfig
            .getApiService()
            .postRegister(
                name,
                email,
                password
            )
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.account_created_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.email_taken),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                loadingDialog.dismissDialog()
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                loadingDialog.dismissDialog()
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.registration_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}