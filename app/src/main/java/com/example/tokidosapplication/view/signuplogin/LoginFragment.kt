package com.example.tokidosapplication.view.signuplogin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    var inputEmail: Editable? = null
    var inputPassword: Editable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)


        binding.loginButton.setOnClickListener{
            println("$inputEmail $inputPassword")
        }
        binding.loginEmailTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isEnableLogInButton(inputPassword, p0)
            }
            override fun afterTextChanged(p0: Editable?) {
                inputEmail = p0
            }
        })
        binding.loginPasswordTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isEnableLogInButton(inputEmail, p0)
            }
            override fun afterTextChanged(p0: Editable?) {
                inputPassword = p0
            }
        })
        binding.loginPasswordInputLayout.setEndIconOnClickListener {
            if (binding.loginPasswordTextField.transformationMethod == null)
                binding.loginPasswordTextField.transformationMethod = PasswordTransformationMethod()
            else
                binding.loginPasswordTextField.transformationMethod = null
        }

        binding.forgetPasswordTextView.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)

        }
        binding.SignUpTextView.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return binding.root
    }
    private fun isEnableLogInButton(input:Editable?, p0:CharSequence?){
        if(input!=null && input.toString().isNotEmpty() && p0?.toString()?.length != 0) {
            binding.loginButton.background = ContextCompat.getDrawable(requireContext(),
                R.drawable.orange_button_background_shape
            )
            binding.loginButton.isEnabled = true
        }
        else {
            binding.loginButton.background = ContextCompat.getDrawable(requireContext(),
                R.drawable.orange_light_button_background_shape
            )
            binding.loginButton.isEnabled = false
        }
    }

}