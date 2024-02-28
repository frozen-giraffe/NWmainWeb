package com.example.tokidosapplication.view.playcubes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tokidosapplication.MainActivity
import com.example.tokidosapplication.databinding.FragmentAllDoneBinding


class AllDoneFragment : Fragment() {
    private lateinit var binding: FragmentAllDoneBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllDoneBinding.inflate(layoutInflater, container, false)
        binding.goDashboardButton.setOnClickListener {
            startActivity(Intent(activity,MainActivity::class.java))
            activity?.finish()
        }
        return binding.root
    }


}