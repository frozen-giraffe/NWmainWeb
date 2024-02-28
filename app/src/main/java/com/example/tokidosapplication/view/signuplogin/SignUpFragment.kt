package com.example.tokidosapplication.view.signuplogin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        binding.LogInTextView.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        return binding.root
    }

}