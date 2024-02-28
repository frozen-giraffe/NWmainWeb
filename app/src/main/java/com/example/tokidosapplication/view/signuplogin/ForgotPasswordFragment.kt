package com.example.tokidosapplication.view.signuplogin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    var inputEmail: Editable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)

        binding.sendResetLinkButton.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_forgotPasswordFragment_to_verifyEmailFragment)
        }
        binding.resetEmailEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0?.toString()?.length != 0) {
                    binding.sendResetLinkButton.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.orange_button_background_shape
                    )
                    binding.sendResetLinkButton.isEnabled = true
                }
                else {
                    binding.sendResetLinkButton.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.orange_light_button_background_shape
                    )
                    binding.sendResetLinkButton.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
                inputEmail = p0
            }
        })
        // Inflate the layout for this fragment
        return binding.root
    }


}