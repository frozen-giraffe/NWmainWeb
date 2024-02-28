package com.example.tokidosapplication.view.signuplogin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.FragmentVerifyEmailBinding

class VerifyEmailFragment : Fragment() {
    private lateinit var binding: FragmentVerifyEmailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyEmailBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


}